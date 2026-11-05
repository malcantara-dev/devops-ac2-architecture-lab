/**
 * Modulo Recomendacao, RF03 do B1 (Desafio 3).
 *
 * <p>O Core nao calcula recomendacoes: ele pede ao ia-service (Python) pela porta
 * {@link org.example.grupo_7_praticaatdd.recomendacao.application.MotorDeRecomendacao}.
 * O adapter HTTP tem timeout e circuit breaker; se a IA falhar, o caso de uso responde com o
 * fallback (cursos do catalogo ainda nao concluidos) e o fluxo essencial do Core continua.</p>
 */
package org.example.grupo_7_praticaatdd.recomendacao;
