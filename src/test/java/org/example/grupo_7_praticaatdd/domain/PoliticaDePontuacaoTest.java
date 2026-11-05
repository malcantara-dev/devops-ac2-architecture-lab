package org.example.grupo_7_praticaatdd.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PoliticaDePontuacaoTest {

    @Test
    @DisplayName("RF01 - curso concluido vale 100, moeda vale 10 e credito vale 1")
    void devePontuarCursosMoedasECreditos() {
        assertEquals(0, PoliticaDePontuacao.pontuar(0, 0, 0));
        assertEquals(103, PoliticaDePontuacao.pontuar(1, 0, 3));
        assertEquals(1266, PoliticaDePontuacao.pontuar(12, 3, 36));
    }
}
