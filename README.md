# Sistema de Solicitudes de Reembolso de Gastos Corporativos

Post-contenido 12 de Patrones de Diseño de Software (UDES): proyecto integrador.
Spring Boot 4.1.1, Java 17 o superior (probado con Java 26), Maven 3.9 y H2 en memoria.

> Parte 1 en curso: esta sección se escribió antes de implementar los patrones.

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
