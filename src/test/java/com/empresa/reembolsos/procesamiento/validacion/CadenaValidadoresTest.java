package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraCapacitacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraRepresentacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraViaje;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.nueva;
import static org.assertj.core.api.Assertions.assertThat;

class CadenaValidadoresTest {

    private final CadenaValidadores cadena = new CadenaValidadores(
            new ValidadorDocumentosSoporte(),
            new ValidadorMontoMaximo(new SelectorCalculadora(List.of(
                    new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion()))),
            new ValidadorDuplicados());

    @Test
    void solicitudCorrectaPasaLosTresEslabones() {
        assertThat(cadena.validar(nueva(CategoriaGasto.CAPACITACION, "1000000"), List.of()).valida()).isTrue();
    }

    @Test
    void laCadenaSeCortaEnElPrimerEslabonQueFalla() {
        // falla en documentos y en monto: gana el primero de la cadena
        var solicitud = nueva(CategoriaGasto.REPRESENTACION, "9000000");
        solicitud.setDocumentosSoporte(false);

        assertThat(cadena.validar(solicitud, List.of()).motivoRechazo()).isEqualTo("Faltan documentos soporte");
    }

    @Test
    void elDuplicadoSeDetectaAlFinalDeLaCadena() {
        var pendientes = List.of(nueva(CategoriaGasto.CAPACITACION, "200000"));

        var resultado = cadena.validar(nueva(CategoriaGasto.CAPACITACION, "1000000"), pendientes);

        assertThat(resultado.motivoRechazo()).startsWith("Ya existe una solicitud pendiente");
    }
}
