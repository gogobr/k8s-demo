# k8s-demo

基于 Spring Cloud 的 Kubernetes 微服务演示项目，涵盖网关、前后端服务、营销活动等模块，配套 GitOps（Argo CD）、Helm 部署与可观测性能力。

## 技术栈

| 类别 | 技术 |
|------|------|
| 运行时 | Java 21、Spring Boot 3.4.1 |
| 微服务 | Spring Cloud 2024.0.0、Spring Cloud Alibaba |
| 配置与注册 | Nacos |
| 网关 | Spring Cloud Gateway、Sentinel 限流/熔断 |
| 数据 | MySQL、MyBatis、读写分离（ReplicationRoutingDataSource） |
| 缓存/消息 | Redis、RocketMQ |
| 分布式事务 | Seata |
| RPC | gRPC |
| 可观测性 | OpenTelemetry、Prometheus、Zipkin/Brave |
| 部署 | Docker、Kubernetes、Helm、Argo CD |

## 项目结构

```
k8s-demo/
├── apps/                          # 业务应用
│   ├── frontend/                  # 前端聚合服务
│   ├── backend/                   # 后端服务（RocketMQ、Seata、gRPC）
│   ├── backend-v2/                # 后端 v2（库存、财务、幂等等）
│   ├── gateway/                   # 网关（Sentinel）
│   └── marketing-activity/        # 营销活动服务（TCC、gRPC）
├── charts/
│   └── spring-boot-app/           # 通用 Spring Boot Helm Chart
├── gitops/                        # Argo CD 应用定义
│   ├── app-frontend.yaml
│   ├── app-backend-v2.yaml
│   └── ...
├── infrastructure/                # 基础设施与中间件
│   ├── middleware/                # Nacos、MySQL、Redis、RocketMQ、Sentinel、Seata、Ingress
│   └── observability/             # 可观测性（Prometheus、告警等）
├── loadtest/                      # k6 压测脚本
└── .github/workflows/              # CI/CD（部署、代码质量）
```

## 前置要求

- **JDK 21**（推荐 Eclipse Temurin）
- **Maven 3.9+**
- **Docker**（本地构建镜像时）
- **Kubernetes 集群**（本地可用 minikube/kind/k3s）
- **Argo CD**（若使用 GitOps 部署）

## 本地构建与运行

### 全量构建

```bash
mvn clean package -DskipTests
```

### 单模块构建（示例：frontend）

```bash
mvn clean package -pl apps/frontend -am -DskipTests
```

### 本地运行某一应用

各应用通过 Nacos 拉取配置，需先确保 Nacos 及依赖中间件（MySQL、Redis 等）可用，再启动对应模块，例如：

```bash
cd apps/frontend && mvn spring-boot:run
```

具体配置见各模块 `bootstrap.yml` 及 Nacos 中的配置。

## 镜像构建

各应用目录下均有 `Dockerfile`，使用 Eclipse Temurin 21 并挂载 OpenTelemetry Java Agent。示例（frontend）：

```bash
cd apps/frontend
mvn clean package -DskipTests
docker build -t your-registry/k8s-demo-frontend:latest .
```

## 部署方式

### Helm 部署

使用通用 Chart `charts/spring-boot-app`，通过各应用下的 `values.yaml` 区分配置：

```bash
helm install frontend ./charts/spring-boot-app -f apps/frontend/values.yaml -n default
```

### Argo CD（GitOps）

仓库中 `gitops/` 下为 Argo CD `Application` 资源，指向本仓库的 Helm Chart 与对应 `values`。将 `gitops/` 中应用按需部署到 Argo CD 后，可实现 Git 变更自动同步到集群。

## CI/CD

- **部署流水线**：`.github/workflows/deploy-frontend.yml`、`deploy-marketing-activity.yml` 等，在推送到 `main` 且相关路径变更时构建镜像并更新部署（如更新 Helm values 中的镜像 tag）。
- **代码质量**：`.github/workflows/qodana_code_quality.yml` 用于 Qodana 代码质量检查。

构建使用 JDK 21、Maven 缓存；镜像构建支持多架构（QEMU + Buildx）。需在仓库 Secrets 中配置 `DOCKER_USERNAME`、`DOCKER_PASSWORD` 等。

## 基础设施

中间件与可观测性资源在 `infrastructure/` 下，需按依赖顺序部署（如先 Nacos、MySQL、Redis，再 MQ、Sentinel、Seata 等）：

- **middleware/**：Nacos、MySQL（含主从/外部）、Redis、RocketMQ（含 Dashboard）、Sentinel、Seata、Ingress
- **observability/**：Prometheus、告警规则、Ingress 等

具体清单见各子目录内 YAML。

## 负载测试

使用 [k6](https://k6.io/) 进行压测，脚本位于 `loadtest/script.js`：

```bash
k6 run loadtest/script.js
```

脚本中请求地址需根据实际环境修改（如 `frontend.local` 或集群 Ingress 地址）。

## 许可证

本项目为演示用途，请根据实际需要自行确定使用与许可方式。
