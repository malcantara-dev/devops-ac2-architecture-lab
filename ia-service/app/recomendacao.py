"""RF03 - Recomendacao inteligente de cursos.

Modelo baseado em conteudo: cada curso do catalogo recebe uma nota pela semelhanca do titulo com
os cursos que o aluno ja concluiu. Cursos ja concluidos nunca sao recomendados.
"""
from .texto import similaridade, tokens


def recomendar(concluidos: list[str], catalogo: list[str], limite: int = 3) -> list[dict]:
    perfil = set()
    for titulo in concluidos:
        perfil |= tokens(titulo)

    ja_feitos = {t.strip().lower() for t in concluidos}
    candidatos = []
    for posicao, titulo in enumerate(catalogo):
        if titulo.strip().lower() in ja_feitos:
            continue
        nota = similaridade(perfil, tokens(titulo))
        candidatos.append((-nota, posicao, titulo, nota))

    candidatos.sort()
    return [{"curso": titulo, "score": round(nota, 3)} for _, _, titulo, nota in candidatos[:limite]]
