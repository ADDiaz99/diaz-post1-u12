package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.nueva;
import static org.assertj.core.api.Assertions.assertThat;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    @Test
    void aceptaSiTieneDocumentosSoporte() {
        var resultado = validador.validar(nueva(CategoriaGasto.REPRESENTACION, "100000"), List.of());

        assertThat(resultado.valida()).isTrue();
    }

    @Test
    void rechazaSiFaltanDocumentosSoporte() {
        var solicitud = nueva(CategoriaGasto.REPRESENTACION, "100000");
        solicitud.setDocumentosSoporte(false);

        var resultado = validador.validar(solicitud, List.of());

        assertThat(resultado.valida()).isFalse();
        assertThat(resultado.motivoRechazo()).isEqualTo("Faltan documentos soporte");
    }
}
