#### 1. 创建 docker-compose.yml 文件

这是核心配置文件，定义所有服务及其关系：

```yml
version: '3.8'

services:
  minio:
    image: minio/minio
    container_name: minio
    volumes:
      - "./minio/data:/data"
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=admin
      - MINIO_ROOT_PASSWORD=adminadmin
      - TZ=Asia/Shanghai
    ports:
      - "9000:9000"
      - "9001:9001"
    restart: always
    privileged: true
  
  mysql:
    image: mysql:8.0
    container_name: mysql
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=123456
      - TZ=Asia/Shanghai
    volumes:
      - "./mysql/data:/var/lib/mysql"
      - "./mysql/conf/conf.d:/etc/mysql/conf.d"
      - "./mysql/logs:/var/log/mysql"
    restart: always
    privileged: true
  
  redis:
    image: redis:7.0
    container_name: redis
    ports:
      - "6379:6379"
    environment:
      - TZ=Asia/Shanghai
    volumes:
      - ./redis/conf/redis.conf:/etc/redis/redis.conf
      - ./redis/data:/data
    restart: always
    privileged: true
    command: ["redis-server", "/etc/redis/redis.conf"]
```

## 2. 构建和运行命令

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止所有服务
docker-compose down

# 重新构建并启动
docker-compose up -d --build
```

直接在docker-compose.yaml文件目录下运行

```bash
docker-compose up -d
```

等待镜像拉取成功

## 3. 常用管理命令

```bash
# 启动特定服务
docker-compose up -d mysql

# 扩展某个服务的实例数
docker-compose up -d --scale zoo-server=3

# 进入容器内部调试
docker-compose exec redis sh

# 查看资源使用情况
docker-compose top
```


