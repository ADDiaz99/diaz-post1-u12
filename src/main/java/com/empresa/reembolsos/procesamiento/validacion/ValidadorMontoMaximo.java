package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Verifica que el monto no exceda el tope de la categoria.
 * <p>
 * El tope no se repite aqui en un mapa propio: se le pregunta a la estrategia de la
 * categoria. Asi el tope de viaje depende de los dias del viaje, y una categoria nueva
 * no obliga a editar este validador.
 */
@Component
public class ValidadorMontoMaximo extends ValidadorSolicitud {

    private final SelectorCalculadora calculadoras;

    public ValidadorMontoMaximo(SelectorCalculadora calculadoras) {
        this.calculadoras = calculadoras;
    }

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud,
                                                List<SolicitudReembolso> pendientesDelEmpleado) {
        if (solicitud.getCategoria() == null) {
            return ResultadoValidacion.rechazar("Debe indicar la categoria del gasto");
        }
        var monto = solicitud.getMontoSolicitado();
        if (monto == null || monto.signum() <= 0) {
            return ResultadoValidacion.rechazar("El monto solicitado debe ser mayor que cero");
        }
        var tope = calculadoras.obtener(solicitud.getCategoria()).topeSolicitud(solicitud);
        return monto.compareTo(tope) > 0
                ? ResultadoValidacion.rechazar("Monto excede el tope de la categoria "
                        + solicitud.getCategoria() + " (" + tope.toPlainString() + ")")
                : ResultadoValidacion.ok();
    }
}
