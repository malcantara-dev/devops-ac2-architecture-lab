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

No Desafio 1 a decisão é macro. O C4 Container mostra que o Core continua sendo **um único
container** e explicita que sua organização interna passa a ser modular. O detalhamento dos
componentes internos fica para o C4 Component do Desafio 2.

```mermaid
flowchart TB
    aluno["Aluno<br/>[Pessoa]"]
    admin["Administrador do banco<br/>[Pessoa]"]

    subgraph plataforma["Plataforma de Gamificação"]
        spa["Front-end Web<br/>[Vue.js 3, servido pelo Core]<br/>Cursos, matrículas, créditos e ranking"]
        core["Core Gamificação<br/>[Java / Spring Boot]<br/>MONÓLITO MODULAR<br/>matricula · ranking · usuario · curso · domain"]
        db[("Banco Relacional<br/>[PostgreSQL 16]")]
        pgadmin["pgAdmin<br/>[Container de administração]"]
    end

    aluno -- "usa (HTTPS)" --> spa
    spa -- "chama (REST/JSON)" --> core
    core -- "lê e grava (JPA)" --> db
    admin -- "administra" --> pgadmin
    pgadmin -- "SQL" --> db
```

Impacto da decisão: nenhum container novo foi criado. A mudança é interna ao Core e será detalhada
no C4 Component do Desafio 2.

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

### 9.3 Estrutura depois (resultado do mini-lab)

```
org/example/grupo_7_praticaatdd/
├── matricula/
│   ├── api/                    MatriculaRestController  MatriculaRequestDTO  MatriculaResponseDTO  NotaRequestDTO
│   ├── application/            MatriculaService
│   └── internal/persistence/   MatriculaEntity  MatriculaRepository
├── ranking/
│   ├── api/            RankingRestController  RankingItemDTO
│   ├── application/    RankingService
│   └── internal/       RankingConsulta  AlunoPontuado
├── domain/             núcleo da AC1, compartilhado e coberto por TDD
└── controller/ service/ repository/ entity/ dto/   usuario e curso, ainda em camadas
```

Mapa de pacotes antes e depois, gerado pelo mesmo script (`scripts/dependencias.sh`, com `jdeps`):
[antes-pacotes.txt](docs/evidencias/desafio-1/antes-pacotes.txt) ×
[depois-pacotes.txt](docs/evidencias/desafio-1/depois-pacotes.txt) e
[antes-estrutura.txt](docs/evidencias/desafio-1/antes-estrutura.txt) ×
[depois-estrutura.txt](docs/evidencias/desafio-1/depois-estrutura.txt).

Regras de fronteira verificadas a cada build por `ModulosArchTest` (ArchUnit):

- nenhuma classe fora de `matricula` acessa `matricula.internal`;
- nenhuma classe fora de `ranking` acessa `ranking.internal`;
- `ranking` e `matricula` não dependem um do outro;
- o legado em camadas não chama o `application` dos módulos;
- o `domain` não depende de Spring nem de JPA (`ArquiteturaCamadasTest`).

### 9.4 O que comparar (resultado)

| Evidência | Antes | Depois |
|---|---|---|
| Organização | Matrícula em 6 pacotes técnicos (`controller`, `dto`, `service`, `entity`, `repository`, `domain`). | Matrícula em 1 módulo (`matricula/`) + o `domain` da AC1; Ranking em 1 módulo (`ranking/`). |
| Dependências | Só setas entre camadas; não dá para saber de qual capacidade é cada classe. | Cada seta diz qual módulo usa o quê; não existe seta entre `ranking` e `matricula`. |
| Proteção | Nenhuma: qualquer classe usava qualquer repository. | Acessar o `internal` de outro módulo derruba o build: [violacao-simulada.md](docs/evidencias/desafio-1/violacao-simulada.md). |
| Comportamento | 15 testes verdes, sendo 5 de API da matrícula escritos **antes** da mudança: [antes-testes.txt](docs/evidencias/desafio-1/antes-testes.txt). | Os mesmos testes verdes, mais Ranking e 5 regras de fronteira (23 testes): [depois-testes.txt](docs/evidencias/desafio-1/depois-testes.txt). |
| Infraestrutura | 1 jar, 1 container de aplicação, 1 banco. | Igual: nenhum processo novo. |

### 9.5 Como reproduzir

```bash
# compilar e medir as dependências
./mvnw -q -DskipTests compile
jdeps -verbose:class -filter:none target/classes

# rodar todos os testes (domínio, API e regras de arquitetura)
./mvnw test
```

## 10. Evidências a versionar

