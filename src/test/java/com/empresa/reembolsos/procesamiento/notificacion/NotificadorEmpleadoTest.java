package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.empresa.reembolsos.Solicitudes.nueva;
import static org.assertj.core.api.Assertions.assertThat;

class NotificadorEmpleadoTest {

    private final NotificadorEmpleado notificador = new NotificadorEmpleado();
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

    @Test
    void avisaAlEmpleadoCualquierCambioDeEstado() {
        var solicitud = nueva(CategoriaGasto.VIAJE, "100000");
        solicitud.setEstado(EstadoSolicitud.REQUIERE_CORRECCION);

        notificador.onCambioEstado(solicitud, EstadoSolicitud.PENDIENTE);

        assertThat(salida.toString())
                .contains("Notificando a emp-001: su solicitud paso de PENDIENTE a REQUIERE_CORRECCION");
    }
}
