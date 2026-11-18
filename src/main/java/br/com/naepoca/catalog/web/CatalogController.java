package br.com.naepoca.catalog.web;

import br.com.naepoca.catalog.domain.EventoSazonal;
import br.com.naepoca.catalog.service.CatalogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@Tag(name = "Catálogo")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    public List<Map<String, Object>> products(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String epoca
    ) {
        return catalogService.search(q, categoria, epoca);
    }

    @GetMapping("/products/{id}")
    public Map<String, Object> product(@PathVariable Long id, @RequestParam(required = false) Long pracaId) {
        return catalogService.productDetail(id, pracaId);
    }

    @GetMapping("/products/{id}/prices")
    public Object prices(@PathVariable Long id, @RequestParam(required = false) Long pracaId) {
        return catalogService.productDetail(id, pracaId).get("seriePrecos");
    }

    @GetMapping("/products/{id}/seasonality")
    public Object seasonality(@PathVariable Long id, @RequestParam(required = false) Long pracaId) {
        Map<String, Object> detail = catalogService.productDetail(id, pracaId);
        return Map.of(
                "calendario12Meses", detail.get("calendario12Meses"),
                "seloEpoca", detail.get("seloEpoca"),
                "substitutos", detail.get("substitutos")
        );
    }

    @GetMapping("/seasons/now")
    public Map<String, Object> seasonsNow(@RequestParam(required = false) String uf) {
        return catalogService.seasonsNow(uf);
    }

    @GetMapping("/seasonal-events")
    public List<EventoSazonal> events() {
        return catalogService.eventos();
    }
}