| Evidência | Situação |
|---|---|
| README arquitetural com RF → RNF → ASR → RPC → alternativas → decisão | Feito (este documento) |
| ADR-001 com decisão e trade-offs | Feito (seção 7) |
| C4 Container atualizado | Feito (seção 8) |
| Estrutura e dependências **antes** | Feito: [antes-dependencias.txt](docs/evidencias/desafio-1/antes-dependencias.txt) |
| Testes MockMvc da matrícula antes da reorganização | Feito: `MatriculaApiTest` (5 testes) |
| Código da modularização da Matrícula e do módulo Ranking (RF01) | Feito: `matricula/` e `ranking/` com `GET /api/ranking` |
| Estrutura e dependências **depois** + regras ArchUnit | Feito: [depois-pacotes.txt](docs/evidencias/desafio-1/depois-pacotes.txt) e `ModulosArchTest` |
| Testes executados após a refatoração, demonstrando preservação do comportamento | Feito: [depois-testes.txt](docs/evidencias/desafio-1/depois-testes.txt) |

## 11. Conclusão

A equipe decidiu evoluir o Core para Monólito Modular, de forma incremental. A medição do "antes"
confirma o problema que motiva a decisão: a capacidade Matrícula está espalhada em seis pacotes
técnicos e o seu service acessa diretamente a persistência de outras capacidades, sem nenhuma
fronteira que impeça esse acoplamento.

A extração do Ranking para um serviço independente não foi adotada neste momento porque o cenário
informa crescimento, mas ainda não fornece evidência de gargalo nem necessidade comprovada de deploy
independente. Essa decisão deverá ser revisitada quando métricas de carga, latência, throughput ou
frequência de deploy justificarem o custo adicional de distribuição. Essas métricas passam a ser
coletadas no Desafio 6.

O mini-lab confirmou a hipótese. A Matrícula foi reorganizada por capacidade e o Ranking nasceu
como módulo, sem comunicação distribuída e sem mudar o comportamento: os 5 testes de API escritos
antes da mudança passaram iguais depois. A fronteira deixou de ser combinação da equipe e passou a
ser verificada pelo build. Fica como dívida conhecida que o módulo `matricula` ainda usa os
repositórios de usuário e curso do legado; eles migram quando forem alterados.

## 12. Rastreabilidade final

| Elemento | Resposta resumida |
|---|---|
| RF | RF01 Ranking (novo) e RF02 Conquistas (créditos da AC1, Premium e moedas a evoluir). |
| RNF | Manutenibilidade, escalabilidade, testabilidade, desempenho e complexidade operacional foram analisados. |
| ASR | Manutenibilidade, escalabilidade e complexidade operacional. |
| RPC | Spring Boot, 6 desenvolvedores, PostgreSQL, legado da AC1 com `domain` coberto por TDD, um único Docker Compose. |
| Alternativas | Monólito em Camadas × Monólito Modular × serviços independentes. |
| ADR | ADR-001: adotar Monólito Modular de forma incremental. |
| C4 | Core permanece um container; a organização interna passa a ser modular. |
| Evidência | Medição de dependências antes/depois com `jdeps`, regras ArchUnit e testes preservando o comportamento. |

**Regra de leitura:** RF/RNF → ASR + RPC → alternativas → ADR → C4 → evidência.

---

# Desafio 2 - Organização interna do Core

**Papel: Tech Lead.** Proteger o domínio contra dependências técnicas, decidindo qual dependência
precisa ser invertida e se o custo estrutural é proporcional ao problema.

## 1. Leitura do cenário

As regras de ranking, moedas, badges e recompensas mudam com frequência. O B1 acrescenta uma regra
nova ao RF02: **ao concluir o 12º curso com aproveitamento, o plano vira Premium e o aluno recebe 3
moedas**. A equipe precisa evitar que domínio, banco, framework e integrações fiquem acoplados.

Ao tentar implementar a regra, o legado mostrou o problema concreto: o `MatriculaService` da AC1
remontava o domínio só com os créditos (`new Assinatura()` + `adicionarCreditos`). O domínio nunca
sabia quantos cursos o aluno já tinha concluído, então a regra do Premium era impossível de aplicar
pela API, mesmo estando correta nos testes de domínio.

## 2. RFs afetados

| ID | Requisito funcional | Impacto no desafio |
|---|---|---|
| RF01 | Ranking | A fórmula de pontuação passa a considerar moedas e deve mudar em um só lugar. |
| RF02 | Conquistas / gamificação | Premium no 12º curso com 3 moedas; as regras devem ser testáveis sem banco. |

## 3. RNFs e seleção dos ASRs

