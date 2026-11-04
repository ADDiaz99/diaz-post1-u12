package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Elige la estrategia de calculo de cada categoria (Decision de diseno 2).
 * <p>
 * Spring inyecta todas las {@link CalculadoraReembolso} registradas y el mapa se arma
 * solo: una categoria nueva no exige tocar esta clase. Si dos calculadoras declaran la
 * misma categoria, {@code Collectors.toMap} lanza una excepcion al arrancar.
 */
@Component
public class SelectorCalculadora {

    private final Map<CategoriaGasto, CalculadoraReembolso> calculadoras;

    public SelectorCalculadora(List<CalculadoraReembolso> lista) {
        this.calculadoras = lista.stream()
                .collect(Collectors.toMap(CalculadoraReembolso::getCategoria, Function.identity()));
    }

    public CalculadoraReembolso obtener(CategoriaGasto categoria) {
        return Optional.ofNullable(calculadoras.get(categoria))
                .orElseThrow(() -> new IllegalArgumentException("Categoria no soportada: " + categoria));
    }
}
