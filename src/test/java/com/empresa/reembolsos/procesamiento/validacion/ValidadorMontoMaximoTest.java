package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraCapacitacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraRepresentacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraViaje;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.nueva;
import static com.empresa.reembolsos.Solicitudes.viaje;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo(new SelectorCalculadora(List.of(
            new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion())));

    @Test
    void viajeDentroDelTopeDeSusDiasEsValido() {
        // 3 dias x 150.000 = 450.000; con el tope fijo de la guia (150.000) se habria rechazado
        assertThat(validador.validar(viaje("400000", 3), List.of()).valida()).isTrue();
    }

    @Test
    void viajeQueSuperaElTopeDeSusDiasSeRechaza() {
        var resultado = validador.validar(viaje("400000", 2), List.of());

        assertThat(resultado.valida()).isFalse();
        assertThat(resultado.motivoRechazo()).isEqualTo("Monto excede el tope de la categoria VIAJE (300000)");
    }

    @Test
    void representacionSobreElTopeSeRechaza() {
        assertThat(validador.validar(nueva(CategoriaGasto.REPRESENTACION, "2000001"), List.of()).valida()).isFalse();
    }

    @Test
    void montoIgualAlTopeEsValido() {
        assertThat(validador.validar(nueva(CategoriaGasto.CAPACITACION, "3000000"), List.of()).valida()).isTrue();
    }

    @Test
    void montoCeroSeRechaza() {
        var resultado = validador.validar(nueva(CategoriaGasto.REPRESENTACION, "0"), List.of());

        assertThat(resultado.motivoRechazo()).isEqualTo("El monto solicitado debe ser mayor que cero");
    }

    @Test
    void sinCategoriaSeRechaza() {
        var solicitud = nueva(CategoriaGasto.REPRESENTACION, "100000");
        solicitud.setCategoria(null);

        assertThat(validador.validar(solicitud, List.of()).motivoRechazo())
                .isEqualTo("Debe indicar la categoria del gasto");
    }
}
