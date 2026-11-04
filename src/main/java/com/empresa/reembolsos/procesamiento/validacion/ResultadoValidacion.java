package com.empresa.reembolsos.procesamiento.validacion;

/** Resultado de pasar una solicitud por la cadena de validadores. */
public record ResultadoValidacion(boolean valida, String motivoRechazo) {

    public static ResultadoValidacion ok() {
        return new ResultadoValidacion(true, null);
    }

    public static ResultadoValidacion rechazar(String motivo) {
        return new ResultadoValidacion(false, motivo);
    }
}
