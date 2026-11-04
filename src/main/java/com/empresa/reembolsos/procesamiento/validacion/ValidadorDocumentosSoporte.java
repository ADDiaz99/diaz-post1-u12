package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.List;

/** Verifica que el empleado haya adjuntado los documentos soporte. */
@Component
public class ValidadorDocumentosSoporte extends ValidadorSolicitud {

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud,
                                                List<SolicitudReembolso> pendientesDelEmpleado) {
        return solicitud.isDocumentosSoporte()
                ? ResultadoValidacion.ok()
                : ResultadoValidacion.rechazar("Faltan documentos soporte");
    }
}
