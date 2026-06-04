# Informe Técnico: Plan de Branching (Git Flow)
**Proyecto:** Plataforma Inteligente de Monitoreo de KPIs (Grupo Cordillera)  
**Autor:** MonkeyDLuffy-Duoc  
**Fecha:** Junio 2026  

---

## 1. Introducción y Justificación del Modelo
Para el control de versiones y el trabajo colaborativo en la plataforma de **Grupo Cordillera**, se adoptó el modelo de ramificación **Git Flow**. Este modelo es el estándar de facto en ingeniería de software para proyectos con ciclos de desarrollo y entrega continuos.

*   **Justificación de Git Flow**:
    *   **Aislamiento del desarrollo**: Evita que los cambios inestables afecten directamente el código productivo.
    *   **Historial auditable**: Permite rastrear con precisión cronológica qué características se añadieron, cuándo y mediante qué rama de características (`feature/`).
    *   **Orden en lanzamientos**: Establece un flujo claro de estabilización del software en una rama de desarrollo antes de dar el paso final a la rama productiva de producción.

---

## 2. Estructura y Roles de las Ramas

La arquitectura de ramas del proyecto está constituida por los siguientes niveles de ciclo de vida:

### A. Ramas Principales (Permanentes)
*   **`main`**: Representa la línea base productiva del proyecto. Cada commit en esta rama corresponde a una versión estable liberada (Release) lista para ser desplegada en entornos de producción.
*   **`desarrollo`**: La rama integradora principal. Recibe de forma continua las fusiones de todas las ramas de características validadas. Sirve como entorno previo de consolidación antes de preparar un lanzamiento.

### B. Ramas de Características (`feature/*`)
Son ramas temporales y de ciclo de vida corto, creadas a partir de `desarrollo`. Cada una se dedica exclusivamente a resolver una tarea o implementar un microservicio específico.
*   **Convención de Nombres**: `feature/nombre-de-la-caracteristica` (ej: `feature/microservicio-kpis`).
*   **Ciclo de Vida**: Nace de `desarrollo` -> Se codifica la característica -> Se compila y verifica -> Se fusiona de vuelta a `desarrollo` -> Se elimina de forma local.

---

## 3. Reglas de Integración y Fusión

Para conservar la visibilidad del flujo de branching y merges ante evaluaciones y auditorías de código, se definieron y aplicaron las siguientes reglas estrictas:

1.  **Fusión sin Avance Rápido (Non-Fast-Forward / `--no-ff`)**:
    *   Al fusionar cualquier rama de características en `desarrollo`, se prohibió la fusión por defecto (*fast-forward*). Se forzó el uso del comando `git merge --no-ff`.
    *   **Por qué**: Esto asegura que Git cree un **commit de fusión explícito** (un nodo de unión) y conserve la bifurcación gráfica en el historial. De lo contrario, Git "alinearía" los commits en una sola línea recta, ocultando el hecho de que se trabajó en una rama separada.
2.  **Eliminación de Ramas Locales Post-Merge**:
    *   Tras confirmar el merge exitoso, la rama `feature/*` local se elimina mediante `git branch -d` para mantener el espacio de desarrollo limpio y libre de ramas muertas.
3.  **Hitos de Lanzamiento (Release merges)**:
    *   Una vez acumuladas las características estables en `desarrollo`, esta rama se fusiona hacia `main` usando también `--no-ff` y etiquetando la versión estable (ej. `v1.0.0`).

---

## 4. Registro Cronológico de Merges en el Repositorio (Crónica del Proyecto)

El historial de Git de la plataforma registra el desarrollo modular del ecosistema a través del siguiente orden de ramas y fusiones:

1.  **`feature/eureka-server`**: Creación del servidor de registro y descubrimiento.
2.  **`feature/admin-server`**: Creación del panel de monitoreo y telemetría.
3.  **`feature/microservicio-areas`**: Construcción del microservicio de Áreas y Equipos con base de datos H2 inicial.
4.  **`feature/microservicio-kpis`**: Implementación de KPIs con el patrón Factory Method.
5.  **`feature/microservicio-metas`**: Implementación del microservicio de Metas e integración síncrona balanceada.
6.  **`feature/api-gateway`**: Estructuración del BFF Gateway y autenticación JWT.
7.  **`feature/frontend`**: Migración de la interfaz en React y gráficos de Chart.js.
8.  **`feature/mysql-db-migration`**: Migración de las bases de datos locales a esquemas independientes en MySQL 3306.
9.  **`feature/bff-metas-proxy`**: Proxying de peticiones y resolución del bloqueo por CORS a nivel de red.
10. **`feature/user-management-and-goals-crud`**: Panel CRUD de gestión de usuarios en base de datos.
11. **`feature/edit-user-and-ui-cleanup`**: Optimización de layouts de interfaz y opciones dinámicas de edición de usuarios.
12. **`feature/circuitbreaker-and-logging`**: Integración de resiliencia inter-servicios con Resilience4j y auditoría SLF4J.
13. **`feature/coded-logs-and-dashboard-resilience`**: Implementación de fallbacks en BFF Gateway y logs unificados con el Índice de Errores.
14. **`feature/docker-orchestration`**: Creación de Dockerfiles y empaquetamiento final multi-contenedor coordinado en Docker Compose.

---

## 5. Conclusión
El uso de Git Flow en conjunto con merges `--no-ff` ha permitido generar un historial de desarrollo altamente estructurado, limpio y profesional. Esta metodología garantiza que el código productivo en `main` permanezca blindado y libre de fallos de compilación, mientras que la rama `desarrollo` refleja la colaboración ordenada y modular del equipo.
