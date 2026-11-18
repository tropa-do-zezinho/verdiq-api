package br.com.naepoca.offers.web;

import br.com.naepoca.offers.domain.Oferta;
import br.com.naepoca.offers.domain.OfertaDenuncia;
import br.com.naepoca.offers.service.OfferService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@Tag(name = "Ofertas")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/offers")
    public List<Oferta> list(
            @RequestParam(required = false) Long produtoId,
            @RequestParam(required = false) Boolean ultimaChance
    ) {
        return offerService.listPublic(produtoId, ultimaChance);
    }

    @PostMapping("/offers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COMERCIANTE')")
    public Oferta create(@RequestBody Map<String, Object> body) {
        return offerService.create(body);
    }

    @PostMapping("/offers/{id}/report")
    @PreAuthorize("isAuthenticated()")
    public OfertaDenuncia report(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return offerService.report(id, body == null ? null : body.get("motivo"));
    }

    @PostMapping("/me/favorites/products/{produtoId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void favProduct(@PathVariable Long produtoId) {
        offerService.favoriteProduct(produtoId);
    }

    @PostMapping("/me/favorites/merchants/{comercianteId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void favMerchant(@PathVariable Long comercianteId) {
        offerService.favoriteMerchant(comercianteId);
    }

    @PostMapping("/me/shopping-lists")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createList(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> produtos = body.get("produtos") == null ? List.of() :
                ((List<?>) body.get("produtos")).stream().map(v -> Long.valueOf(String.valueOf(v))).toList();
        Long id = offerService.createShoppingList((String) body.get("nome"), produtos);
        return Map.of("id", id);
    }

    @GetMapping("/me/shopping-lists")
    @PreAuthorize("isAuthenticated()")
    public List<Map<String, Object>> lists() {
        return offerService.myLists();
    }
}
