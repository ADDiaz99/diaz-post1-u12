package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.Solicitudes.nueva;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();

    private static SolicitudReembolso pendiente(CategoriaGasto categoria, LocalDate fecha) {
        var pendiente = nueva(categoria, "50000");
        pendiente.setFechaGasto(fecha);
        return pendiente;
    }

    @Test
    void sinPendientesEsValida() {
        assertThat(validador.validar(nueva(CategoriaGasto.REPRESENTACION, "100000"), List.of()).valida()).isTrue();
    }

    @Test
    void mismaCategoriaEnElMismoMesSeRechaza() {
        var pendientes = List.of(pendiente(CategoriaGasto.REPRESENTACION, LocalDate.of(2026, 10, 20)));

        var resultado = validador.validar(nueva(CategoriaGasto.REPRESENTACION, "100000"), pendientes);

        assertThat(resultado.valida()).isFalse();
        assertThat(resultado.motivoRechazo()).contains("2026-10");
    }

    @Test
    void mismoMesDeOtroAnioNoEsDuplicado() {
        // la guia comparaba solo getMonth(): octubre de 2025 chocaba con octubre de 2026
        var pendientes = List.of(pendiente(CategoriaGasto.REPRESENTACION, LocalDate.of(2025, 10, 5)));

        assertThat(validador.validar(nueva(CategoriaGasto.REPRESENTACION, "100000"), pendientes).valida()).isTrue();
    }

    @Test
    void otraCategoriaEnElMismoMesNoEsDuplicado() {
        var pendientes = List.of(pendiente(CategoriaGasto.CAPACITACION, LocalDate.of(2026, 10, 5)));

        assertThat(validador.validar(nueva(CategoriaGasto.REPRESENTACION, "100000"), pendientes).valida()).isTrue();
    }
}
