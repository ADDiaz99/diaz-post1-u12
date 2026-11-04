package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import static com.empresa.reembolsos.Solicitudes.nueva;
import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    @Test
    void reembolsaElMontoCompletoDentroDelTope() {
        assertThat(calculadora.calcular(nueva(CategoriaGasto.REPRESENTACION, "1500000")))
                .isEqualByComparingTo("1500000");
    }

    @Test
    void limitaAlTopeMensual() {
        assertThat(calculadora.calcular(nueva(CategoriaGasto.REPRESENTACION, "2500000")))
                .isEqualByComparingTo("2000000");
    }
}
