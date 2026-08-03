package it.unisa.sad.playlistmanager.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Regole ArchUnit su layering e accoppiamento.
 *
 * Esegui con: {@code mvn test -Dtest=ArchitectureTest}
 * Se una regola fallisce, ArchUnit indica le classi che violano il layering.
 */
@AnalyzeClasses(
        packages = "it.unisa.sad.playlistmanager",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String MAIN = "Main";
    private static final String DOMAIN = "Domain";
    private static final String APPLICATION = "Application";
    private static final String PERSISTENCE = "Persistence";
    private static final String UI = "UI";
    private static final String BOOTSTRAP = "Bootstrap";

    @ArchTest
    static final ArchRule layeredArchitectureIsRespected =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer(MAIN).definedBy("it.unisa.sad.playlistmanager")
                    .layer(DOMAIN).definedBy("..domain..")
                    .layer(APPLICATION).definedBy("..application..")
                    .layer(PERSISTENCE).definedBy("..persistence..")
                    .layer(UI).definedBy("..ui..")
                    .layer(BOOTSTRAP).definedBy("..bootstrap..")
                    .whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(APPLICATION, PERSISTENCE, UI, BOOTSTRAP, MAIN)
                    .whereLayer(APPLICATION).mayOnlyBeAccessedByLayers(UI, BOOTSTRAP, MAIN)
                    .whereLayer(PERSISTENCE).mayOnlyBeAccessedByLayers(APPLICATION, BOOTSTRAP)
                    .whereLayer(UI).mayOnlyBeAccessedByLayers(BOOTSTRAP, MAIN)
                    .whereLayer(BOOTSTRAP).mayOnlyBeAccessedByLayers(MAIN)
                    .whereLayer(MAIN).mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule domainDoesNotDependOnOtherLayers =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..application..",
                            "..persistence..",
                            "..ui..",
                            "..bootstrap..")
                    .because("Il Domain deve restare indipendente dai dettagli applicativi e di infrastruttura");

    @ArchTest
    static final ArchRule uiDoesNotAccessPersistence =
            noClasses()
                    .that().resideInAPackage("..ui..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..persistence..")
                    .because("La UI non deve conoscere SQLite o i repository");

    @ArchTest
    static final ArchRule uiDoesNotAccessApplicationServices =
            noClasses()
                    .that().resideInAPackage("..ui..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..application.service..")
                    .because("I controller dialogano con la Facade, non con i service");

    @ArchTest
    static final ArchRule persistenceDoesNotAccessUiOrApplication =
            noClasses()
                    .that().resideInAPackage("..persistence..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..ui..", "..application..")
                    .because("La persistenza dipende solo dal Domain e da JDBC/SQLite");

    @ArchTest
    static final ArchRule domainOnlyDependsOnItselfAndJava =
            classes()
                    .that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "..domain..",
                            "java..",
                            "javax..")
                    .because("Il Domain non deve dipendere da framework UI o librerie esterne");

    @ArchTest
    static final ArchRule repositoryInterfacesLiveInPersistence =
            classes()
                    .that().haveSimpleNameEndingWith("Repository")
                    .and().areInterfaces()
                    .should().resideInAPackage("..persistence.repository..")
                    .because("Le astrazioni Repository stanno nel layer Persistence");
}
