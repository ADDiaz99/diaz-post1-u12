package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

/**
 * Estrategia de calculo del reembolso de una categoria de gasto (patron Strategy).
 * <p>
 * Todo lo que cambia de una categoria a otra vive en su implementacion: el tope que
 * se puede solicitar y la formula del monto aprobado. Agregar una categoria nueva es
 * agregar una implementacion anotada con {@code @Component}, sin tocar las existentes
 * ni el {@link SelectorCalculadora}.
 */
public interface CalculadoraReembolso {

    /** Categoria de gasto que atiende esta estrategia. */
    CategoriaGasto getCategoria();

    /**
     * Monto maximo que se puede solicitar en esta categoria para esta solicitud.
     * Lo usa la validacion de monto (RF1) para no duplicar los topes.
     */
    BigDecimal topeSolicitud(SolicitudReembolso solicitud);

    /** Monto que se aprueba para la solicitud segun la formula de la categoria (RF2). */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
