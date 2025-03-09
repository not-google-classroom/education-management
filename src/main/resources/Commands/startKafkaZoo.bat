@echo off
setlocal

REM Set Kafka installation directory (Modify if needed)
set KAFKA_DIR=C:\kafka

REM Change to Kafka directory
cd /d %KAFKA_DIR%

echo Checking if Zookeeper is already running...
netstat -an | find "2181" >nul
if %ERRORLEVEL% == 0 (
    echo Zookeeper is already running.
) else (
    echo Starting Zookeeper...
    start cmd /c bin\windows\zookeeper-server-start.bat config\zookeeper.properties
    timeout /t 10 >nul
)

echo Checking if Kafka is already running...
netstat -an | find "9092" >nul
if %ERRORLEVEL% == 0 (
    echo Kafka is already running.
) else (
    echo Starting Kafka...
    start cmd /c bin\windows\kafka-server-start.bat config\server.properties
)

echo Kafka and Zookeeper started successfully!