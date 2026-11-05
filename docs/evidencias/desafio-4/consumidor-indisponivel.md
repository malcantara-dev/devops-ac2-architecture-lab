# Consumidor indisponível: sync × async (Desafio 4)

Hipótese: com a notificação assíncrona, a falha do consumidor não impede a conclusão do curso;
com a notificação síncrona, impede.

Comando: `./mvnw test -Dtest='Notificacao*Test'`

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in org.example.grupo_7_praticaatdd.notificacao.NotificacaoAssincronaTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in org.example.grupo_7_praticaatdd.notificacao.NotificacaoSincronaTest
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Modo sync (`notificacao.modo=sync`) com o worker fora do ar

`NotificacaoSincronaTest` aponta o worker para `http://localhost:1` (conexão recusada).
Resultado: `PUT /api/matriculas/{id}/concluir` responde **503 Service Unavailable** e a matrícula
continua **EM_ANDAMENTO**, porque a exceção da notificação desfaz a transação. A falha de um serviço
secundário derrubou a operação essencial.

## Modo async (`notificacao.modo=async`, padrão)

`NotificacaoAssincronaTest` verifica com Mockito (`verify` / `verifyNoInteractions`):

| Situação | Resultado |
|---|---|
| Concluir com nota 8 | 200; `CursoConcluidoEvento` publicado **uma vez**, depois do commit, com `aproveitamento = true` |
| Nota parcial | 200; nenhum evento publicado |
| Matrícula inexistente (transação falha) | 400; nenhum evento publicado |
| Broker fora do ar (`AmqpConnectException`) | 200 e matrícula **CONCLUIDO**; a falha só aparece no log |

Com o broker no ar e o **worker parado**, o Core nem percebe: a mensagem fica na fila durável
`notificacoes.curso-concluido` (mensagem persistente) até o worker voltar.

## Como reproduzir com os containers

```bash
docker compose up -d --build
docker stop gamificacao-notificacao                 # consumidor fora do ar
# concluir um curso pelo Swagger: 200 e CONCLUIDO
# http://localhost:15672 (guest/guest): fila notificacoes.curso-concluido com 1 mensagem pronta
docker start gamificacao-notificacao                # a mensagem é consumida
docker logs gamificacao-notificacao                 # "[notificacao] Parabens! ..."

# comparação com o modo sync
NOTIFICACAO_MODO=sync docker compose up -d app && docker stop gamificacao-notificacao
# concluir um curso: 503 e a matrícula continua EM_ANDAMENTO
```
