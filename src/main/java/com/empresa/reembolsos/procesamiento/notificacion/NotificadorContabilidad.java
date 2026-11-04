package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

/** Contabilidad solo reacciona a las aprobaciones, para programar el pago. */
@Component
public class NotificadorContabilidad implements ObservadorCambioEstado {

    @Override
    public void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior) {
        if (solicitud.getEstado() == EstadoSolicitud.APROBADA) {
            System.out.println("Contabilidad: programar pago de " + solicitud.getMontoAprobado()
                    + " para " + solicitud.getEmpleadoId());
        }
    }
}
