# 📊 Plataforma Inteligente de Monitoreo de KPIs (Grupo Cordillera)

¡Bienvenido a la plataforma empresarial de observabilidad y monitoreo de objetivos de **Grupo Cordillera**! 

Este proyecto implementa una arquitectura moderna de **microservicios distribuidos** con base de datos independiente por servicio (**MySQL**), un orquestador centralizado (**BFF Gateway**) con seguridad basada en roles (JWT/RBAC), tolerancia extrema a fallos mediante **Resilience4j Circuit Breakers**, observabilidad avanzada con un **Índice de Errores** oficial, y una interfaz de usuario interactiva y fluida construida en **React** y servida sobre **Nginx**.

---

## 🛠️ Requisitos del Sistema (Qué necesitas instalar)

Para compilar y levantar la plataforma por primera vez, asegúrate de tener instalado en tu máquina:

*   **Git**: Para control de versiones y gestión de ramas.
*   **Java JDK 17 o 21**: Necesario para ejecutar el backend de Spring Boot.
*   **Apache Maven**: Para la gestión de dependencias y compilación del backend.
*   **Node.js (v18 o superior)** y **npm**: Para la ejecución y construcción del frontend de React.
*   **Docker** y **Docker Compose**: Requisito principal para el despliegue multi-contenedor unificado.
*   **MySQL 8.0** *(Opcional)*: Solo necesario si deseas levantar la base de datos de forma local y nativa en lugar de utilizar los contenedores de Docker.

---

## 🚀 Guía de Lanzamiento Paso a Paso

Tienes **dos modalidades independientes** para levantar y probar el proyecto completo:

### 🐳 Opción A: Despliegue Dockerizado (Recomendado - Profesional)
Esta modalidad compila todo el proyecto y levanta **10 contenedores en simultáneo** aislados en su propia red puente (`cordillera-net`), respetando el patrón *Database per Service*:

1.  **Abre una terminal** en la carpeta raíz del proyecto.
2.  **Compila y empaqueta el backend** de todos los microservicios mediante Maven:
    ```bash
    mvn clean package -DskipTests
    ```
3.  **Construye y levanta el ecosistema multi-contenedor**:
    ```bash
    docker-compose up --build -d
    ```
4.  **¡Listo!** Abre tu navegador en **`http://localhost:3000`** para ingresar a la aplicación web.

---

### 💻 Opción B: Despliegue Local (Mediante Scripts de un Clic)
Esta modalidad levanta los servicios nativos sobre tu sistema operativo. Es ideal para depuración en vivo:

1.  **Inicia tu servidor MySQL local** en el puerto estándar **`3306`** de tu máquina. *(Asegúrate de que el usuario sea `root` y la contraseña esté vacía, o edita los archivos `application.yml` de los microservicios en `/src/main/resources`).*
2.  **Abre la carpeta raíz** del proyecto en tu Explorador de Archivos de Windows.
3.  Haz doble clic en el archivo automatizado **`run_all.bat`**.
4.  El script abrirá automáticamente ventanas independientes para cada servidor en el orden y con los tiempos de espera óptimos:
    *   **Ventana 1**: Eureka Discovery Server (Puerto 8761)
    *   **Ventana 2**: Spring Boot Admin Server (Puerto 8000)
    *   **Ventana 3**: Microservicio Areas & Equipos (Puerto 8081)
    *   **Ventana 4**: Microservicio KPIs (Puerto 8082)
    *   **Ventana 5**: Microservicio Metas (Puerto 8083)
    *   **Ventana 6**: BFF Gateway (Puerto 8080)
    *   **Ventana 7**: React Frontend (Puerto 3000)

---

## 📡 Puertos y Direcciones Útiles de la Plataforma

| Componente | Dirección URL | Descripción |
| :--- | :--- | :--- |
| **Frontend Web (React/Nginx)** | 💻 **`http://localhost:3000`** | **Interfaz del Cliente**. Panel dinámico de monitoreo e ingreso por roles. |
| **Spring Boot Admin** | 📊 **`http://localhost:8000`** | **Dashboard de Observabilidad**. Muestra telemetría, salud de servicios y logs en vivo. |
| **Eureka Server** | 📡 **`http://localhost:8761`** | **Servidor de Descubrimiento**. Panel con el registro de las instancias de microservicios. |
| **BFF Gateway (API)** | 🔌 **`http://localhost:8080`** | **Punto de Entrada Único**. Orquesta las llamadas seguras a los servicios internos. |

### Redirección de Puertos MySQL (Solo en Docker)
Si usas Docker, cada base de datos MySQL corre de forma aislada. Si deseas conectarte con un cliente de escritorio (como DBeaver), utiliza:
*   **`areas_db`**: Puerto **`33061`** (Usuario: `root` / Sin contraseña)
*   **`kpis_db`**: Puerto **`33062`** (Usuario: `root` / Sin contraseña)
*   **`metas_db`**: Puerto **`33063`** (Usuario: `root` / Sin contraseña)

---

## 🔑 Credenciales y Simulación de Roles (RBAC)

En la pantalla de Login, puedes usar los botones de **Acceso Rápido** de un clic o ingresar manualmente con los siguientes usuarios de demostración para evaluar el control de acceso:

