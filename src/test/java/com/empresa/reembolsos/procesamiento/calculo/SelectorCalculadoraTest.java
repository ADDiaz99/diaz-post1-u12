package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectorCalculadoraTest {

    @Test
    void devuelveLaCalculadoraDeCadaCategoria() {
        var selector = new SelectorCalculadora(List.of(
                new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion()));

        for (CategoriaGasto categoria : CategoriaGasto.values()) {
            assertThat(selector.obtener(categoria).getCategoria()).isEqualTo(categoria);
        }
    }

    @Test
    void categoriaSinCalculadoraLanzaExcepcion() {
        var selector = new SelectorCalculadora(List.of(new CalculadoraViaje()));

        assertThatThrownBy(() -> selector.obtener(CategoriaGasto.REPRESENTACION))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Categoria no soportada");
    }
}