| RNF | FURPS+ | ASR? | Justificativa arquitetural |
|---|---|---|---|
| Modificabilidade | Supportability | **SIM** | A regra muda com frequência; precisa ficar no domínio, isolada de banco e framework. |
| Testabilidade | Supportability | **SIM** | O caso de uso deve funcionar com qualquer persistência, inclusive em memória. |
| Manutenibilidade | Supportability | **SIM** | A tradução entre tabela e domínio deve morar em um único adapter. |
| Facilidade de evolução | Supportability | NÃO como driver separado | É consequência dos três ASRs acima. |

## 4. RPCs - restrições e premissas

| RPC | Consequência para a decisão |
|---|---|
| Core continua Spring Boot | Ports & Adapters dentro do mesmo processo, com injeção do Spring. |
| Regras existentes devem ser reaproveitadas | Os 6 cenários do TDD da AC1 continuam passando sem alteração. |
| Proibido reescrever o produto para aplicar um padrão | O padrão entra só no módulo de gamificação. |

## 5. Alternativas e trade-offs

| Alternativa | Benefícios | Custos / riscos | Avaliação |
|---|---|---|---|
| A - Estrutura atual (service usa JPA direto) | Nenhum código novo. | A regra do Premium não chega ao domínio; testar exige banco. | Rejeitada: não atende o RF02. |
| B - Ports & Adapters (Hexagonal) no módulo de gamificação | Domínio isolado; mesma regra testada com adapter JPA e em memória; mudança localizada. | Uma interface e um adapter a mais. | **Melhor equilíbrio.** |
| C - Clean Architecture em todo o Core | Regras de dependência uniformes. | Reescrever controllers, services e entidades de todas as capacidades. | Rejeitada: viola a RPC. |

## 6. Decisão do Tech Lead

**DECISÃO: aplicar Ports & Adapters só no módulo de gamificação, combinado com o domínio do DDD.**

- **Qual dependência precisa ser invertida?** A do caso de uso para a persistência. A porta
  `AssinaturaRepositorio` pertence ao caso de uso; o adapter JPA depende dela, nunca o contrário.
- **O custo é proporcional?** Sim: uma interface, um adapter JPA e um adapter em memória para os
  testes. O restante do Core não muda.
- **DDD, Hexagonal, Clean e Onion são concorrentes?** Não, são combináveis. Do DDD vem o domínio
  rico e a linguagem do case (Assinatura, Premium, moedas). Do Hexagonal vêm a porta e os adapters.
  Clean e Onion levariam as mesmas regras a todas as camadas, o que seria desproporcional agora.

## 7. ADR-002 - Ports & Adapters na gamificação

| Campo | Registro |
|---|---|
| Título | ADR-002 - Isolar a gamificação com Ports & Adapters |
| Status | Aceito |
| Contexto | Regras de gamificação mudam com frequência; o Premium exige o estado completo da assinatura, que o service da AC1 não levava ao domínio. |
| ASRs | Modificabilidade; Testabilidade; Manutenibilidade. |
| RPCs | Spring Boot; regras da AC1 reaproveitadas; proibido reescrever o produto. |
| Alternativas | A) service com JPA direto; B) Ports & Adapters na gamificação; C) Clean Architecture no Core inteiro. |
| Decisão | Criar a porta `AssinaturaRepositorio` em `gamificacao.application`, implementada por `AssinaturaJpaAdapter` (`gamificacao.internal.jpa`) e por `AssinaturaEmMemoria` nos testes; regras em `PoliticaDeGamificacao` e `PoliticaDePontuacao` no `domain`. |
| Consequências positivas | Regra do Premium funciona de ponta a ponta; o mesmo contrato roda com os dois adapters; trocar a persistência não muda o caso de uso. |
| Trade-offs | Convivem módulos com porta e legado em camadas; o adapter JPA ainda grava na tabela `usuarios` do legado. |
| Gatilho de revisão | Levar a porta a outros módulos quando eles tiverem regras que mudam com frequência ou precisarem trocar a persistência (ex.: ranking em cache). |

## 8. C4 - visão de Component do Core

A decisão é interna ao Core, então o nível afetado é o Component. As setas mostram o sentido das
dependências: a porta pertence ao caso de uso e o adapter JPA depende dela.

