package com.empresa.reembolsos.procesamiento.calculo;

import org.junit.jupiter.api.Test;

import static com.empresa.reembolsos.Solicitudes.viaje;
import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    @Test
    void elTopeEsElTopeDiarioPorLosDiasDelViaje() {
        assertThat(calculadora.topeSolicitud(viaje("100000", 3))).isEqualByComparingTo("450000");
    }

    @Test
    void reembolsaTodoSiNoSuperaElTopeDelViaje() {
        assertThat(calculadora.calcular(viaje("400000", 3))).isEqualByComparingTo("400000");
    }

    @Test
    void limitaAlTopeDiarioPorLosDias() {
        assertThat(calculadora.calcular(viaje("400000", 2))).isEqualByComparingTo("300000");
    }
}
