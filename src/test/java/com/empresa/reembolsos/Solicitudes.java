package com.empresa.reembolsos;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Fabrica de solicitudes validas para las pruebas; cada prueba cambia solo lo que le importa. */
public final class Solicitudes {

    private Solicitudes() {
    }

    public static SolicitudReembolso nueva(CategoriaGasto categoria, String monto) {
        var solicitud = new SolicitudReembolso();
        solicitud.setEmpleadoId("emp-001");
        solicitud.setCategoria(categoria);
        solicitud.setMontoSolicitado(new BigDecimal(monto));
        solicitud.setFechaGasto(LocalDate.of(2026, 10, 5));
        solicitud.setDocumentosSoporte(true);
        return solicitud;
    }

    public static SolicitudReembolso viaje(String monto, int dias) {
        var solicitud = nueva(CategoriaGasto.VIAJE, monto);
        solicitud.setDiasViaje(dias);
        return solicitud;
    }
}