```mermaid
flowchart TB
    subgraph core["Container: Core Gamificação [Spring Boot]"]
        matApi["matricula.api<br/>[Component: REST]"]
        matApp["matricula.application<br/>[Component: casos de uso]"]
        rankApi["ranking.api<br/>[Component: REST]"]
        rankApp["ranking.application<br/>[Component]"]
        rankInt["ranking.internal<br/>[Component: consulta]"]
        subgraph gam["módulo gamificacao - Ports & Adapters"]
            gamApp["GamificacaoService<br/>[caso de uso]"]
            porta{{"AssinaturaRepositorio<br/>[porta]"}}
            jpa["AssinaturaJpaAdapter<br/>[adapter JPA]"]
        end
        dom["domain<br/>Assinatura · PoliticaDeGamificacao · PoliticaDePontuacao"]
    end
    mem["AssinaturaEmMemoria<br/>[adapter de teste]"]
    db[("PostgreSQL")]

    matApi --> matApp --> gamApp
    gamApp --> porta
    gamApp --> dom
    jpa -. implementa .-> porta
    mem -. implementa .-> porta
    jpa --> db
    rankApi --> rankApp --> rankInt --> db
    rankApp --> dom
```

## 9. Mini-lab - evidência arquitetural

**Hipótese:** com a regra no domínio e a persistência atrás de uma porta, o mesmo caso de uso
funciona com qualquer adapter e a regra do Premium passa a valer de ponta a ponta.

| Evidência | Resultado |
|---|---|
| TDD do RF02 (cenários 7 e 8) | [RED](docs/evidencias/desafio-2/tdd/01-red.md) → [GREEN](docs/evidencias/desafio-2/tdd/02-green.md) → [BLUE](docs/evidencias/desafio-2/tdd/03-blue.md), com 100% de cobertura no `domain` ([JaCoCo](docs/evidencias/desafio-2/tdd/jacoco-domain.md)). |
| Troca de adapter | Os mesmos 4 testes do contrato passam com o adapter em memória (0,25 s, sem Spring) e com o JPA (21 s, H2): [contrato-dois-adapters.md](docs/evidencias/desafio-2/contrato-dois-adapters.md). |
| Dependências do caso de uso | `gamificacao.application -> domain` é a única dependência; o JPA só aparece em `gamificacao.internal.jpa`: [depois-pacotes.txt](docs/evidencias/desafio-2/depois-pacotes.txt). |
| Regra de ponta a ponta | `GamificacaoApiTest`: 12 cursos concluídos pela API resultam em plano `PREMIUM` e 3 moedas. |
| Proteção | `ModulosArchTest` derruba o build se `gamificacao.application` importar JPA, entidade ou repository. |

## 10. Evidências a versionar

| Evidência | Situação |
|---|---|
| Regra de domínio isolada (Premium e pontuação) | Feito: `PoliticaDeGamificacao`, `PoliticaDePontuacao` |
| Troca de adapter mantendo os testes do domínio | Feito: `AssinaturaRepositorioContrato` com dois adapters |
| C4 Component | Feito (seção 8) |
| Regras ArchUnit da porta | Feito: `ModulosArchTest` |

## 11. Conclusão

A dependência que precisava ser invertida era a do caso de uso para a persistência, e a inversão
custou uma interface e um adapter. Com ela, a regra do Premium, impossível no desenho da AC1, passou
a funcionar pela API, e o domínio ficou testável em milissegundos. O restante do Core continua como
estava, respeitando a RPC de não reescrever o produto.

## 12. Rastreabilidade final

| Elemento | Resposta resumida |
|---|---|
| RF | RF01 Ranking (pontuação com moedas) e RF02 Conquistas (Premium no 12º curso). |
| RNF | Modificabilidade, testabilidade, manutenibilidade e facilidade de evolução. |
| ASR | Modificabilidade, testabilidade e manutenibilidade. |
| RPC | Spring Boot; regras da AC1 reaproveitadas; sem reescrita. |
| Alternativas | Estrutura atual × Ports & Adapters na gamificação × Clean no Core inteiro. |
| ADR | ADR-002: Ports & Adapters na gamificação. |
| C4 | Component do Core com porta e adapters. |
| Evidência | TDD com 100% no domínio, contrato verde nos dois adapters, ArchUnit e teste de API do Premium. |

---

# Matriz cumulativa de ASRs

| ASR | D1 | D2 | D3 | D4 | D5 | D6 | Evidência / métrica que valida |
|---|---|---|---|---|---|---|---|
| Manutenibilidade | ✔ | ✔ | | | | | Dependências antes/depois (`jdeps`) e regras ArchUnit |
| Escalabilidade | ✔ | | | | | | Fronteira do Ranking; latência do ranking a medir no D6 |
| Complexidade operacional | ✔ | | | | | | Continua 1 container de aplicação, 1 banco e 1 pipeline |
| Modificabilidade | | ✔ | | | | | Regras só no `domain`; ArchUnit proíbe JPA no caso de uso |
| Testabilidade | | ✔ | | | | | Mesmo contrato verde com adapter em memória e JPA |

# Desafios seguintes

- Desafio 2 - Organização interna do Core: concluído
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
