这个报错 `E: Package 'docker-ce' has no installation candidate` 的核心原因是 **Docker 的软件源配置错误**（系统找不到 `docker-ce` 相关包），大概率是「源的 codename 与你的 Ubuntu 系统版本不匹配」或「源文件配置有误」。

下面按「排查 → 修复」的步骤解决，全程复制命令执行即可：

### 第一步：先确认你的 Ubuntu 系统版本（关键！）

执行以下命令，查看系统的 **codename**（比如 Ubuntu 20.04 是 `focal`，22.04 是 `jammy`，24.04 是 `noble`）：



```bash
lsb_release -cs
```

⚠️ 记住这个 codename（后面要用到），比如输出 `jammy` 就对应 Ubuntu 22.04。

### 第二步：删除错误的 Docker 源文件

先删除之前可能配置错的 Docker 源，避免冲突：



```bash
# 删除旧的 Docker 源文件（如果存在）
sudo rm -f /etc/apt/sources.list.d/docker.list
```

### 第三步：重新配置「正确的国内 Docker 源」

按你系统的 codename 配置源（以 **Ubuntu 22.04（jammy）** 为例，其他版本替换 codename 即可）：

#### 1. 重新安装依赖 + 导入 Docker 官方 GPG 密钥

```bash
# 确保依赖工具已安装
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release
```

#### 2. 导入 Docker 官方密钥（避免校验失败）

```bash
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
```

#### 3. 配置阿里云 Docker 源（替换为你的 codename）

把下面命令中的 `jammy` 换成第一步查到的 **你的系统 codename**（比如 `focal`/`noble`），然后执行：

```bash
# 替换 <你的codename> 为实际值（比如 jammy/focal/noble）
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://mirrors.aliyun.com/docker-ce/linux/ubuntu <你的codename> stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

✅ 示例（如果你的 codename 是 `focal`，即 Ubuntu 20.04）：

```bash
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://mirrors.aliyun.com/docker-ce/linux/ubuntu focal stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

### 第四步：更新源并重新安装 Docker

```bash
# 更新软件包索引（让系统识别到新配置的 Docker 源）
sudo apt-get update

# 安装 Docker 核心组件（此时不会再提示「无安装候选」）
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

### 第五步：验证安装成功

```bash
# 启动 Docker 服务
sudo systemctl start docker

# 设置开机自启
sudo systemctl enable docker

# 测试运行 hello-world 镜像（输出 "Hello from Docker!" 即成功）
sudo docker run hello-world
```
