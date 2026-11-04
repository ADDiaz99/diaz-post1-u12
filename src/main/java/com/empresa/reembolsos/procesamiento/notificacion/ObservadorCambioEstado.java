package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Interesado en los cambios de estado de las solicitudes (patron Observer).
 * <p>
 * El servicio de aplicacion avisa a todos los observadores registrados sin conocer que
 * hace cada uno. Un interesado nuevo, como el panel de metricas, es una implementacion
 * mas anotada con {@code @Component}.
 */
public interface ObservadorCambioEstado {

    /**
     * Se invoca despues de guardar el cambio de estado.
     *
     * @param solicitud      la solicitud ya con su estado nuevo
     * @param estadoAnterior el estado que tenia antes del cambio
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
