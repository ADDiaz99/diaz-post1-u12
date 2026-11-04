package com.empresa.reembolsos;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo por HTTP con Spring, JPA y H2 reales: crear, validar, calcular y notificar.
 * Cada prueba usa un empleado distinto para que las solicitudes de una no sean
 * "duplicadas" de las de otra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FlujoSolicitudReembolsoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions crear(String empleado, String categoria, String monto, int dias, boolean documentos)
            throws Exception {
        String json = """
                {"empleadoId": "%s", "categoria": "%s", "montoSolicitado": %s,
                 "fechaGasto": "2026-10-05", "diasViaje": %d, "documentosSoporte": %b}
                """.formatted(empleado, categoria, monto, dias, documentos);
        return mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long crearYObtenerId(String empleado, String categoria, String monto, int dias) throws Exception {
        String respuesta = crear(empleado, categoria, monto, dias, true)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private ResultActions cambiarEstado(long id, String estado) throws Exception {
        return mockMvc.perform(put("/api/solicitudes/{id}/estado", id)
                .contentType(MediaType.APPLICATION_JSON).content("\"" + estado + "\""));
    }

    private static BigDecimal montoAprobado(String respuesta) {
        return new BigDecimal(JsonPath.read(respuesta, "$.montoAprobado").toString());
    }

    @Test
    void creaSolicitudesDeLasTresCategoriasConSuMontoAprobado() throws Exception {
        String viaje = crear("emp-cat-1", "VIAJE", "400000", 3, true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();
        String representacion = crear("emp-cat-2", "REPRESENTACION", "1500000", 0, true)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String capacitacion = crear("emp-cat-3", "CAPACITACION", "1000000", 0, true)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(montoAprobado(viaje)).isEqualByComparingTo("400000");
        assertThat(montoAprobado(representacion)).isEqualByComparingTo("1500000");
        assertThat(montoAprobado(capacitacion)).isEqualByComparingTo("800000");
    }

    @Test
    void rechazaUnaSolicitudSinDocumentosConUnMensajeClaro() throws Exception {
        crear("emp-sin-docs", "REPRESENTACION", "100000", 0, false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Faltan documentos soporte"));
    }

    @Test
    void rechazaUnaSegundaSolicitudPendienteDeLaMismaCategoriaYMes() throws Exception {
        crearYObtenerId("emp-duplicado", "CAPACITACION", "500000", 0);

        crear("emp-duplicado", "CAPACITACION", "300000", 0, true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "Ya existe una solicitud pendiente de la misma categoria en el periodo 2026-10"));
    }

    @Test
    void aprobarNotificaALosTresInteresados(CapturedOutput salida) throws Exception {
        long id = crearYObtenerId("emp-aprobado", "VIAJE", "200000", 2);

        cambiarEstado(id, "APROBADA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        assertThat(salida.getOut())
                .contains("Notificando a emp-aprobado: su solicitud paso de PENDIENTE a APROBADA")
                // el monto vuelve de la base de datos con dos decimales: 200000.00
                .containsPattern("Contabilidad: programar pago de 200000(\\.00)? para emp-aprobado")
                .contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a APROBADA");
    }

    @Test
    void rechazarNoLeInteresaAContabilidad(CapturedOutput salida) throws Exception {
        long id = crearYObtenerId("emp-rechazado", "REPRESENTACION", "300000", 0);

        cambiarEstado(id, "RECHAZADA").andExpect(status().isOk());

        assertThat(salida.getOut())
                .contains("Notificando a emp-rechazado: su solicitud paso de PENDIENTE a RECHAZADA")
                .contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a RECHAZADA")
                .doesNotContain("para emp-rechazado");
    }

    @Test
    void cambiarEstadoDeUnaSolicitudInexistenteDevuelve404() throws Exception {
        cambiarEstado(999_999L, "APROBADA")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Solicitud no encontrada: 999999"));
    }
}
