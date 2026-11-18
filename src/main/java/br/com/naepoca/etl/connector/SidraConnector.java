package br.com.naepoca.etl.connector;

import br.com.naepoca.catalog.domain.Produto;
import br.com.naepoca.catalog.repo.ProdutoRepository;
import br.com.naepoca.config.NaEpocaProperties;
import br.com.naepoca.market.domain.Producao;
import br.com.naepoca.market.repo.ProducaoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SidraConnector {

    private static final Logger log = LoggerFactory.getLogger(SidraConnector.class);

    /** FLV temporárias (tabela 1612, c81) + permanentes (1613, c82). */
    private static final List<SidraItem> ITENS = List.of(
            new SidraItem(1612, 81, 2715, "Tomate", "LEGUME"),
            new SidraItem(1612, 81, 83375, "Alface", "VERDURA"),
            new SidraItem(1612, 81, 2697, "Cebola", "LEGUME"),
            new SidraItem(1612, 81, 2695, "Batata", "LEGUME"),
            new SidraItem(1612, 81, 83381, "Cenoura", "LEGUME"),
            new SidraItem(1612, 81, 83378, "Morango", "FRUTA"),
            new SidraItem(1612, 81, 2709, "Melancia", "FRUTA"),
            new SidraItem(1613, 82, 2720, "Banana Nanica", "FRUTA"),
            new SidraItem(1613, 82, 2733, "Laranja Pera", "FRUTA"),
            new SidraItem(1613, 82, 2734, "Limão Taiti", "FRUTA"),
            new SidraItem(1612, 81, 2688, "Abacaxi", "FRUTA")
    );

    private final RestClient restClient;
    private final NaEpocaProperties properties;
    private final ProdutoRepository produtoRepository;
    private final ProducaoRepository producaoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SidraConnector(
            RestClient.Builder builder,
            NaEpocaProperties properties,
            ProdutoRepository produtoRepository,
            ProducaoRepository producaoRepository
    ) {
        this.restClient = builder.build();
        this.properties = properties;
        this.produtoRepository = produtoRepository;
        this.producaoRepository = producaoRepository;
    }

    public Map<String, Object> fetchPamSample() {
        List<Map<String, Object>> salvos = new ArrayList<>();
        List<String> erros = new ArrayList<>();
        for (SidraItem item : ITENS) {
            try {
                Map<String, Object> row = fetchAndPersist(item);
                if (row != null) salvos.add(row);
            } catch (Exception e) {
                log.warn("SIDRA item {} failed: {}", item.nomeCanonico(), e.getMessage());
                erros.add(item.nomeCanonico() + ": " + e.getMessage());
            }
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fonte", "IBGE_SIDRA");
        payload.put("registros", salvos.size());
        payload.put("produtos", salvos);
        if (!erros.isEmpty()) {
            payload.put("avisos", erros);
        }
        if (salvos.isEmpty()) {
            payload.put("error", "Nenhum registro SIDRA persistido");
            payload.put("fallback", "SEED_KEEP_LAST");
        }
        return payload;
    }

    private Map<String, Object> fetchAndPersist(SidraItem item) throws Exception {
        // n3/35 = UF São Paulo
        String url = properties.etl().sidra().baseUrl()
                + "/values/t/" + item.tabela()
                + "/n3/35/v/214/p/last/c" + item.classificacao() + "/" + item.categoriaId();
        String body = restClient.get().uri(url).retrieve().body(String.class);
        List<Map<String, String>> rows = objectMapper.readValue(body, new TypeReference<>() {});
        if (rows == null || rows.size() < 2) {
            throw new IllegalStateException("Sem dados para " + item.nomeCanonico());
        }
        Map<String, String> data = rows.getLast();
        String valor = data.get("V");
        if (valor == null || valor.isBlank() || "-".equals(valor) || "...".equals(valor)) {
            throw new IllegalStateException("Valor indisponível");
        }
        int ano = Integer.parseInt(data.getOrDefault("D3C", data.getOrDefault("D3N", "0")));
        BigDecimal toneladas = new BigDecimal(valor.replace(",", "."));

        Produto produto = resolveProduto(item);
        Producao producao = producaoRepository
                .findByProdutoIdAndUfAndAnoAndFonte(produto.getId(), "SP", ano, "IBGE_SIDRA")
                .orElseGet(Producao::new);
        producao.setProdutoId(produto.getId());
        producao.setUf("SP");
        producao.setAno(ano);
        producao.setProducaoT(toneladas);
        producao.setFonte("IBGE_SIDRA");
        producaoRepository.save(producao);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("produtoId", produto.getId());
        out.put("nome", produto.getNomeCanonico());
        out.put("ano", ano);
        out.put("uf", "SP");
        out.put("producaoT", toneladas);
        out.put("sidraNome", data.get("D4N"));
        out.put("url", url);
        return out;
    }

    private Produto resolveProduto(SidraItem item) {
        return produtoRepository.findAll().stream()
                .filter(p -> namesMatch(p.getNomeCanonico(), item.nomeCanonico()))
                .findFirst()
                .orElseGet(() -> {
                    Produto novo = new Produto();
                    novo.setNomeCanonico(item.nomeCanonico());
                    novo.setCategoria(item.categoria());
                    novo.setUnidadeBase("KG");
                    novo.setAtivo(true);
                    return produtoRepository.save(novo);
                });
    }

    private static boolean namesMatch(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        return na.equals(nb) || na.contains(nb) || nb.contains(na);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase()
                .replace("á", "a").replace("ã", "a").replace("â", "a")
                .replace("é", "e").replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o").replace("ô", "o").replace("õ", "o")
                .replace("ú", "u")
                .replace("ç", "c")
                .replace("-inglesa", "")
                .replace("(cacho)", "")
                .trim();
    }

    private record SidraItem(int tabela, int classificacao, int categoriaId, String nomeCanonico, String categoria) {}
}
