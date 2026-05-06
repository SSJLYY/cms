@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ==========================================
REM 资源下载平台 - 一键启动脚本 (Windows)
REM ==========================================
REM 使用方法: start.bat
REM 功能: 检查环境、启动Docker服务、显示访问信息
REM ==========================================

echo ==========================================
echo   资源下载平台 - Docker一键启动
echo ==========================================
echo.

REM 1. 检查Docker是否安装
echo [INFO] 检查Docker环境...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker未安装
    echo 请先安装Docker Desktop: https://www.docker.com/products/docker-desktop
    echo.
    pause
    exit /b 1
)

REM 检查Docker服务是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker服务未运行
    echo 请启动Docker Desktop后重试
    echo.
    pause
    exit /b 1
)

echo [OK] Docker已安装并运行

REM 2. 检查Docker Compose是否安装
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose未安装
    echo.
    pause
    exit /b 1
)

echo [OK] Docker Compose已安装
echo.

REM 3. 检查端口占用
echo [INFO] 检查端口占用情况...
set PORT_OK=1

netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARN] 端口 3306 ^(MySQL^) 已被占用
    set PORT_OK=0
)

netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARN] 端口 6379 ^(Redis^) 已被占用
    set PORT_OK=0
)

netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARN] 端口 8080 ^(客户前台^) 已被占用
    set PORT_OK=0
)

netstat -ano | findstr ":8081" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARN] 端口 8081 ^(管理后台^) 已被占用
    set PORT_OK=0
)

netstat -ano | findstr ":9090" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
    echo [WARN] 端口 9090 ^(后端API^) 已被占用
    set PORT_OK=0
)

if !PORT_OK! equ 1 (
    echo [OK] 所有端口可用
) else (
    echo.
    echo [WARN] 部分端口已被占用，可能导致服务启动失败
    set /p CONTINUE="是否继续启动? (Y/N): "
    if /i not "!CONTINUE!"=="Y" (
        echo 已取消启动
        pause
        exit /b 0
    )
)

echo.
echo [INFO] 开始启动服务...
echo.

REM 4. 检查docker-compose.yml是否存在
if not exist "docker-compose.yml" (
    echo [ERROR] 找不到 docker-compose.yml 文件
    echo 请确保在项目根目录下运行此脚本
    echo.
    pause
    exit /b 1
)

REM 5. 启动Docker Compose
echo 正在启动容器...
docker-compose up -d
if errorlevel 1 (
    echo [ERROR] 容器启动失败
    echo 请查看错误信息并重试
    echo.
    pause
    exit /b 1
)

echo [OK] 容器启动成功
echo.

REM 6. 等待服务启动
echo [INFO] 等待服务初始化...
echo 这可能需要30-60秒，请耐心等待...
timeout /t 30 /nobreak >nul
echo.

REM 7. 检查服务状态
echo [INFO] 服务状态:
docker-compose ps
echo.

REM 8. 检查服务健康状态
echo [INFO] 检查服务健康状态...
timeout /t 5 /nobreak >nul

docker-compose exec -T mysql mysqladmin ping -h localhost --silent >nul 2>&1
if not errorlevel 1 (
    echo [OK] MySQL 运行正常
) else (
    echo [WARN] MySQL 可能还在初始化中
)

docker-compose exec -T redis redis-cli ping >nul 2>&1
if not errorlevel 1 (
    echo [OK] Redis 运行正常
) else (
    echo [WARN] Redis 可能还在初始化中
)

curl -s http://localhost:9090/actuator/health >nul 2>&1
if not errorlevel 1 (
    echo [OK] 后端服务运行正常
) else (
    echo [WARN] 后端服务可能还在启动中，请稍后访问
)

echo.
echo ==========================================
echo   启动完成！
echo ==========================================
echo.
echo 访问地址:
echo   客户前台: http://localhost:8080
echo   管理后台: http://localhost:8081
echo   API文档:  http://localhost:9090/doc.html
echo.
echo 默认账号:
echo   用户名: admin
echo   密码: admin123
echo.
echo 常用命令:
echo   查看日志:   docker-compose logs -f
echo   查看状态:   docker-compose ps
echo   停止服务:   stop.bat 或 docker-compose down
echo   重启服务:   docker-compose restart
echo.
echo 提示:
echo   - 首次启动需要下载镜像，可能需要较长时间
echo   - 如果服务无法访问，请等待1-2分钟后重试
echo   - 数据库初始化需要时间，请耐心等待
echo.
echo ==========================================
echo.
pause
