@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ==========================================
REM 资源下载平台 - 停止脚本 (Windows)
REM ==========================================
REM 使用方法: stop.bat [选项]
REM 选项:
REM   /v  同时删除数据卷（清空数据库）
REM   /?  显示帮助信息
REM ==========================================

REM 检查帮助参数
if "%1"=="/?" (
    echo 使用方法: stop.bat [选项]
    echo.
    echo 选项:
    echo   /v    同时删除数据卷（清空数据库和Redis数据）
    echo   /?    显示此帮助信息
    echo.
    echo 示例:
    echo   stop.bat        # 仅停止服务，保留数据
    echo   stop.bat /v     # 停止服务并删除所有数据
    echo.
    pause
    exit /b 0
)

echo ==========================================
echo   资源下载平台 - 停止服务
echo ==========================================
echo.

REM 检查Docker Compose是否安装
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose未安装
    echo.
    pause
    exit /b 1
)

REM 检查是否有运行的容器
docker-compose ps -q >nul 2>&1
if errorlevel 1 (
    echo [WARN] 没有运行中的服务
    echo.
    pause
    exit /b 0
)

echo [INFO] 正在停止服务...
echo.

REM 显示当前运行的服务
echo 当前运行的服务:
docker-compose ps
echo.

REM 检查是否删除数据卷
if "%1"=="/v" (
    echo [WARN] 将删除所有数据卷（数据库和Redis数据将被清空）
    set /p CONFIRM="确认删除所有数据? (Y/N): "
    if /i "!CONFIRM!"=="Y" (
        docker-compose down -v
        echo [OK] 服务已停止，数据已删除
    ) else (
        echo 已取消操作
        pause
        exit /b 0
    )
) else (
    docker-compose down
    echo [OK] 服务已停止，数据已保留
)

echo.
echo ==========================================
echo   操作完成
echo ==========================================
echo.
echo 常用命令:
echo   重新启动:     start.bat
echo   查看日志:     docker-compose logs
echo   删除所有数据: stop.bat /v
echo.
echo 提示:
echo   - 数据已保留在Docker卷中，重启后数据不会丢失
echo   - 如需完全清理，请使用: stop.bat /v
echo   - 查看所有卷: docker volume ls
echo.
echo ==========================================
echo.
pause
