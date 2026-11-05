"""RF04 - Assistente educacional com RAG simplificado (retrieval + generation)."""
from .conhecimento import TRECHOS
from .texto import similaridade, tokens


def recuperar(pergunta: str, quantidade: int = 2) -> list[dict]:
    consulta = tokens(pergunta)
    pontuados = [(similaridade(consulta, tokens(t["texto"] + " " + t["fonte"])), t) for t in TRECHOS]
    pontuados.sort(key=lambda par: par[0], reverse=True)
    return [t for nota, t in pontuados[:quantidade] if nota > 0]


def gerar_resposta(pergunta: str, trechos: list[dict]) -> str:
    if not trechos:
        return "Nao encontrei essa informacao nas regras da plataforma."
    return " ".join(t["texto"] for t in trechos)


def perguntar(pergunta: str) -> dict:
    trechos = recuperar(pergunta)
    return {
        "resposta": gerar_resposta(pergunta, trechos),
        "fontes": [t["fonte"] for t in trechos],
    }
