"""Base de conhecimento do assistente (RF04).

Cada trecho e uma regra da plataforma. O assistente recupera os trechos mais parecidos com a
pergunta (retrieval) e monta a resposta so com eles (generation), sempre citando a fonte.
Trocar este gerador por um LLM real muda so a funcao gerar_resposta em assistente.py.
"""

TRECHOS = [
    {
        "fonte": "Regra de creditos (cenarios 1 e 2)",
        "texto": "Ao concluir um curso com nota maior ou igual a 7 o aluno recebe 3 creditos de curso gratuito. "
                 "Com nota menor que 7 o curso fica concluido mas nenhum credito e adicionado.",
    },
    {
        "fonte": "Curso em andamento (cenarios 3 e 4)",
        "texto": "Curso em andamento nao gera credito, mesmo com nota parcial alta. "
                 "O credito so e concedido quando o curso e finalizado.",
    },
    {
        "fonte": "Desbloqueio de curso bonus (cenarios 5 e 6)",
        "texto": "Para desbloquear um curso bonus o aluno precisa ter pelo menos 1 credito. "
                 "O desbloqueio consome um credito. Sem credito aparece o aviso creditos insuficientes.",
    },
    {
        "fonte": "Plano Premium (cenarios 7 e 8)",
        "texto": "Ao concluir o 12o curso com aproveitamento o plano passa de Basico para Premium "
                 "e o aluno recebe 3 moedas, que podem virar novos cursos ou ser acumuladas.",
    },
    {
        "fonte": "Ranking (RF01)",
        "texto": "O ranking soma 100 pontos por curso concluido, 10 pontos por moeda e 1 ponto por credito.",
    },
]