*   **Administrador**: `admin` / `admin123` (Acceso total, CRUD de metas y panel de edición de usuarios MySQL).
*   **Gerente**: `gerente` / `gerente123` (Lectura global del dashboard, sin permisos de escritura).
*   **Jefe de Ventas**: `jefe.ventas` / `jefe123` (Filtrado de datos exclusivo al área de Ventas, CRUD de metas).
*   **Colaborador Ventas**: `juan.ventas` / `colab123` (Vista restringida únicamente a las métricas de su equipo de trabajo).

---

## 🛡️ Demostración de Resiliencia, Fallback y Observabilidad

Para demostrar la arquitectura de grado corporativo implementada ante tus evaluadores, sigue estos escenarios prácticos de prueba:

### Escenario 1: Tolerancia a Fallos del BFF (Aislamiento de Caídas)
1.  Levanta la plataforma completa (en Docker o de forma Local).
2.  Inicia sesión como administrador y ve al dashboard general.
3.  **Simula la caída de un servicio**: Detén o apaga el microservicio de KPIs (`service-kpis` en el puerto `8082` o contenedor respectivo).
4.  **Resultado Resiliente**: En lugar de colapsar la pantalla completa con un error de red o arrojar un error HTTP `500` generalizado en el BFF Gateway, **el dashboard cargará exitosamente de forma degradada**. Mostrará la información disponible e inyectará listas vacías para KPIs, manteniendo la estabilidad operativa del cliente.
5.  Revisa los logs del **`bff-gateway`** y verás la alerta codificada indicando el aislamiento del fallo.

### Escenario 2: Circuit Breaker en Comunicación Inter-Servicios
1.  Con `service-kpis` aún apagado, intenta crear una nueva meta en el formulario del dashboard.
2.  Al enviar la petición, `service-metas` llamará síncronamente a `service-kpis` para validar el ID del KPI.
3.  **Resultado CB**: El **Circuit Breaker** (Resilience4j) de `service-metas` se activará de inmediato al detectar la caída, ejecutará su método **Fallback** de contingencia técnica, aprobará temporalmente el registro indicándolo en los logs, y evitará una larga espera (timeout) para el usuario final.

---

## 📑 Catálogo Oficial del Índice de Errores

Todos los fallos lógicos, técnicos y de seguridad en el sistema han sido unificados bajo un **Índice de Errores** formalizado. Cada traza de log (`warn` o `error`) se imprime con un código único estructurado como `[ERR-CATEGORÍA-CÓDIGO]` para agilizar la observabilidad distribuida:

| Código de Error | Categoría | Severidad | Descripción del Error | Contingencia o Acción Recomendada |
| :--- | :--- | :--- | :--- | :--- |
| **`[ERR-SEC-401]`** | Seguridad | ALTA | Credenciales incorrectas o usuario inexistente en el login. | Denegar token JWT e incrementar contador de intentos. |
| **`[ERR-SEC-403]`** | Seguridad | MEDIA | Falla en validación de privilegios RBAC (Forbidden). | Retornar código `403` y bloquear operación a nivel de red. |
| **`[ERR-VAL-400]`** | Validación | BAJA | Datos faltantes o duplicados en el RequestBody. | Retornar código `400 Bad Request` indicando la inconsistencia. |
| **`[ERR-BFF-501]`** | Conectividad | CRÍTICA | Fallo al consumir el microservicio `service-areas`. | BFF inyecta lista vacía de Áreas/Equipos (Fallback). |
| **`[ERR-BFF-502]`** | Conectividad | CRÍTICA | Fallo al consumir el microservicio `service-metas`. | BFF inyecta lista vacía de Metas (Fallback). |
| **`[ERR-BFF-503]`** | Conectividad | CRÍTICA | Fallo al consumir el microservicio `service-kpis`. | BFF inyecta lista vacía de KPIs y mediciones (Fallback). |
| **`[ERR-BFF-504]`** | Orquestación | ALTA | Excepción interna de cálculo y consolidación en el BFF. | Retornar código `500` con advertencia controlada. |
| **`[ERR-CB-503]`** | Resiliencia | CRÍTICA | Circuit Breaker activado en `service-metas` por caída de KPIs. | Ejecutar fallback de contingencia y registrar meta degradada. |
| **`[ERR-DB-500]`** | Base de Datos | ALTA | Error de integridad, sintaxis o JPA en MySQL local. | Capturar excepción de persistencia y cancelar la transacción. |

---

## 📈 Estructura del Ecosistema de Ramas (Git Flow)

Este repositorio sigue estrictamente la estrategia de ramificación **Git Flow** para asegurar una integración continua limpia y estructurada. Puedes auditar las siguientes ramas en el repositorio:

*   **`main`**: Código productivo y estable (Lanzamiento v1.0.0 dockerizado).
*   **`desarrollo`**: Rama de integración continua y estabilización.
*   **Ramas `feature/*`**: Ramas de características independientes utilizadas para el desarrollo de cada microservicio, frontend, base de datos MySQL, logs y resiliencia, las cuales fueron fusionadas sin avance rápido (`--no-ff`) para mantener un historial gráfico auditable impecable.
