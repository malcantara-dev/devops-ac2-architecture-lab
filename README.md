# AC2 - B1 Architecture Lab - Grupo 7

Repositório da AC2 (Exercício B1 - Architecture Lab). O produto é o MVP de
**Gamificação para Educação Continuada** construído na AC1. No B1 ele passa a receber crescimento
de usuários, IA, eventos, IoT e exigências de operação. Em cada desafio a equipe assume o papel de
Tech Lead e justifica a decisão antes de mexer no código, seguindo sempre a mesma cadeia:

```
RF → RNF → ASR + RPC → alternativas/trade-offs → ADR → C4 → evidência
```

## Equipe

- Matheus de Alcantara
- Rafael Silva Fiamengue
- Henrique Bicudo Paulino

## Registro arquitetural

O registro cumulativo das seis decisões, a matriz de ASRs, os ADRs, os diagramas C4 e a síntese
para a PoC da Avaliação Final estão em [docs/arquitetura/README.md](docs/arquitetura/README.md).
As evidências de cada desafio estão em [docs/evidencias/](docs/evidencias/).

| Desafio | Decisão (ADR) |
|---|---|
| D1 - Drivers e evolução do Core | ADR-001: Monólito Modular incremental |
| D2 - Organização interna do Core | ADR-002: Ports & Adapters na gamificação |
| D3 - Serviço Python e IA | ADR-003: `ia-service` independente com fallback no Core |
| D4 - Comunicação e eventos | ADR-004: conclusão síncrona, notificação assíncrona via RabbitMQ |
| D5 - IoT e MQTT | ADR-005: MQTT com gateway e idempotência no Core |
| D6 - CI/CD e observabilidade | ADR-006: pipeline por componente e métricas ligadas aos ASRs |

# Como executar

Pré-requisitos: JDK 17 ou superior e Docker.

```bash
./mvnw test                                            # testes + relatório JaCoCo em target/site/jacoco
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2   # banco em memória
./mvnw package -DskipTests && docker compose up -d --build   # app e serviços dos desafios
```

Os passos para reproduzir cada mini-lab estão no arquivo de evidência do desafio, em
[docs/evidencias/](docs/evidencias/). Documentação da AC1: [docs/ac1/README.md](docs/ac1/README.md).
