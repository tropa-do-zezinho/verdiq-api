package br.com.naepoca.plans.web;

import br.com.naepoca.plans.domain.InteressePlano;
import br.com.naepoca.plans.domain.Plano;
import br.com.naepoca.plans.repo.InteressePlanoRepository;
import br.com.naepoca.plans.repo.PlanoRepository;
import br.com.naepoca.shared.exception.ApiException;
import br.com.naepoca.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/plans")
@Tag(name = "Planos")
public class PlanController {

    private final PlanoRepository planoRepository;
    private final InteressePlanoRepository interessePlanoRepository;

    public PlanController(PlanoRepository planoRepository, InteressePlanoRepository interessePlanoRepository) {
        this.planoRepository = planoRepository;
        this.interessePlanoRepository = interessePlanoRepository;
    }

    @GetMapping
    public List<Plano> list() {
        return planoRepository.findByAtivoTrue();
    }

    @PostMapping("/{codigo}/interest")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> interest(@PathVariable String codigo) {
        Plano plano = planoRepository.findByCodigo(codigo.toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PLAN_NOT_FOUND", "Plano não encontrado"));
        InteressePlano interesse = new InteressePlano();
        interesse.setUsuarioId(SecurityUtils.currentUserId());
        interesse.setPlanoId(plano.getId());
        interessePlanoRepository.save(interesse);
        return Map.of(
                "status", "INTERESSE_REGISTRADO",
                "plano", plano.getCodigo(),
                "mensagem", "Sem pagamento integrado no MVP. Interesse registrado."
        );
    }
}
