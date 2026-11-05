package org.example.grupo_7_praticaatdd.recomendacao.application;

import java.util.List;

// Porta de saida para a IA. Qualquer falha do motor e tratada pelo caso de uso com fallback.
public interface MotorDeRecomendacao {

    List<String> recomendar(Long usuarioId, List<String> concluidos, List<String> catalogo);
}
