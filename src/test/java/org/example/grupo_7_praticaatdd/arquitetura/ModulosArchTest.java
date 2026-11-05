package org.example.grupo_7_praticaatdd.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Desafio 1 (depois): as fronteiras dos modulos sao verificadas a cada build.
// Se alguem acessar o internal de outro modulo, o teste falha e o CI fica vermelho.
@AnalyzeClasses(packages = "org.example.grupo_7_praticaatdd", importOptions = ImportOption.DoNotIncludeTests.class)
class ModulosArchTest {

    @ArchTest
    static final ArchRule ninguemAcessaInternalDaMatricula = noClasses()
            .that().resideOutsideOfPackage("..matricula..")
            .should().dependOnClassesThat().resideInAPackage("..matricula.internal..");

    @ArchTest
    static final ArchRule ninguemAcessaInternalDoRanking = noClasses()
            .that().resideOutsideOfPackage("..ranking..")
            .should().dependOnClassesThat().resideInAPackage("..ranking.internal..");

    // Ranking e Matricula evoluem em ritmos diferentes: um nao depende do outro.
    @ArchTest
    static final ArchRule rankingNaoDependeDaMatricula = noClasses()
            .that().resideInAPackage("..ranking..")
            .should().dependOnClassesThat().resideInAPackage("..matricula..");

    @ArchTest
    static final ArchRule matriculaNaoDependeDoRanking = noClasses()
            .that().resideInAPackage("..matricula..")
            .should().dependOnClassesThat().resideInAPackage("..ranking..");

    // Os modulos so entram pela api: o legado em camadas nao chama application de modulo.
    @ArchTest
    static final ArchRule legadoNaoUsaApplicationDosModulos = noClasses()
            .that().resideInAnyPackage("..controller..", "..service..", "..repository..")
            .should().dependOnClassesThat().resideInAnyPackage("..matricula.application..", "..ranking.application..");

    // Desafio 2: o caso de uso da gamificacao so conhece a porta, nunca o JPA.
    @ArchTest
    static final ArchRule gamificacaoApplicationNaoConheceJpa = noClasses()
            .that().resideInAPackage("..gamificacao.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "jakarta.persistence..", "org.springframework.data..", "..entity..", "..repository..");

    @ArchTest
    static final ArchRule ninguemAcessaAdapterDaGamificacao = noClasses()
            .that().resideOutsideOfPackage("..gamificacao..")
            .should().dependOnClassesThat().resideInAPackage("..gamificacao.internal..");
}
