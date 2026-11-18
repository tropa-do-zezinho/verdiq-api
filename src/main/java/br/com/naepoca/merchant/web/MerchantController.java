package br.com.naepoca.merchant.web;

import br.com.naepoca.merchant.domain.Compra;
import br.com.naepoca.merchant.domain.LoteEnvio;
import br.com.naepoca.merchant.domain.Perda;
import br.com.naepoca.merchant.domain.Venda;
import br.com.naepoca.merchant.service.MerchantService;
import br.com.naepoca.seasonality.domain.Recomendacao;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/merchant")
@Tag(name = "Comerciante")
@PreAuthorize("hasRole('COMERCIANTE') or hasRole('ADMIN')")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/purchases")
    public Compra purchase(@RequestBody Map<String, Object> body) {
        return merchantService.registerPurchase(body);
    }

    @PostMapping("/sales")
    public Venda sale(@RequestBody Map<String, Object> body) {
        return merchantService.registerSale(body);
    }

    @PostMapping("/losses")
    public Perda loss(@RequestBody Map<String, Object> body) {
        return merchantService.registerLoss(body);
    }

    @GetMapping("/records")
    public Map<String, Object> records(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long produtoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate
    ) {
        return merchantService.listRecords(tipo, produtoId, de, ate);
    }

    @GetMapping("/lots")
    public List<LoteEnvio> lots() {
        return merchantService.lots();
    }

    @GetMapping("/lots/{id}")
    public Map<String, Object> lot(@PathVariable Long id) {
        return merchantService.lotDetail(id);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate
    ) {
        return merchantService.dashboard(de, ate);
    }

    @GetMapping("/recommendations")
    public List<Recomendacao> recommendations() {
        return merchantService.recommendations();
    }

    @GetMapping("/comparativo")
    public Map<String, Object> comparativo(@RequestParam Long produtoId) {
        return merchantService.praçaComparativo(produtoId);
    }

    @GetMapping("/export")
    public ResponseEntity<String> export(@RequestParam String tipo) {
        String csv = merchantService.exportCsv(tipo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export-" + tipo.toLowerCase() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping(value = "/imports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> imports(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipo") String tipo
    ) {
        return merchantService.importSpreadsheet(file, tipo);
    }
}
