# AC2 - B1 Architecture Lab - Grupo 7

Registro arquitetural vivo da AC2 (Exercício B1 - Architecture Lab). O produto é o MVP de
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

### Papéis por desafio

| Desafio | Mini-lab (código + evidência) | Decisão + ADR | C4 |
|---|---|---|---|
| D1 - Drivers e evolução do Core | Matheus | Rafael | Henrique |
| D2 - Organização interna do Core | Rafael | Henrique | Matheus |
| D3 - Serviço Python e IA | Henrique | Matheus | Rafael |
| D4 - Comunicação e eventos | Rafael | Henrique | Matheus |
| D5 - IoT e MQTT | Henrique | Matheus | Rafael |
| D6 - CI/CD e observabilidade | Matheus | Rafael | Henrique |

## Ponto de partida: o legado da AC1

Este repositório começou como cópia do repositório da AC1
([malcantara-dev/devops-ac1-atdd](https://github.com/malcantara-dev/devops-ac1-atdd)), com todo o
histórico. A tag `ac1-final` marca exatamente o estado entregue na AC1, que é o "antes" de todas as
decisões abaixo. A documentação original da AC1 (User Stories, BDD, TDD e evidências) foi movida
para [docs/ac1/README.md](docs/ac1/README.md).

O que o legado entrega hoje:

- Spring Boot 4.1 (Java 17) organizado em camadas técnicas: `controller`, `service`, `repository`,
  `entity`, `dto` e `config`.
- Um pacote `domain` puro (sem framework) com as regras nascidas do TDD: crédito de 3 cursos ao
  concluir com nota >= 7, curso em andamento não gera crédito e desbloqueio de curso bônus
  consumindo crédito (cenários 1 a 6, cobertura de 100% no `domain`).
- API REST com Swagger, front-end em Vue.js servido pela própria aplicação, H2 para estudo e
  PostgreSQL + pgAdmin via Docker Compose.
- Testes rodando no GitHub Actions a cada push, com relatório do JaCoCo.

---

# Desafio 1 - Drivers e evolução do Core

**Papel: Tech Lead.** Decidir a arquitetura macro do Core antes de decompor o sistema, a partir dos
RFs, RNFs, ASRs e RPCs, comparando alternativas e sustentando a escolha com evidência técnica.

## 1. Leitura do cenário

O MVP da AC1 foi pensado para poucos usuários. No B1 ele cresce de aproximadamente 1.000 para até
**100.000 usuários**. Ranking, cursos, conquistas e recompensas continuam no Core, mas o **Ranking
tende a crescer mais** que as demais capacidades, porque é uma consulta que todo aluno faz com
frequência e que compara todos os alunos entre si.

Hoje o Core é um monólito em camadas técnicas. Cada capacidade de negócio fica espalhada pelas seis
camadas: para entender ou mudar a matrícula, por exemplo, é preciso passar por `controller`, `dto`,
`service`, `entity`, `repository` e `domain`. Isso funcionou bem para o tamanho da AC1, mas é o
ponto que mais pesa quando o produto começa a crescer de forma desigual.

> **Pergunta do Tech Lead:** há evidência suficiente para introduzir distribuição e deploy
> independente agora, ou uma modularização interna resolve o problema com menor custo operacional?

A decisão não parte da tecnologia desejada, e sim dos drivers que realmente justificam mudança
estrutural.

## 2. RFs afetados

| ID | Requisito funcional | Situação no legado | Impacto no desafio |
|---|---|---|---|
| RF01 | Ranking de alunos | Não existe na AC1 | Capacidade nova e com carga que tende a crescer mais; precisa nascer com fronteira clara e ser mensurável. |
| RF02 | Conquistas / gamificação | Parcial: créditos por conclusão e desbloqueio bônus (cenários 1, 2, 5 e 6). Premium após 12 cursos e moedas ainda não existem | As regras de créditos, Premium, moedas e recompensas continuam evoluindo e precisam de fronteiras claras. |
| - | Funcionalidades existentes: matrícula, conclusão, nota parcial, desbloqueio | Implementadas em camadas | Precisam continuar funcionando sem mudança de comportamento durante a evolução. |

## 3. RNFs e seleção dos ASRs

Nem todo RNF é automaticamente um ASR. Foram selecionados apenas os atributos capazes de mudar a
estrutura, a implantação ou a operação do Core (máximo de 3).

| RNF (FURPS+) | ASR? | Justificativa arquitetural |
|---|---|---|
| Manutenibilidade (Supportability) | **SIM** | As capacidades evoluem em ritmos diferentes: Ranking é novo, a gamificação vai ganhar Premium e moedas, a matrícula está estável. Com a organização por camada técnica, qualquer mudança atravessa seis pacotes. Fronteiras por capacidade mudam a **estrutura** do código e reduzem o impacto de cada mudança. |
| Escalabilidade (Performance) | **SIM** | O Ranking tem crescimento de carga diferente das demais capacidades e pode exigir estratégia própria no futuro (cache, réplica de leitura ou extração). Isso pode mudar a **implantação**, então a fronteira do Ranking precisa existir desde já. |
| Complexidade operacional (+ restrição de equipe) | **SIM** | Uma equipe de 6 pessoas teria de absorver vários deploys, rede, contratos, observabilidade e falhas distribuídas se serviços fossem criados agora. Isso muda a **operação** e é o principal freio à distribuição. |
| Desempenho (Performance) | NÃO, neste momento | É relevante, mas a AC1 nunca mediu latência nem throughput e o cenário não traz nenhuma medição que prove gargalo arquitetural. Sem medição, não há como ele dirigir a decisão. |
| Testabilidade (Supportability) | NÃO como driver macro | É importante e vai melhorar com a modularização (testes por capacidade), mas sozinha não determina a decomposição. O `domain` já tem 100% de cobertura; o que falta são testes das camadas de aplicação, o que não exige mudar a arquitetura macro. |

## 4. RPCs - restrições e premissas

| RPC | Consequência para a decisão |
|---|---|
| Core atual em Java/Spring Boot (Spring Boot 4.1, Java 17, build e CI com JDK 21) | A evolução deve preservar o investimento e o legado existentes. |
| Equipe de 6 desenvolvedores | A solução deve manter a complexidade operacional proporcional à capacidade da equipe. |
| Banco relacional existente (PostgreSQL 16, com H2 para estudo) | Não há justificativa, neste desafio, para trocar a tecnologia de dados apenas por modernização. |
| Legado da AC1: `domain` puro coberto 100% por TDD (cenários 1 a 6) | A solução deve evoluir de forma incremental, sem reescrita integral e sem quebrar os testes que provam as regras de negócio. |
| Um único Docker Compose (app + PostgreSQL + pgAdmin) e um pipeline no GitHub Actions | Qualquer novo processo implantável aumenta diretamente o custo de build, deploy e operação. |

## 5. Alternativas e trade-offs

| Alternativa | Benefícios | Custos / riscos | Avaliação |
|---|---|---|---|
| A - Monólito em Camadas (manter a AC1) | Nenhum custo de mudança; baixa complexidade operacional; continuidade imediata. | A organização por camada técnica espalha cada capacidade em seis pacotes e tende a aumentar o acoplamento entre capacidades conforme o produto cresce. O Ranking nasceria misturado às demais. | Possível, mas pouco preparada para a evolução prevista. |
| B - Monólito Modular | Fronteiras por capacidade; continua um único jar, um container, um banco e um pipeline; facilita testes, manutenção e futura extração seletiva. | Reorganização de pacotes; ainda compartilha processo de execução, banco e deploy; fronteiras precisam ser verificadas para não virarem só pastas. | Melhor equilíbrio entre os ASRs e as RPCs. |
| C - Serviços independentes (ex.: Ranking como microsserviço) | Permite escala e deploy independentes por capacidade. | Rede, contratos entre serviços, observabilidade distribuída, tolerância a falhas, consistência de dados entre bancos e maior custo operacional para a equipe. | Prematura sem evidência de necessidade de independência. |

## 6. Decisão do Tech Lead

> **DECISÃO: evoluir o Core para um MONÓLITO MODULAR, de forma incremental.**

O Core continua sendo uma única aplicação Spring Boot, mas passa a ser organizado por capacidade de
negócio. A evolução começa por duas capacidades:

- **Matrícula**, funcionalidade que veio da AC1, é reorganizada como o primeiro módulo
  (`matricula/api`, `matricula/application`, `matricula/internal`), sem mudar comportamento.
- **Ranking (RF01)** já nasce como módulo, com fronteira própria, e é tratado como **candidato
  explícito à extração futura**. Ele não vira microsserviço apenas pela previsão de crescimento.

As demais capacidades (usuário, curso) continuam no formato em camadas e migram quando forem
alteradas. O pacote `domain` da AC1 permanece como núcleo compartilhado, preservando os testes do
TDD.

**Racional:** a modularização atende principalmente manutenibilidade e controle da complexidade
operacional e, ao mesmo tempo, cria uma fronteira adequada para observar o ASR de escalabilidade. A
decomposição em serviço será reavaliada somente se métricas demonstrarem necessidade de escala ou de
deploy independente.

## 7. ADR-001 - Evolução do Core

| Campo | Registro |
|---|---|
| Título | ADR-001 - Evoluir o Core de Monólito em Camadas para Monólito Modular |
| Status | Aceito |
| Contexto | O produto cresce para até 100.000 usuários; o Ranking (RF01) é uma capacidade nova com crescimento de carga diferente; as regras de gamificação (RF02) continuam evoluindo; no legado cada capacidade está espalhada por seis camadas técnicas. |
| ASRs | Manutenibilidade; Escalabilidade; Complexidade operacional. |
| RPCs | Java/Spring Boot; equipe de 6 desenvolvedores; banco relacional PostgreSQL; legado da AC1 com `domain` coberto por TDD; um único Docker Compose. |
| Alternativas | A) Monólito em Camadas; B) Monólito Modular; C) decomposição seletiva em serviços. |
| Decisão | Adotar Monólito Modular de forma incremental: Matrícula vira o primeiro módulo, Ranking nasce módulo e preserva a fronteira para eventual extração futura. |
| Consequências positivas | Melhor separação de responsabilidades; menor acoplamento entre capacidades; evolução incremental sem reescrita; custo operacional igual ao atual e muito menor que o de microsserviços. |
| Trade-offs | Deploy ainda conjunto; processo e banco compartilhados; Ranking ainda não possui escala independente; durante a transição convivem módulos e camadas. |
| Gatilho de revisão | Reavaliar a extração do Ranking quando métricas mostrarem gargalo, necessidade de escala própria ou de deploy independente. |

