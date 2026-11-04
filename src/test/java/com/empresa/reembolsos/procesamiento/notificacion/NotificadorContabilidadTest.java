package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.nueva;
import static org.assertj.core.api.Assertions.assertThat;

class NotificadorContabilidadTest {

    private final NotificadorContabilidad notificador = new NotificadorContabilidad();
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();
    private PrintStream salidaOriginal;

    @BeforeEach
    void capturarConsola() {
        salidaOriginal = System.out;
        System.setOut(new PrintStream(salida, true));
    }

    @AfterEach
    void restaurarConsola() {
        System.setOut(salidaOriginal);
    }

    private static SolicitudReembolso solicitudEn(EstadoSolicitud estado) {
        var solicitud = nueva(CategoriaGasto.CAPACITACION, "1000000");
        solicitud.setMontoAprobado(new BigDecimal("800000"));
        solicitud.setEstado(estado);
        return solicitud;
    }

    @Test
    void programaElPagoCuandoLaSolicitudSeAprueba() {
        notificador.onCambioEstado(solicitudEn(EstadoSolicitud.APROBADA), EstadoSolicitud.PENDIENTE);

        assertThat(salida.toString()).contains("Contabilidad: programar pago de 800000 para emp-001");
    }

    @Test
    void ignoraLosEstadosQueNoSonAprobada() {
        notificador.onCambioEstado(solicitudEn(EstadoSolicitud.RECHAZADA), EstadoSolicitud.PENDIENTE);
        notificador.onCambioEstado(solicitudEn(EstadoSolicitud.REQUIERE_CORRECCION), EstadoSolicitud.PENDIENTE);

        assertThat(salida.toString()).isEmpty();
    }
}
