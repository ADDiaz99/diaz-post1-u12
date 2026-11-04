# Sistema de Solicitudes de Reembolso de Gastos Corporativos

Post-contenido 12 de Patrones de Diseño de Software (UDES): proyecto integrador.
Spring Boot 4.1.1, Java 17 o superior (probado con Java 26), Maven 3.9 y H2 en memoria.

**Contenido**

1. [Diseño Propuesto](#1-diseño-propuesto)
2. [Decisiones de diseño (Parte 1)](#2-decisiones-de-diseño-parte-1)
3. [Pruebas](#3-pruebas)
4. [Cómo ejecutar](#4-cómo-ejecutar)
5. [Ajustes respecto al enunciado](#5-ajustes-respecto-al-enunciado)

---

## 1. Diseño Propuesto

Antes de programar se analizó cada requisito buscando **qué cambia** y **qué va a crecer**. Un
patrón solo se propone donde hay variabilidad real y documentada en la especificación, y cada uno
debe poder justificarse en una oración (test de explicabilidad).

| Requisito | Qué varía (punto de variabilidad) | Patrón | Justificación en una oración |
|---|---|---|---|
| **RF1** Validación en varias etapas | El conjunto de verificaciones: hoy son tres y Finanzas ya anunció más (límite anual acumulado). Cada una puede detener el proceso. | **Chain of Responsibility** | Cada verificación es un eslabón que corta la cadena al fallar o la pasa al siguiente, así que una verificación nueva se agrega como eslabón sin modificar las existentes. |
| **RF2** Cálculo distinto por categoría | La fórmula del monto aprobado y el tope de cada categoría (en viáticos el tope depende de los días). Llegarán categorías nuevas, como "bienestar". | **Strategy** (con un selector que las registra por categoría) | Cada categoría trae su propia fórmula y su propio tope, así que cada una es una estrategia y una categoría nueva es una clase nueva, sin `switch` que editar. |
| **RF3** Notificación a varios interesados | Quién se entera de un cambio de estado y cómo reacciona (empleado: todo; Contabilidad: solo aprobaciones; auditoría: todo). Crecerá con el panel de métricas. | **Observer** | Varios interesados reaccionan de forma distinta al mismo evento y su número crece, así que el servicio avisa a una interfaz común y cada interesado decide qué hacer. |

**Alternativas consideradas antes de elegir**

- **RF1:** una simple lista de validadores recorrida con un `for` también funcionaría. Se prefiere la
  cadena porque el orden de las verificaciones es una decisión de diseño (Decisión 1) que queda
  escrita en un solo lugar, y porque cada eslabón decide si la solicitud continúa.
- **RF2:** un `enum` con un método abstracto por constante (como en la Unidad 11) sirve cuando el
  conjunto de casos es cerrado. Aquí no lo es: Finanzas agregará categorías, y con el `enum` cada
  categoría nueva obligaría a editar el archivo que contiene todas las fórmulas. Además metería
  reglas de negocio de Finanzas dentro del dominio.
- **RF3:** el bus de eventos de Spring (`ApplicationEventPublisher` con `@EventListener`) también
  desacoplaría, pero con tres interesados fijos y conocidos una interfaz basta y deja ver en el
  código quién reacciona a qué.

**El tope por categoría pertenece a la estrategia.** RF1 exige verificar "el tope de la categoría"
y RF2 define ese tope por categoría. Si el validador de monto guardara su propia tabla de topes,
habría dos lugares que editar por cada categoría nueva (un `switch` disfrazado de mapa) y el tope
de viáticos no podría depender de los días. Por eso cada estrategia expone `topeSolicitud(...)` y
el validador se lo pregunta.

**Cómo se componen.** `SolicitudReembolsoService` es el único punto que coordina los tres patrones
y el único que conoce el repositorio; para el controlador funciona como una fachada (Facade). No
se crean clases adicionales para eso.

**Plan de paquetes**

```
com.empresa.reembolsos
├── dominio/            SolicitudReembolso, CategoriaGasto, EstadoSolicitud (sin Spring ni JPA)
├── procesamiento/
│   ├── validacion/     RF1: Chain of Responsibility
│   ├── calculo/        RF2: Strategy + SelectorCalculadora
│   └── notificacion/   RF3: Observer
├── aplicacion/         SolicitudReembolsoService (coordina los patrones)
├── infraestructura/    RepositorioSolicitudesJpa
└── adaptadores/rest/   SolicitudReembolsoController
```

---

## 2. Decisiones de diseño (Parte 1)

### Decisión 1: orden de la cadena de validación

**Orden elegido: documentos → monto → duplicados**, del eslabón más barato al más caro.

| Eslabón | Qué hace | Costo |
|---|---|---|
| `ValidadorDocumentosSoporte` | lee un booleano | constante |
| `ValidadorMontoMaximo` | busca la estrategia en un mapa y compara | constante |
| `ValidadorDuplicados` | recorre las solicitudes pendientes del empleado | crece con los datos |

- **Qué se gana:** las solicitudes que fallan por documentos o por monto nunca llegan a recorrer la
  lista. Y el empleado recibe primero el motivo que él mismo puede corregir (adjuntar el soporte o
  ajustar el monto) en lugar de "ya tienes una solicitud pendiente".
- **Qué se pierde:** si la mayoría de los rechazos fueran duplicados, poner ese eslabón primero los
  detectaría antes. Y hay un límite honesto: el servicio carga las pendientes **antes** de entrar
  a la cadena, así que la consulta a la base de datos se paga siempre. El orden ahorra la
  comparación en memoria, no la consulta. Ahorrar también la consulta exigiría cargarlas de forma
  perezosa; esa alternativa se evaluará en el análisis de impacto de la Parte 2.

El orden queda escrito en un solo lugar, el constructor de `CadenaValidadores`, y lo verifica
`CadenaValidadoresTest.laCadenaSeCortaEnElPrimerEslabonQueFalla`: una solicitud sin documentos y
con monto excesivo se rechaza por documentos.

### Decisión 2: cómo se elige la calculadora de cada categoría

**Elegido: un mapa construido a partir de la `List<CalculadoraReembolso>` que Spring inyecta** con
todos los `@Component` registrados (`SelectorCalculadora`).

- **Con un `switch` sobre `CategoriaGasto`**, cada categoría nueva obliga a editar ese método. Si
  alguien lo olvida, el error aparece en ejecución.
- **Con el mapa**, agregar "bienestar" es crear `CalculadoraBienestar` con `@Component`: Spring la
  incluye en la lista y el selector la registra sola. No cambia ninguna línea del selector, de las
  otras calculadoras ni de los validadores. Lo único existente que se toca es la constante
  `BIENESTAR` del `enum` `CategoriaGasto`, inevitable porque la categoría es un dato del dominio.

La guía dejaba un segundo `switch` escondido: `ValidadorMontoMaximo` tenía su propio
`Map<CategoriaGasto, BigDecimal> TOPES`. Con ese mapa, una categoría nueva sin su tope provocaba un
`NullPointerException`, y el tope fijo de viáticos (150.000) rechazaba cualquier viaje de más de un
día con gastos mayores a 150.000, con lo que la multiplicación por días de `CalculadoraViaje` nunca
tenía efecto. Por eso el tope pasó a la estrategia (`topeSolicitud`).

Consecuencias: el paquete `validacion` depende de `calculo` (los dos dentro de `procesamiento`, lo
que las reglas permiten). Si dos calculadoras declaran la misma categoría, `Collectors.toMap` lanza
una excepción al arrancar, así que el error se ve de inmediato.

---

## 3. Pruebas

| Clase de prueba | Tipo | Pruebas |
|---|---|---|
| `CalculadoraViajeTest`, `CalculadoraRepresentacionTest`, `CalculadoraCapacitacionTest` | Unitaria | 3 + 2 + 2 |
| `SelectorCalculadoraTest` | Unitaria | 2 |
| `ValidadorDocumentosSoporteTest`, `ValidadorMontoMaximoTest`, `ValidadorDuplicadosTest` | Unitaria | 2 + 6 + 4 |
| `CadenaValidadoresTest` | Unitaria | 3 |
| `NotificadorContabilidadTest`, `NotificadorEmpleadoTest` | Unitaria | 2 + 1 |
| `FlujoSolicitudReembolsoIntegracionTest` | Integración `@SpringBootTest` (HTTP + JPA + H2) | 6 |
| **Total** | | **33** |

La prueba de integración recorre el flujo completo por HTTP: crea solicitudes de las tres
categorías y comprueba su monto aprobado, rechaza una sin documentos y un duplicado con un mensaje
claro (400), devuelve 404 para una solicitud inexistente, y verifica en la salida de consola que al
aprobar se enteran los tres interesados y que al rechazar Contabilidad no reacciona.

![Pruebas de la Parte 1](docs/01-pruebas-parte1.png)

---

## 4. Cómo ejecutar

```
mvn clean verify                               # 33 pruebas
mvn spring-boot:run                            # API en http://localhost:8080
```

Crear una solicitud de viáticos de 3 días (en Git Bash):

```
curl -X POST http://localhost:8080/api/solicitudes -H "Content-Type: application/json" -d '{"empleadoId":"emp-001","categoria":"VIAJE","montoSolicitado":400000,"fechaGasto":"2026-10-05","diasViaje":3,"documentosSoporte":true}'
```

Aprobarla (el cuerpo es el estado entre comillas, como texto JSON):

```
curl -X PUT http://localhost:8080/api/solicitudes/1/estado -H "Content-Type: application/json" -d '"APROBADA"'
```

---

## 5. Ajustes respecto al enunciado

| En la guía | Problema | Qué se hizo |
|---|---|---|
| El dominio no importa JPA, pero `RepositorioSolicitudesJpa extends JpaRepository<SolicitudReembolso, Long>` | Sin `@Entity`, Spring Data no arranca ("Not a managed type") | Mapeo en `META-INF/orm.xml`: el dominio sigue limpio y el repositorio funciona |
| Tope de viáticos fijo en 150.000 en `ValidadorMontoMaximo` | Todo viaje con gastos mayores a 150.000 se rechazaba sin importar los días | El tope vive en cada estrategia (`topeSolicitud`) |
| Mapa `TOPES` propio del validador | Una categoría nueva sin tope causaba `NullPointerException` | Igual que el anterior: un solo lugar por categoría |
| `ValidadorDuplicados` comparaba solo `getMonth()` | Octubre de 2025 chocaba con octubre de 2026 | Se compara el periodo con `YearMonth` |
| `SolicitudInvalidaException` sin manejo | El rechazo llegaba como error 500 | `@ExceptionHandler` con `ProblemDetail`: 400 si es inválida, 404 si no existe |
| `crear` guardaba el `id` enviado por el cliente | Un POST con `id` podía sobrescribir otra solicitud | El servicio pone `id` en `null` antes de guardar |
| `cambiarEstado` al mismo estado | Notificaba un "cambio" que no ocurrió | Si el estado no cambia, no se notifica |

