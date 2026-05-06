#!/bin/bash

# ==========================================
# 资源下载平台 - 停止脚本
# ==========================================
# 使用方法: ./stop.sh [选项]
# 选项:
#   -v, --volumes  同时删除数据卷（清空数据库）
#   -h, --help     显示帮助信息
# ==========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# 显示帮助信息
show_help() {
    echo "使用方法: ./stop.sh [选项]"
    echo ""
    echo "选项:"
    echo "  -v, --volumes    同时删除数据卷（清空数据库和Redis数据）"
    echo "  -h, --help       显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  ./stop.sh              # 仅停止服务，保留数据"
    echo "  ./stop.sh -v           # 停止服务并删除所有数据"
    exit 0
}

# 解析命令行参数
REMOVE_VOLUMES=false
while [[ $# -gt 0 ]]; do
    case $1 in
        -v|--volumes)
            REMOVE_VOLUMES=true
            shift
            ;;
        -h|--help)
            show_help
            ;;
        *)
            print_error "未知选项: $1"
            echo "使用 './stop.sh --help' 查看帮助"
            exit 1
            ;;
    esac
done

echo "=========================================="
echo "  资源下载平台 - 停止服务"
echo "=========================================="
echo ""

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose未安装"
    exit 1
fi

# 检查是否有运行的容器
if ! docker-compose ps -q &> /dev/null; then
    print_warning "没有运行中的服务"
    exit 0
fi

print_info "正在停止服务..."
echo ""

# 显示当前运行的服务
echo "当前运行的服务:"
docker-compose ps

echo ""

# 停止服务
if [ "$REMOVE_VOLUMES" = true ]; then
    print_warning "将删除所有数据卷（数据库和Redis数据将被清空）"
    read -p "确认删除所有数据? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v
        print_success "服务已停止，数据已删除"
    else
        echo "已取消操作"
        exit 0
    fi
else
    docker-compose down
    print_success "服务已停止，数据已保留"
fi

echo ""
echo "=========================================="
echo "  ✅ 操作完成"
echo "=========================================="
echo ""
echo "📝 常用命令:"
echo "  重新启动:     ./start.sh"
echo "  查看日志:     docker-compose logs"
echo "  删除所有数据: ./stop.sh -v"
echo ""
echo "💡 提示:"
echo "  - 数据已保留在Docker卷中，重启后数据不会丢失"
echo "  - 如需完全清理，请使用: ./stop.sh -v"
echo "  - 查看所有卷: docker volume ls"
echo ""
echo "=========================================="
