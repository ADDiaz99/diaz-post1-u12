package com.empresa.reembolsos;

import com.empresa.reembolsos.procesamiento.calculo.CalculadoraReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Limites entre paquetes que el sistema si tiene. Ninguna regla supone puertos de
 * persistencia, coherente con ADR-001.
 * <p>
 * Se analiza solo el codigo de produccion: las pruebas viven en los mismos paquetes
 * (por ejemplo CalculadoraViajeTest en procesamiento.calculo) y romperian las reglas 3 y 4.
 */
@AnalyzeClasses(packages = "com.empresa.reembolsos", importOptions = ImportOption.DoNotIncludeTests.class)
public class ReglasArquitectura {

    // Regla 1: procesamiento no depende de persistencia ni de la capa web.
    // Las anotaciones de estereotipo (@Component) si se permiten: ver Decision de diseno 4.
    @ArchTest
    static final ArchRule procesamientoSinPersistenciaNiWeb = noClasses()
            .that().resideInAPackage("..procesamiento..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "..infraestructura..", "..adaptadores..",
                    "org.springframework.data..", "jakarta.persistence..",
                    "org.springframework.web..", "org.springframework.http..")
            .because("los patrones de procesamiento solo deben conocer el dominio");

    // Regla 2: el controlador solo accede al servicio de aplicacion y al dominio.
    // Se incluye su propio paquete porque leer su campo "servicio" ya es un acceso.
    @ArchTest
    static final ArchRule controladorSoloUsaServicio = classes()
            .that().resideInAPackage("..adaptadores.rest..")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                    "..adaptadores.rest..", "..aplicacion..", "..dominio..",
                    "org.springframework.web..", "org.springframework.http..", "java..")
            .because("el controlador no debe saltarse el servicio para llegar al repositorio o a los patrones");

    // Regla 3: las calculadoras implementan CalculadoraReembolso.
    // SelectorCalculadora vive en el mismo paquete y no es una estrategia, por eso se filtra por nombre.
    @ArchTest
    static final ArchRule calculadorasImplementanInterfaz = classes()
            .that().resideInAPackage("..procesamiento.calculo..")
            .and().haveSimpleNameStartingWith("Calculadora")
            .and().areNotInterfaces()
            .should().implement(CalculadoraReembolso.class)
            .because("el SelectorCalculadora solo encuentra las estrategias que implementan la interfaz");

    // Regla 4: los notificadores implementan ObservadorCambioEstado.
    @ArchTest
    static final ArchRule notificadoresImplementanInterfaz = classes()
            .that().resideInAPackage("..procesamiento.notificacion..")
            .and().areNotInterfaces()
            .should().implement(ObservadorCambioEstado.class)
            .because("el servicio solo avisa a los interesados que implementan la interfaz");

    // Regla 5 (adicional): el dominio no depende de ningun framework.
    // Convierte en prueba automatica el checkpoint del Paso 1 que la guia verifica a mano.
    @ArchTest
    static final ArchRule dominioSinFrameworks = noClasses()
            .that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..")
            .because("el mapeo JPA del dominio vive en META-INF/orm.xml, no en anotaciones");
}
