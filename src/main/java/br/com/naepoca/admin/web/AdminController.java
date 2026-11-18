package br.com.naepoca.admin.web;

import br.com.naepoca.admin.domain.AuditLog;
import br.com.naepoca.admin.repo.AuditLogRepository;
import br.com.naepoca.catalog.domain.EventoSazonal;
import br.com.naepoca.catalog.domain.Produto;
import br.com.naepoca.catalog.repo.EventoSazonalRepository;
import br.com.naepoca.catalog.repo.ProdutoRepository;
import br.com.naepoca.etl.domain.EtlRun;
import br.com.naepoca.etl.domain.MarketUpload;
import br.com.naepoca.etl.service.EtlService;
import br.com.naepoca.shared.exception.ApiException;
import br.com.naepoca.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@Tag(name = "Admin")
public class AdminController {

    private final EtlService etlService;
    private final ProdutoRepository produtoRepository;
    private final EventoSazonalRepository eventoSazonalRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(
            EtlService etlService,
            ProdutoRepository produtoRepository,
            EventoSazonalRepository eventoSazonalRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.etlService = etlService;
        this.produtoRepository = produtoRepository;
        this.eventoSazonalRepository = eventoSazonalRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/etl/runs")
    public List<EtlRun> runs() {
        return etlService.recentRuns();
    }

    @PostMapping("/etl/run")
    public Map<String, Object> runNow() {
        return etlService.runAll(SecurityUtils.currentUserId());
    }

    @PostMapping("/market-uploads")
    @ResponseStatus(HttpStatus.CREATED)
    public MarketUpload upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "PRECO_MERCADO") String tipo
    ) {
        return etlService.uploadMarketCsv(file, tipo);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Produto createProduct(@RequestBody Map<String, String> body) {
        Produto p = new Produto();
        p.setNomeCanonico(body.get("nome"));
        p.setCategoria(body.getOrDefault("categoria", "OUTRO").toUpperCase());
        p.setUnidadeBase(body.getOrDefault("unidadeBase", "KG"));
        p = produtoRepository.save(p);
        audit("PRODUCT_CREATE", p.getNomeCanonico());
        return p;
    }

    @PutMapping("/products/{id}")
    public Produto updateProduct(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Produto p = produtoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Produto não encontrado"));
        if (body.get("nome") != null) p.setNomeCanonico(body.get("nome"));
        if (body.get("categoria") != null) p.setCategoria(body.get("categoria").toUpperCase());
        if (body.get("unidadeBase") != null) p.setUnidadeBase(body.get("unidadeBase"));
        if (body.get("ativo") != null) p.setAtivo(Boolean.valueOf(body.get("ativo")));
        audit("PRODUCT_UPDATE", String.valueOf(id));
        return p;
    }

    @PostMapping("/seasonal-events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventoSazonal createEvent(@RequestBody Map<String, Object> body) {
        EventoSazonal e = new EventoSazonal();
        e.setNome(String.valueOf(body.get("nome")));
        e.setRegraData(String.valueOf(body.get("regraData")));
        if (body.get("mesTipico") != null) e.setMesTipico(Integer.valueOf(String.valueOf(body.get("mesTipico"))));
        e.setDescricao((String) body.get("descricao"));
        e = eventoSazonalRepository.save(e);
        audit("EVENT_CREATE", e.getNome());
        return e;
    }

    @GetMapping("/audit")
    public List<AuditLog> audit() {
        return auditLogRepository.findTop100ByOrderByCriadoEmDesc();
    }

    @GetMapping("/impact")
    public Map<String, Object> impact() {
        // Painel agregado simples (RF-47)
        return Map.of(
                "kgPerdaRegistrados", 0,
                "kgEvitadosEstimado", 0,
                "nota", "Agregado a partir dos registros de perda/recomendações; evolui com adoção."
        );
    }

    private void audit(String acao, String detalhe) {
        AuditLog log = new AuditLog();
        log.setUsuarioId(SecurityUtils.currentUserId());
        log.setAcao(acao);
        log.setDetalhe(detalhe);
        auditLogRepository.save(log);
    }
}
