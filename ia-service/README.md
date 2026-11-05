# ia-service

Serviço Python (FastAPI) com a fronteira de IA da plataforma, criado no Desafio 3 do B1.

| Endpoint | RF | O que faz |
|---|---|---|
| `GET /health` | - | Saúde do serviço |
| `POST /recomendacoes` | RF03 | Recomenda cursos do catálogo parecidos com os que o aluno já concluiu |
| `POST /assistente/perguntar` | RF04 | Responde dúvidas sobre as regras da plataforma com RAG simplificado, citando a fonte |

```bash
pip install -r requirements-dev.txt
pytest                                   # testes
uvicorn app.main:app --port 8000         # http://localhost:8000/docs
```

Também sobe pelo `docker compose up -d --build ia-service` na raiz do repositório.
