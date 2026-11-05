package org.example.grupo_7_praticaatdd.gamificacao;

import org.example.grupo_7_praticaatdd.domain.Assinatura;
import org.example.grupo_7_praticaatdd.domain.PlanoAssinatura;
import org.example.grupo_7_praticaatdd.gamificacao.application.AssinaturaRepositorio;
import org.example.grupo_7_praticaatdd.gamificacao.application.GamificacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Contrato da porta AssinaturaRepositorio. Os mesmos testes rodam contra o adapter em memoria
// e contra o adapter JPA: se os dois passam, o caso de uso nao depende de qual esta plugado.
public abstract class AssinaturaRepositorioContrato {

    protected abstract AssinaturaRepositorio repositorio();

    protected abstract Long novoUsuario(String nome);

    @Test
    @DisplayName("Contrato - salvar e buscar preserva creditos, cursos, plano e moedas")
    void devePreservarEstadoCompleto() {
        Long id = novoUsuario("Joana");

        repositorio().salvar(id, Assinatura.restaurar(5, 12, PlanoAssinatura.PREMIUM, 3));
        Assinatura lida = repositorio().buscarPorUsuario(id);

        assertEquals(5, lida.getCreditosCursos());
        assertEquals(12, lida.getCursosConcluidosComSucesso());
        assertEquals(PlanoAssinatura.PREMIUM, lida.getPlano());
        assertEquals(3, lida.getMoedas());
    }

    @Test
    @DisplayName("Contrato - 12 conclusoes pelo caso de uso promovem para Premium")
    void casoDeUsoPromovePeloAdapter() {
        Long id = novoUsuario("Kleber");
        GamificacaoService service = new GamificacaoService(repositorio());

        for (int i = 0; i < 12; i++) {
            service.registrarConclusaoComSucesso(id);
        }

        Assinatura lida = repositorio().buscarPorUsuario(id);
        assertEquals(PlanoAssinatura.PREMIUM, lida.getPlano());
        assertEquals(3, lida.getMoedas());
        assertEquals(36, lida.getCreditosCursos());
    }

    @Test
    @DisplayName("Contrato - sem credito o caso de uso recusa e nada muda")
    void semCreditoRecusa() {
        Long id = novoUsuario("Lia");
        GamificacaoService service = new GamificacaoService(repositorio());

        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> service.consumirCredito(id));

        assertEquals(Assinatura.MENSAGEM_CREDITOS_INSUFICIENTES, erro.getMessage());
        assertEquals(0, repositorio().buscarPorUsuario(id).getCreditosCursos());
    }

    @Test
    @DisplayName("Contrato - usuario inexistente gera erro de negocio")
    void usuarioInexistente() {
        assertThrows(IllegalArgumentException.class, () -> repositorio().buscarPorUsuario(999_999L));
    }
}