## 8. C4 - visão de Container

_Em elaboração._

## 9. Mini-lab - evidência arquitetural

A evidência não é apenas um print da aplicação funcionando. O objetivo é testar a hipótese de que a
modularização torna as fronteiras e dependências mais explícitas **sem alterar o comportamento**.

### 9.1 Estrutura antes (tag `ac1-final`)

```
org/example/grupo_7_praticaatdd/
├── controller/  CursoRestController  MatriculaRestController  UsuarioRestController  ApiExceptionHandler
├── service/     CursoService  MatriculaService  UsuarioService
├── repository/  CursoRepository  MatriculaRepository  UsuarioRepository
├── entity/      CursoEntity  MatriculaEntity  UsuarioEntity
├── dto/         CursoRequestDTO  CursoResponseDTO  MatriculaRequestDTO  MatriculaResponseDTO
│                NotaRequestDTO  UsuarioRequestDTO  UsuarioResponseDTO
├── domain/      Assinatura  Curso  Matricula  StatusMatricula  Usuario
└── config/      OpenApiConfig
```

### 9.2 Dependências antes (medidas com `jdeps`)

Coletado sobre as classes compiladas da tag `ac1-final`. Arquivo completo:
[docs/evidencias/desafio-1/antes-dependencias.txt](docs/evidencias/desafio-1/antes-dependencias.txt).

