# Informe Técnico: Análisis de Patrones y Arquetipos de Diseño
**Proyecto:** Plataforma Inteligente de Monitoreo de KPIs (Grupo Cordillera)  
**Autor:** MonkeyDLuffy-Duoc  
**Fecha:** Junio 2026  

---

## 1. Introducción
El presente documento tiene como objetivo analizar, justificar y detallar la selección de arquetipos arquitectónicos y patrones de diseño implementados en la plataforma de monitoreo de KPIs de **Grupo Cordillera**. La arquitectura del sistema ha sido diseñada bajo estándares corporativos modernos para garantizar la **alta disponibilidad**, la **escalabilidad horizontal**, el **bajo acoplamiento** y la **tolerancia a fallos**.

---

## 2. Arquetipos Arquitectónicos Seleccionados

### A. Arquitectura de Microservicios (Database per Service)
El sistema está diseñado bajo un arquetipo de microservicios, donde las responsabilidades funcionales del negocio se dividen en componentes independientes, autónomos y autocontenidos.

*   **Justificación de Uso**: 
    *   **Bajo acoplamiento (Loose Coupling)**: Cada microservicio (`service-areas`, `service-kpis`, `service-metas`) posee lógica de negocio aislada. Un cambio en las reglas de cálculo de KPIs no afecta el registro de áreas y equipos.
    *   **Base de datos por servicio (Database per Service)**: Cada microservicio gestiona su propia base de datos física en MySQL (`areas_db`, `kpis_db`, `metas_db`). Esto garantiza que los microservicios no compartan recursos de base de datos a nivel físico, previniendo el antipatrón de "Base de datos compartida" que genera acoplamiento de esquemas.
    *   **Escalabilidad independiente**: Si el microservicio de KPIs sufre una alta demanda debido a procesos de ingesta de datos transaccionales, se pueden instanciar múltiples réplicas de `service-kpis` en la nube sin necesidad de duplicar el consumo de memoria de los otros microservicios.

### B. Patrón BFF (Backend-For-Frontend) / API Gateway
Se implementó un microservicio dedicado (`bff-gateway`) que actúa como la fachada y punto de entrada único para la aplicación cliente de React.

*   **Justificación de Uso**:
    *   **Orquestación y Consolidación**: El cliente (React) requiere pintar un dashboard consolidado. En lugar de hacer llamadas múltiples desde el navegador a cada microservicio (lo que saturaría el ancho de banda del cliente), el BFF consulta en paralelo a los microservicios backend, realiza los cruces aritméticos y entrega un JSON unificado.
    *   **Seguridad Centralizada (JWT y RBAC)**: La validación del token JWT de sesión y el control de accesos basados en roles (Role-Based Access Control) se resuelven en el BFF. Los microservicios internos quedan protegidos dentro de la red interna de Docker (`cordillera-net`), reduciendo la superficie de ataque.
    *   **Resolución de CORS**: Evita la necesidad de configurar reglas complejas de Cross-Origin Resource Sharing en cada microservicio independiente, centralizando el tráfico en el puerto `8080`.

---

## 3. Patrones de Diseño de Software

### A. Patrón Creacional: Factory Method
Implementado en la lógica de fabricación de KPIs en el microservicio `service-kpis` a través de la clase `KpiFactory` y la jerarquía polimórfica de la clase base `KPI`.

*   **Detalle de Implementación**: 
    *   El método `createKpi(tipo, nombre, descripcion, unidadMedida)` decide en tiempo de ejecución qué subclase concreta instanciar (`FinancieroKPI`, `CumplimientoKPI`, o `OperacionalKPI`) basándose en el parámetro de tipo.
*   **Justificación de Uso**:
    *   **Polimorfismo y Validación Específica**: Cada tipo de KPI tiene reglas de negocio y restricciones físicas particulares (ej: un KPI de tipo `CUMPLIMIENTO` se valida estrictamente en un rango de `0.0` a `100.0`, mientras que uno `FINANCIERO` soporta valores negativos o monetarios). El Factory Method encapsula la creación de estas clases polimórficas.
    *   **Principio de Abierto/Cerrado (Open/Closed)**: Si el negocio requiere incorporar un nuevo tipo de indicador (ej. *KPI Cualitativo*), solo se debe extender la clase `KPI` y agregar el caso en la fábrica, sin modificar los controladores o controladores de persistencia existentes.

### B. Patrón Estructural: Proxy (Puerta de Enlace Seguro)
Implementado en el `BffController` para canalizar peticiones de escritura y lectura hacia microservicios específicos.

*   **Justificación de Uso**:
    *   Cuando el frontend envía una petición para crear una meta (`POST /api/bff/metas`), el BFF actúa como un Proxy inteligente: valida el token, verifica que el rol del usuario posea los alcances necesarios (ej. `ADMIN` o `JEFE_AREA`) y redirige de forma transparente la solicitud hacia el endpoint de `service-metas` utilizando el balanceador de carga dinámico de Eureka.

### C. Patrón de Resiliencia: Circuit Breaker (Disyuntor) y Fallback
Implementado con **Resilience4j** en la comunicación inter-servicios de `service-metas` hacia `service-kpis`, y mediante capturas con fallbacks controlados en el `BffController` en `/api/bff/dashboard`.

*   **Justificación de Uso**:
    *   **Prevención de Fallas en Cascada**: Si `service-kpis` experimenta latencia o caída física, el disyuntor en `service-metas` pasa a estado *Abierto* tras acumular un umbral de fallos, interrumpiendo las llamadas directas y evitando que los hilos de ejecución de la aplicación queden bloqueados en esperas de red.
    *   **Degradación Gráfica Degradada (Graceful Degradation)**: En el BFF, al capturar de forma individual las excepciones de conexión de cada microservicio, se inyectan listas vacías como alternativa (fallback). Esto permite que si la base de datos de metas o el microservicio de KPIs están apagados, el frontend renderice el resto del dashboard (ej. áreas y equipos) en lugar de colapsar la aplicación con un error de servidor general `500`.

---

## 4. Conclusión
La combinación de la arquitectura de microservicios con patrones específicos de diseño creacional y de resiliencia dota a la plataforma de **Grupo Cordillera** de características empresariales de primer nivel. El sistema no solo separa la lógica de datos de forma limpia a través de contenedores aislados de MySQL, sino que asegura que la experiencia del usuario (UI) sea tolerante a fallos lógicos distribuidos, reflejando las mejores prácticas vigentes de ingeniería de software.
