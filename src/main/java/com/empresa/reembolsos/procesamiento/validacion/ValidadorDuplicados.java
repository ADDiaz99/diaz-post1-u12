package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

/**
 * Verifica que el empleado no tenga otra solicitud pendiente de la misma categoria en
 * el mismo periodo (mes y anio).
 * <p>
 * No consulta la base de datos: recibe las pendientes ya cargadas por el servicio, asi
 * el paquete procesamiento no depende de la infraestructura.
 */
@Component
public class ValidadorDuplicados extends ValidadorSolicitud {

    // VIOLACION INTENCIONAL de la Regla 1 (procesamiento no depende de infraestructura).
    // Es un campo y no solo un import: un import sin usar no queda en el bytecode y ArchUnit no lo veria.
    // Se deshace con git revert.
    private RepositorioSolicitudesJpa repositorio;

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud,
                                                List<SolicitudReembolso> pendientesDelEmpleado) {
        if (solicitud.getFechaGasto() == null) {
            return ResultadoValidacion.rechazar("Debe indicar la fecha del gasto");
        }
        var periodo = YearMonth.from(solicitud.getFechaGasto());
        boolean existeDuplicado = pendientesDelEmpleado.stream()
                .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && YearMonth.from(p.getFechaGasto()).equals(periodo));
        return existeDuplicado
                ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo " + periodo)
                : ResultadoValidacion.ok();
    }
}
