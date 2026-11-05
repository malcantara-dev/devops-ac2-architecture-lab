# Violacao simulada de fronteira (depois)

Foi criada temporariamente a classe controller/AtalhoProibido com um campo do tipo
matricula.internal.persistence.MatriculaRepository, ou seja, o legado acessando o internal
do modulo Matricula. Comando: ./mvnw test -Dtest=ModulosArchTest

```
[ERROR] Tests run: 5, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 9.143 s <<< FAILURE! -- in org.example.grupo_7_praticaatdd.arquitetura.ModulosArchTest
Architecture Violation [Priority: MEDIUM] - Rule 'no classes that reside outside of package '..matricula..' should depend on classes that reside in a package '..matricula.internal..'' was violated (1 times):
Field <org.example.grupo_7_praticaatdd.controller.AtalhoProibido.repositorio> has type <org.example.grupo_7_praticaatdd.matricula.internal.persistence.MatriculaRepository> in (AtalhoProibido.java:0)
[ERROR] Tests run: 5, Failures: 1, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
```

A classe foi removida em seguida e o build voltou a ficar verde (ver depois-testes.txt).
