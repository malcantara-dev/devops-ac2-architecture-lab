"""ia-service: fronteira de IA da plataforma (Desafio 3).

Servico Python independente do Core: tem deploy, escala e ciclo de atualizacao proprios.
Se ele cair, o Core continua funcionando com a recomendacao de fallback.
"""
from fastapi import FastAPI
from pydantic import BaseModel, Field

from . import assistente, recomendacao

app = FastAPI(title="ia-service", version="1.0.0",
              description="Recomendacao de cursos (RF03) e assistente educacional com RAG (RF04)")


class PedidoRecomendacao(BaseModel):
    usuarioId: int
    concluidos: list[str] = Field(default_factory=list)
    catalogo: list[str] = Field(default_factory=list)
    limite: int = 3


class Pergunta(BaseModel):
    pergunta: str


@app.get("/health")
def health():
    return {"status": "UP"}


@app.post("/recomendacoes")
def recomendar(pedido: PedidoRecomendacao):
    return {
        "usuarioId": pedido.usuarioId,
        "modelo": "conteudo-jaccard-v1",
        "recomendacoes": recomendacao.recomendar(pedido.concluidos, pedido.catalogo, pedido.limite),
    }


@app.post("/assistente/perguntar")
def perguntar(pergunta: Pergunta):
    return assistente.perguntar(pergunta.pergunta)
