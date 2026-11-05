# Métricas que validam os ASRs e gate de deploy (Desafio 6)

## 1. Condição provocada e vista no /actuator/prometheus

Cenário executado contra o Core (perfil H2, endpoint que o Prometheus coleta):

1. 4 recomendações com o ia-service respondendo;
2. ia-service fora do ar: mais 6 recomendações;
3. 5 consultas ao ranking;
4. o mesmo evento IoT (`eventId = ev-1`) entregue 3 vezes.

Linhas do `/actuator/prometheus` depois do cenário:

```
http_server_requests_seconds_count{application="gamificacao-core",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/api/recomendacoes/usuario/{usuarioId}"} 10
iot_eventos_total{application="gamificacao-core",status="duplicado"} 2.0
iot_eventos_total{application="gamificacao-core",status="registrado"} 1.0
ranking_consulta_seconds_count{application="gamificacao-core"} 5
ranking_consulta_seconds_max{application="gamificacao-core"} 0.0529043
recomendacao_resultado_total{application="gamificacao-core",origem="FALLBACK"} 6.0
recomendacao_resultado_total{application="gamificacao-core",origem="IA"} 4.0
```

Leitura arquitetural:

| Métrica | ASR que ela valida | O que mostrou |
|---|---|---|
| `recomendacao_resultado_total{origem="FALLBACK"}` | Tolerância a falhas (D3) | As 6 chamadas com a IA fora do ar viraram fallback |
| `http_server_requests_seconds_count{status="200",uri="/api/recomendacoes/..."}` | Confiabilidade (D6) | As 10 chamadas responderam 200: nenhum erro chegou ao aluno |
| `iot_eventos_total{status="duplicado"}` | Conectividade intermitente (D5) | 3 entregas do mesmo evento = 1 registro + 2 duplicatas descartadas |
| `ranking_consulta_seconds` | Escalabilidade do Ranking (D1) | Base da latência p95 que dispara o gatilho do ADR-001 |

Métrica técnica × evidência arquitetural: "6 fallbacks" sozinho é número; junto com "10 respostas
200" ele prova a decisão do ADR-003 (a IA cai e o aluno não percebe).

## 2. Gate que bloqueia o deploy

O `Jenkinsfile` do Core roda `./mvnw verify`, que executa os testes (inclusive ArchUnit) e o
`jacoco:check` com mínimo de 90% de linhas no pacote `domain`. Para provar o bloqueio, foi
acrescentado temporariamente um método sem teste no `domain`:

```
[WARNING] Rule violated for package org.example.grupo_7_praticaatdd.domain: lines covered ratio is 0.88, but expected minimum is 0.90
[INFO] BUILD FAILURE
```

O build falhou antes dos stages `Imagem` e `Deploy`. O método foi removido e o `verify` voltou a
passar.

## 3. Como ver no Grafana e no Jenkins

```bash
docker compose up -d --build                                          # Core, IA, RabbitMQ, Mosquitto, Prometheus, Grafana
docker compose -f infra/jenkins/docker-compose.jenkins.yml up -d --build  # Jenkins em http://localhost:8081
# Grafana http://localhost:3000 (admin/admin) > AC2 > "AC2 - Arquitetura (ASRs)"
docker stop gamificacao-ia      # painel "D3 Tolerância a falhas" mostra o pico de FALLBACK
docker stop gamificacao-notificacao   # painel "D4 Recuperabilidade" mostra a fila crescendo
```
