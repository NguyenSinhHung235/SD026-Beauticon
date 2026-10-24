@echo off
setlocal
cd /d "%~dp0"
echo =========================================
echo BeautIcon Admin GD6 - Spring Boot
 echo =========================================
where java >nul 2>&1
if errorlevel 1 (
  echo KHONG TIM THAY JAVA. Hay cai JDK 17+ (khuyen nghi JDK 21).
  pause
  exit /b 1
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo KHONG TIM THAY MAVEN. Cai Maven hoac chay tu IntelliJ.
  pause
  exit /b 1
)
echo Kiem tra DB_URL, DB_USER, DB_PASSWORD va chay SQL migration truoc.
echo Khi server khoi dong, mo http://localhost:8080/login
echo Neu doi PORT thi doi port trong link.
mvn clean spring-boot:run
if errorlevel 1 echo Co loi khi khoi dong. Xem dong log ngay phia tren.
pause
endlocal
