# Falha simulada do ia-service (Desafio 3)

Hipótese: a indisponibilidade da IA não pode bloquear o fluxo essencial do Core.

## 1. ia-service no ar (uvicorn app.main:app --port 8000)

`POST /recomendacoes` com o histórico "Spring Boot Fundamentos":

```json
{"usuarioId":7,"modelo":"conteudo-jaccard-v1","recomendacoes":[{"curso":"Spring Boot Avancado","score":0.5},{"curso":"Docker na Pratica","score":0.0},{"curso":"Kubernetes","score":0.0}]}
```

`POST /assistente/perguntar` com "Quando viro Premium?" (RAG cita a fonte):

```json
{"resposta":"Curso em andamento nao gera credito, mesmo com nota parcial alta. O credito so e concedido quando o curso e finalizado. Ao concluir o 12o curso com aproveitamento o plano passa de Basico para Premium e o aluno recebe 3 moedas, que podem virar novos cursos ou ser acumuladas.","fontes":["Curso em andamento (cenarios 3 e 4)","Plano Premium (cenarios 7 e 8)"]}
```

## 2. ia-service derrubado (processo encerrado)

```
curl: (7) Failed to connect to localhost port 8000 - conexao recusada (exit=7)
```

## 3. Comportamento do Core com a IA fora do ar

Os testes abaixo usam o mesmo contrato HTTP do ia-service, simulado com `MockRestServiceServer`:
timeout de leitura, erro 500 e circuito aberto depois de 3 falhas seguidas (a 4ª chamada nem
chega ao ia-service). No caso de uso, qualquer falha vira resposta com `origem = FALLBACK` e
os cursos do catálogo ainda não concluídos, sem erro para o aluno.

```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 9.499 s -- in org.example.grupo_7_praticaatdd.recomendacao.application.RecomendacaoServiceTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 9.291 s -- in org.example.grupo_7_praticaatdd.recomendacao.internal.IaServiceMotorTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Como reproduzir com tudo no ar: `docker compose up -d --build`, depois
`curl localhost:8080/api/recomendacoes/usuario/1` (origem IA), `docker stop gamificacao-ia` e a
mesma chamada (origem FALLBACK, resposta em até 2 s e, a partir da 4ª, imediata pelo circuito aberto).
