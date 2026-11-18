package br.com.naepoca.offers.service;

import br.com.naepoca.merchant.domain.Comerciante;
import br.com.naepoca.merchant.repo.ComercianteRepository;
import br.com.naepoca.offers.domain.Oferta;
import br.com.naepoca.offers.domain.OfertaDenuncia;
import br.com.naepoca.offers.repo.OfertaDenunciaRepository;
import br.com.naepoca.offers.repo.OfertaRepository;
import br.com.naepoca.shared.exception.ApiException;
import br.com.naepoca.shared.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class OfferService {

    private final OfertaRepository ofertaRepository;
    private final OfertaDenunciaRepository denunciaRepository;
    private final ComercianteRepository comercianteRepository;
    private final JdbcTemplate jdbcTemplate;

    public OfferService(
            OfertaRepository ofertaRepository,
            OfertaDenunciaRepository denunciaRepository,
            ComercianteRepository comercianteRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.ofertaRepository = ofertaRepository;
        this.denunciaRepository = denunciaRepository;
        this.comercianteRepository = comercianteRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<Oferta> listPublic(Long produtoId, Boolean ultimaChance) {
        return ofertaRepository.findByStatusAndFimGreaterThanEqual("ATIVA", LocalDate.now()).stream()
                .filter(o -> produtoId == null || produtoId.equals(o.getProdutoId()))
                .filter(o -> ultimaChance == null || ultimaChance.equals(o.getUltimaChance()))
                .toList();
    }

    @Transactional
    public Oferta create(Map<String, Object> body) {
        Comerciante c = comercianteRepository.findByUsuarioId(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NOT_MERCHANT", "Somente comerciante"));
        BigDecimal normal = new BigDecimal(String.valueOf(body.get("precoNormal")));
        BigDecimal promo = new BigDecimal(String.valueOf(body.get("precoPromocional")));
        if (promo.compareTo(normal) >= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RN11", "Preço promocional deve ser menor que o normal");
        }
        Oferta o = new Oferta();
        o.setComercianteId(c.getId());
        o.setProdutoId(Long.valueOf(String.valueOf(body.get("produtoId"))));
        o.setPrecoNormal(normal);
        o.setPrecoPromocional(promo);
        if (body.get("quantidadeKg") != null) o.setQuantidadeKg(new BigDecimal(String.valueOf(body.get("quantidadeKg"))));
        o.setInicio(LocalDate.parse(String.valueOf(body.getOrDefault("inicio", LocalDate.now()))));
        o.setFim(LocalDate.parse(String.valueOf(body.get("fim"))));
        o.setUltimaChance(Boolean.TRUE.equals(body.get("ultimaChance")) || "true".equalsIgnoreCase(String.valueOf(body.get("ultimaChance"))));
        if (o.getUltimaChance() && body.get("loteInfo") == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RN12", "Selo última chance exige lote_info");
        }
        o.setLoteInfo((String) body.get("loteInfo"));
        return ofertaRepository.save(o);
    }

    @Transactional
    public OfertaDenuncia report(Long ofertaId, String motivo) {
        Long userId = SecurityUtils.currentUserId();
        if (denunciaRepository.existsByOfertaIdAndUsuarioId(ofertaId, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_REPORTED", "Você já denunciou esta oferta");
        }
        Oferta oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "OFFER_NOT_FOUND", "Oferta não encontrada"));
        OfertaDenuncia d = new OfertaDenuncia();
        d.setOfertaId(ofertaId);
        d.setUsuarioId(userId);
        d.setMotivo(motivo);
        denunciaRepository.save(d);
        if (denunciaRepository.countByOfertaId(ofertaId) >= 3) {
            oferta.setStatus("REVISAO"); // RN-19
        }
        return d;
    }

    @Transactional
    public void favoriteProduct(Long produtoId) {
        jdbcTemplate.update(
                "INSERT INTO favorito_produto(usuario_id, produto_id) VALUES (?,?) ON CONFLICT DO NOTHING",
                SecurityUtils.currentUserId(), produtoId);
    }

    @Transactional
    public void favoriteMerchant(Long comercianteId) {
        jdbcTemplate.update(
                "INSERT INTO favorito_comerciante(usuario_id, comerciante_id) VALUES (?,?) ON CONFLICT DO NOTHING",
                SecurityUtils.currentUserId(), comercianteId);
    }

    @Transactional
    public Long createShoppingList(String nome, List<Long> produtos) {
        Long userId = SecurityUtils.currentUserId();
        Long listaId = jdbcTemplate.queryForObject(
                "INSERT INTO lista_compras_epoca(usuario_id, nome) VALUES (?,?) RETURNING id",
                Long.class, userId, nome == null ? "Lista da vez" : nome);
        if (produtos != null) {
            for (Long pid : produtos) {
                jdbcTemplate.update(
                        "INSERT INTO lista_compras_item(lista_id, produto_id) VALUES (?,?) ON CONFLICT DO NOTHING",
                        listaId, pid);
            }
        }
        return listaId;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> myLists() {
        return jdbcTemplate.query(
                "select id, nome, criado_em from lista_compras_epoca where usuario_id = ? order by criado_em desc",
                (rs, i) -> Map.of(
                        "id", rs.getLong("id"),
                        "nome", rs.getString("nome"),
                        "criadoEm", rs.getTimestamp("criado_em").toInstant().toString()
                ),
                SecurityUtils.currentUserId());
    }
}
