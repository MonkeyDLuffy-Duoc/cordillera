@echo off
title GRUPO CORDILLERA - Lanzador Completo de Microservicios
color 0B
cls

echo =====================================================================
echo    GRUPO CORDILLERA - PLATAFORMA INTELIGENTE DE MONITOREO DE KPIS
echo =====================================================================
echo  Este script levantara toda la infraestructura, microservicios y el
echo  frontend en ventanas independientes de forma ordenada.
echo.
echo  REQUISITO: Asegurate de tener MySQL corriendo en el puerto 3306.
echo =====================================================================
echo.

echo [1/5] Iniciando Servidor de Descubrimiento (Eureka Server)...
start "EUREKA-SERVER [Puerto 8761]" cmd /k "cd infraestructura\eureka-server && mvn spring-boot:run"
echo Esperando 12 segundos a que Eureka inicialice...
timeout /t 12 /nobreak > nul

echo [2/5] Iniciando Servidor de Monitoreo (Spring Boot Admin)...
start "ADMIN-SERVER [Puerto 8000]" cmd /k "cd infraestructura\admin-server && mvn spring-boot:run"
echo Esperando 8 segundos a que Admin Server inicialice...
timeout /t 8 /nobreak > nul

echo [3/5] Iniciando Microservicios de Negocio (Areas, KPIs y Metas)...
start "SERVICE-AREAS [Puerto 8081]" cmd /k "cd microservices\service-areas && mvn spring-boot:run"
start "SERVICE-KPIS [Puerto 8082]" cmd /k "cd microservices\service-kpis && mvn spring-boot:run"
start "SERVICE-METAS [Puerto 8083]" cmd /k "cd microservices\service-metas && mvn spring-boot:run"
echo Esperando 10 segundos a que los microservicios se registren en Eureka...
timeout /t 10 /nobreak > nul

echo [4/5] Iniciando BFF Gateway (Seguridad JWT y Orquestacion)...
start "BFF-GATEWAY [Puerto 8080]" cmd /k "cd bff-gateway && mvn spring-boot:run"
echo Esperando 6 segundos a que BFF Gateway este en linea...
timeout /t 6 /nobreak > nul

echo [5/5] Iniciando Frontend React (Azia Admin Dashboard)...
start "FRONTEND-REACT [Puerto 3000]" cmd /k "cd frontend && npm start"

echo.
echo =====================================================================
echo             TODOS LOS SERVICIOS HAN SIDO LANZADOS
echo =====================================================================
echo  - Eureka Server:      http://localhost:8761
echo  - Admin Server:       http://localhost:8000
echo  - BFF Gateway:        http://localhost:8080
echo  - Frontend React:     http://localhost:3000
echo.
echo  Puedes cerrar este lanzador. Las ventanas de comandos secundarias
echo  mantendran los servicios corriendo.
echo =====================================================================
pause
