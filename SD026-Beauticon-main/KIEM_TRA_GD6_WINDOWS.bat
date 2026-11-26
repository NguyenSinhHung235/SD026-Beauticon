@echo off
setlocal
cd /d "%~dp0"
echo ==============================================
echo BeautIcon GD6 - kiem tra ma nguon va logic
echo ==============================================
where py >nul 2>&1
if not errorlevel 1 (
  py -3 scripts\verify_project.py
  if errorlevel 1 exit /b 1
) else (
  echo [SKIP] Khong tim thay Python. Bo qua kiem tra route/HTML.
)
where javac >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Khong tim thay JDK 17+ va lenh javac.
  exit /b 1
)
set "TEST_DIR=%TEMP%\BeautIcon_GD6_%RANDOM%"
mkdir "%TEST_DIR%"
javac --release 17 -encoding UTF-8 -d "%TEST_DIR%" src\main\java\com\example\vongtay\orders\OrderWorkflow.java src\main\java\com\example\vongtay\sales\SaleSupport.java src\main\java\com\example\vongtay\people\PeopleValidation.java scripts\JavaSmoke.java
if errorlevel 1 (
  rd /s /q "%TEST_DIR%"
  exit /b 1
)
java -cp "%TEST_DIR%" JavaSmoke
set "EXIT_CODE=%ERRORLEVEL%"
rd /s /q "%TEST_DIR%"
if not "%EXIT_CODE%"=="0" exit /b %EXIT_CODE%
echo [PASS] Kiem tra GD6 xong. Maven/SQL Server can kiem thu rieng.
exit /b 0
