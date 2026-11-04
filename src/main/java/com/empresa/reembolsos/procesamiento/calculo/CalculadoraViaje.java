package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Viaticos de viaje: tope diario multiplicado por los dias del viaje. */
@Component
public class CalculadoraViaje implements CalculadoraReembolso {

    private static final BigDecimal TOPE_DIARIO = new BigDecimal("150000");

    @Override
    public CategoriaGasto getCategoria() {
        return CategoriaGasto.VIAJE;
    }

    @Override
    public BigDecimal topeSolicitud(SolicitudReembolso solicitud) {
        return TOPE_DIARIO.multiply(BigDecimal.valueOf(solicitud.getDiasViaje()));
    }

    @Override
    public BigDecimal calcular(SolicitudReembolso solicitud) {
        return solicitud.getMontoSolicitado().min(topeSolicitud(solicitud));
    }
}
