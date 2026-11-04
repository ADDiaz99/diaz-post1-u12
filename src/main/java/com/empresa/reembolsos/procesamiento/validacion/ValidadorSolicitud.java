package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.util.List;

/**
 * Eslabon de la cadena de validacion de solicitudes (patron Chain of Responsibility).
 * <p>
 * Cada eslabon hace una sola verificacion. Si la solicitud no la pasa, la cadena se corta
 * con ese motivo; si la pasa, se entrega al siguiente eslabon. Ningun eslabon sabe cuantos
 * vienen despues, asi que una verificacion nueva se agrega como subclase sin modificar las
 * existentes.
 */
public abstract class ValidadorSolicitud {

    private ValidadorSolicitud siguiente;

    /**
     * Encadena el siguiente eslabon y lo devuelve, para escribir
     * {@code a.siguiente(b).siguiente(c)}.
     */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    public ResultadoValidacion validar(SolicitudReembolso solicitud,
                                       List<SolicitudReembolso> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    /** La verificacion particular de este eslabon. */
    protected abstract ResultadoValidacion validarPropio(SolicitudReembolso solicitud,
                                                         List<SolicitudReembolso> pendientesDelEmpleado);
}
