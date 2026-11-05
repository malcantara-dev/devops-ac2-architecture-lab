# Mesmo contrato, dois adapters

Comando: ./mvnw test -Dtest='Assinatura*Test'

Os 4 testes de AssinaturaRepositorioContrato rodam sem nenhuma alteracao contra o adapter em
memoria (sem Spring e sem banco) e contra o adapter JPA (H2 via @DataJpaTest).

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.247 s -- in org.example.grupo_7_praticaatdd.gamificacao.AssinaturaEmMemoriaTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 21.31 s -- in org.example.grupo_7_praticaatdd.gamificacao.AssinaturaJpaAdapterTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
