package org.example.grupo_7_praticaatdd.observabilidade;

import org.example.grupo_7_praticaatdd.recomendacao.application.MotorDeRecomendacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Desafio 6: as metricas que validam os ASRs aparecem no endpoint que o Prometheus coleta.
@SpringBootTest
@AutoConfigureMockMvc
class ObservabilidadeTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MotorDeRecomendacao motor;

    @Test
    @DisplayName("IA fora do ar + ranking consultado aparecem no /actuator/prometheus")
    void metricasDosAsrsNoPrometheus() throws Exception {
        when(motor.recomendar(any(), anyList(), anyList())).thenThrow(new RuntimeException("Connection refused"));
        for (int i = 0; i < 3; i++) {
            mvc.perform(get("/api/recomendacoes/usuario/1")).andExpect(status().isOk());
        }
        mvc.perform(get("/api/ranking")).andExpect(status().isOk());

        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("recomendacao_resultado_total{")))
                .andExpect(content().string(containsString("origem=\"FALLBACK\"")))
                .andExpect(content().string(containsString("ranking_consulta_seconds_count")));
    }
}
