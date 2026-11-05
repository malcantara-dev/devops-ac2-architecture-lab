package org.example.grupo_7_praticaatdd.arquitetura;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Desafio 1 (antes): registra as regras que a arquitetura em camadas da AC1 ja cumpre.
// Elas continuam valendo depois da modularizacao.
@AnalyzeClasses(packages = "org.example.grupo_7_praticaatdd", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaCamadasTest {

    // Repositories e entidades JPA do proprio projeto (ResponseEntity do Spring nao conta).
    private static final DescribedPredicate<JavaClass> PERSISTENCIA_DO_PROJETO =
            DescribedPredicate.describe("repositories ou entidades do projeto", c ->
                    c.getPackageName().startsWith("org.example")
                            && (c.getSimpleName().endsWith("Repository") || c.getSimpleName().endsWith("Entity")));

    // O dominio nasceu do TDD sem framework e deve continuar assim.
    @ArchTest
    static final ArchRule dominioNaoDependeDeFramework = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..");

    // Controller fala com service; nunca acessa repository ou entity direto.
    @ArchTest
    static final ArchRule controllerNaoAcessaPersistencia = noClasses()
            .that().haveSimpleNameEndingWith("RestController")
            .should().dependOnClassesThat(PERSISTENCIA_DO_PROJETO);

    // O dominio nao conhece as entidades JPA.
    @ArchTest
    static final ArchRule dominioNaoConheceEntidades = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Entity");
}