| De → Para | Pares classe → classe |
|---|---|
| controller → dto | 7 |
| controller → service | 3 |
| service → domain | 5 |
| service → entity | 5 |
| service → repository | 5 |
| service → dto | 3 |
| repository → entity | 3 |
| entity → domain | 1 |

O que os números mostram sobre a capacidade **Matrícula**:

- Ela está espalhada em **9 classes distribuídas em 6 pacotes** (`controller`, `dto`, `service`,
  `entity`, `repository`, `domain`).
- O `MatriculaService` depende diretamente de **3 repositórios e 3 entidades**, incluindo os de
  Usuário e Curso. Nada na estrutura impede que qualquer classe acesse a persistência de outra
  capacidade.

### 9.3 Estrutura depois (alvo do mini-lab)

_Em elaboração._

### 9.4 O que comparar

_Em elaboração._

### 9.5 Como reproduzir

```bash
# compilar e medir as dependências
./mvnw -q -DskipTests compile
jdeps -verbose:class -filter:none target/classes

# rodar todos os testes (domínio, API e regras de arquitetura)
./mvnw test
```

## 10. Evidências a versionar

_Em elaboração._

## 11. Conclusão

_Em elaboração._

## 12. Rastreabilidade final

_Em elaboração._

---

# Matriz cumulativa de ASRs

| ASR | D1 | D2 | D3 | D4 | D5 | D6 | Evidência / métrica que valida |
|---|---|---|---|---|---|---|---|
| Manutenibilidade | ✔ | | | | | | Dependências antes/depois (`jdeps`) e regras ArchUnit |
| Escalabilidade | ✔ | | | | | | Fronteira do Ranking; latência do ranking a medir no D6 |
| Complexidade operacional | ✔ | | | | | | Continua 1 container de aplicação, 1 banco e 1 pipeline |

# Desafios seguintes

- Desafio 2 - Organização interna do Core: pendente
- Desafio 3 - Serviço Python e IA: pendente
- Desafio 4 - Comunicação e eventos: pendente
- Desafio 5 - IoT e MQTT: pendente
- Desafio 6 - CI/CD e observabilidade: pendente

# Como executar

Pré-requisitos: JDK 17 ou superior e Docker para o perfil PostgreSQL.

```bash
./mvnw test                                            # testes + relatório JaCoCo em target/site/jacoco
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2   # banco em memória
./mvnw package -DskipTests && docker compose up -d --build   # app + PostgreSQL + pgAdmin
```

- Aplicação e front-end: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- pgAdmin: http://localhost:5050 (`admin@admin.com` / `admin`)

Detalhes de execução, endpoints e evidências da AC1: [docs/ac1/README.md](docs/ac1/README.md).
