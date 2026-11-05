package org.example.grupo_7_praticaatdd.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromocaoPremiumTest {

    private static void concluirCursos(Usuario usuario, int quantidade) {
        for (int i = 1; i <= quantidade; i++) {
            Matricula matricula = usuario.matricularEm(new Curso("Curso " + i));
            usuario.concluirCurso(matricula, 8.0);
        }
    }

    @Test
    @DisplayName("Cenario 7 - ao concluir o 12o curso com aproveitamento o plano vira Premium e recebe 3 moedas")
    void devePromoverParaPremiumNoDecimoSegundoCurso() {
        // GIVEN - dado um usuario com 11 cursos concluidos com aproveitamento
        Usuario usuario = new Usuario("Fernanda");
        concluirCursos(usuario, 11);

        // WHEN - quando ele conclui o 12o curso com nota maior ou igual a 7
        Matricula decimoSegundo = usuario.matricularEm(new Curso("Curso 12"));
        usuario.concluirCurso(decimoSegundo, 7.5);

        // THEN - entao o plano passa a ser Premium e ele recebe 3 moedas
        assertEquals(12, usuario.getAssinatura().getCursosConcluidosComSucesso());
        assertEquals(PlanoAssinatura.PREMIUM, usuario.getAssinatura().getPlano());
        assertEquals(3, usuario.getAssinatura().getMoedas());
    }

    @Test
    @DisplayName("Cenario 8 - com 11 cursos concluidos o plano continua Basico e sem moedas")
    void naoDevePromoverAntesDoDecimoSegundoCurso() {
        // GIVEN - dado um usuario novo
        Usuario usuario = new Usuario("Gustavo");

        // WHEN - quando ele conclui 11 cursos com aproveitamento
        concluirCursos(usuario, 11);

        // THEN - entao continua no plano Basico, sem moedas
        assertEquals(11, usuario.getAssinatura().getCursosConcluidosComSucesso());
        assertEquals(PlanoAssinatura.BASICO, usuario.getAssinatura().getPlano());
        assertEquals(0, usuario.getAssinatura().getMoedas());
    }

    @Test
    @DisplayName("Cenario 7 - quem ja e Premium nao recebe as 3 moedas de novo no 13o curso")
    void naoDeveCreditarMoedasDeNovoAposPremium() {
        // GIVEN - dado um usuario que ja virou Premium no 12o curso
        Usuario usuario = new Usuario("Helena");
        concluirCursos(usuario, 12);

        // WHEN - quando ele conclui mais um curso com aproveitamento
        concluirCursos(usuario, 1);

        // THEN - entao continua Premium e com as mesmas 3 moedas
        assertEquals(13, usuario.getAssinatura().getCursosConcluidosComSucesso());
        assertEquals(PlanoAssinatura.PREMIUM, usuario.getAssinatura().getPlano());
        assertEquals(3, usuario.getAssinatura().getMoedas());
    }
}
