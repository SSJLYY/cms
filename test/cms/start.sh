#!/bin/bash

# ==========================================
# 资源下载平台 - 一键启动脚本
# ==========================================
# 使用方法: ./start.sh
# 功能: 检查环境、启动Docker服务、显示访问信息
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

echo "=========================================="
echo "  资源下载平台 - Docker一键启动"
echo "=========================================="
echo ""

# 1. 检查Docker是否安装
print_info "检查Docker环境..."
if ! command -v docker &> /dev/null; then
    print_error "Docker未安装"
    echo "请先安装Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# 检查Docker服务是否运行
if ! docker info &> /dev/null; then
    print_error "Docker服务未运行"
    echo "请启动Docker服务后重试"
    exit 1
fi

print_success "Docker已安装并运行"

# 2. 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose未安装"
    echo "请先安装Docker Compose"
    exit 1
fi

print_success "Docker Compose已安装"
echo ""

# 3. 检查端口是否被占用
check_port() {
    local port=$1
    local service=$2
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        print_warning "端口 $port ($service) 已被占用"
        return 1
    fi
    return 0
}

print_info "检查端口占用情况..."
ports_ok=true
check_port 3306 "MySQL" || ports_ok=false
check_port 6379 "Redis" || ports_ok=false
check_port 8080 "客户前台" || ports_ok=false
check_port 8081 "管理后台" || ports_ok=false
check_port 9090 "后端API" || ports_ok=false

if [ "$ports_ok" = true ]; then
    print_success "所有端口可用"
else
    echo ""
    print_warning "部分端口已被占用，可能导致服务启动失败"
    read -p "是否继续启动? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "已取消启动"
        exit 1
    fi
fi

echo ""
print_info "开始启动服务..."
echo ""

# 4. 检查docker-compose.yml是否存在
if [ ! -f "docker-compose.yml" ]; then
    print_error "找不到 docker-compose.yml 文件"
    echo "请确保在项目根目录下运行此脚本"
    exit 1
fi

# 5. 启动Docker Compose
echo "正在启动容器..."
if docker-compose up -d; then
    print_success "容器启动成功"
else
    print_error "容器启动失败"
    echo "请查看错误信息并重试"
    exit 1
fi

echo ""
print_info "等待服务初始化..."
echo "这可能需要30-60秒，请耐心等待..."

# 等待服务启动
for i in {1..6}; do
    echo -n "."
    sleep 5
done
echo ""

# 6. 检查服务状态
echo ""
print_info "服务状态:"
docker-compose ps

# 7. 检查服务健康状态
echo ""
print_info "检查服务健康状态..."
sleep 5

# 检查MySQL
if docker-compose exec -T mysql mysqladmin ping -h localhost --silent &> /dev/null; then
    print_success "MySQL 运行正常"
else
    print_warning "MySQL 可能还在初始化中"
fi

# 检查Redis
if docker-compose exec -T redis redis-cli ping &> /dev/null; then
    print_success "Redis 运行正常"
else
    print_warning "Redis 可能还在初始化中"
fi

# 检查后端
if curl -s http://localhost:9090/actuator/health &> /dev/null; then
    print_success "后端服务运行正常"
else
    print_warning "后端服务可能还在启动中，请稍后访问"
fi

echo ""
echo "=========================================="
echo "  ✅ 启动完成！"
echo "=========================================="
echo ""
echo "📱 访问地址:"
echo "  🌐 客户前台: http://localhost:8080"
echo "  🔧 管理后台: http://localhost:8081"
echo "  📚 API文档:  http://localhost:9090/doc.html"
echo ""
echo "🔑 默认账号:"
echo "  👤 用户名: admin"
echo "  🔒 密码: admin123"
echo ""
echo "📝 常用命令:"
echo "  查看日志:   docker-compose logs -f"
echo "  查看状态:   docker-compose ps"
echo "  停止服务:   ./stop.sh 或 docker-compose down"
echo "  重启服务:   docker-compose restart"
echo ""
echo "💡 提示:"
echo "  - 首次启动需要下载镜像，可能需要较长时间"
echo "  - 如果服务无法访问，请等待1-2分钟后重试"
echo "  - 数据库初始化需要时间，请耐心等待"
echo ""
echo "=========================================="
