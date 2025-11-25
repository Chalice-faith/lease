# wsl

## windows使用wsl搭建开发环境

- 推荐wsl里使用独立docker服务，而不是docker-desktop集成使用，这样互不影响

  - 因为docker-desktop集成wsl，只是使用desktop的docker-cli，引擎还是windows里的那个docker，这样host网络模式不互通

  - Docker Desktop 即使是linux容器运行，在 Windows 上通过 Hyper-V 或 WSL 2 运行一个轻量级 Linux 虚拟机（VM），而 Linux 容器实际上是在这个 VM 中运行的

### 1. 安装wsl-Ubuntu环境

- 启动 windows 与 wsl 相关的功能组件，并更新wsl

```cmd
  wsl --update
```



- 进入 Windows Store 应用商店 安装 最新版Ubuntu，第一次打开可不设置用户名密码，后续默认root登录

- 或者

- wsl命令安装

```cmd
  wsl --list --online

  wsl --install Ubuntu
```



#### 1.1 wsl环境参数配置

- 配置wsl，进入windows用户目录下创建 .wslconfig 最好重启下电脑

```cmd
  [wsl2]

  #memory=6GB

  #processors=3

  #swap=0

  #localhostForwarding=true

  #autoMemoryReclaim=gradual # 可以在 gradual 、dropcache 、disabled 之间选择

  [boot]

  systemd=true

  [experimental]

  networkingMode=mirrored

  dnsTunneling=true # 启用 DNS 隧道功能，将容器的 DNS 查询通过加密隧道转发到主机的 DNS 解析器，绕过容器内默认的 DNS 配置

  firewall=false  # 为容器启用 动态防火墙

  autoProxy=true  # 启用 自动代理配置，动态同步主机代理变更到容器

  hostAddressLoopback=true  # 根据需要添加此选项，允许容器通过环回地址（127.0.0.1）访问主机服务
```



#### 1.2 wsl配置自启动

- 执行 `shell:startup` 打开文件夹，放入 `wsl-ubuntu-startup.vbs` 脚本即可

- 使用windows的任务计划程序，导入配置 Ubuntu.xml 即可

  - 这种方式经常有问题，需延迟1分钟执行计划任务

### 2. wsl-docker环境配置

安装依赖 + 配置 Docker 官方源（或国内源，解决网络问题）：

```bash
# 安装依赖工具
sudo apt-get update
sudo apt-get install ca-certificates curl gnupg lsb-release

# 配置 Docker 官方 GPG 密钥（避免校验失败）
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 配置国内源（推荐阿里云，比官方源快，避免网络超时）
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://mirrors.aliyun.com/docker-ce/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker 引擎（最新稳定版）
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

验证安装成功：

```bash
# 启动 Docker 服务
sudo systemctl start docker
# 设置开机自启
sudo systemctl enable docker
# 测试运行 hello-world 镜像（能输出信息则成功）
sudo docker run hello-world
```

安装dockercompose

```bash

curl -L https://github.com/docker/compose/releases/download/v2.35.1/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose

  如果github.com访问不通，也可以访问 https://ghproxy.cn/https://github.com/docker/compose/releases/download/v2.35.1/docker-compose-linux-x86_64

chmod +x /usr/local/bin/docker-compose
```

验证安装

```bash
docker-compose version
```





## 答疑解惑

### wsl中docker pull报错：Error response from daemon: Get "https://registry-1.docker.io/v2/"

- 首先进入 /etc/docker/daemon.json 配置镜像源和dns，但大部分情况没有效果

```json
  {

    "registry-mirrors": [

      "https://docker.m.daocloud.io"

    ],

    "log-driver": "json-file",

    "log-opts": {

      "max-size": "1024m",

      "max-file": "7"

    },

    "exec-opts": ["native.cgroupdriver=systemd"],

    "features": {"buildkit": true}

  }
```



- 测试网络连通性：curl -v https://registry-1.docker.io/v2/

- 配置`docker代理`，此种方法可行

  进入wsl环境

```bash
  sudo mkdir -p /etc/systemd/system/docker.service.d

  sudo nano /etc/systemd/system/docker.service.d/http-proxy.conf

  编辑以下内容，视代理端口进行修改，v2ray一般10809，clash一般7890

    [Service]

    Environment="HTTP_PROXY=http://127.0.0.1:10809"

    Environment="HTTPS_PROXY=http://127.0.0.1:10809"

  sudo systemctl daemon-reexec

  sudo systemctl restart docker
```



  docker info 查看是否配置代理成功

- 实在不行，推荐可以在其他可以拉取镜像的环境手动导入镜像到wsl内

### 迁移wsl环境安装目录，默认是C盘

```cmd
wsl --export Ubuntu "D:\\less\Ubuntu.tar"

wsl --unregister Ubuntu

wsl --import Ubuntu "D:\\less\wsl" "D:\\less\Ubuntu.tar" --version 2

wsl -d Ubuntu #进入指定的分发版本

- 上述操作完成之后能够顺利打开WSL Ubuntu，但是显示以root身份登录

    - 进行用户配置<username>是你前面注册的用户名（如果你的WSL Ubuntu的名称是Ubuntu-20.04，那么对应的可执行文件名为ubuntu2004.exe）

    - ubuntu.exe config --default-user zhh

    - 或者进入wsl编辑/etc/wsl.conf 配置[user] default = root
```

### windows程序连接wsl的mysql数据库经常会报错

- wsasend: An established connection was aborted by the software in your host machine.

  - 因为WSL 使用虚拟网卡 (Hyper-V Switch)，TCP 空闲一段时间会被 Windows 的 NAT 丢弃

    - Set-NetTCPSetting -SettingName InternetCustom -IdleTimeoutSec 14400

    - /etc/wsl.conf 配置，避免网络刷新导致连接断，默认值 7200 秒（即 2 小时）

```bash
      [network]

      generateResolvConf = false
```



  - MYSQL空闲连接超时

  - 最佳方案，还是程序和wsl在同一个环境运行
