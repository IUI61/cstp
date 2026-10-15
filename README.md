# 校园二手交易平台（CSTP）

一个前后端分离的校园二手交易平台：学生可以发布闲置商品、浏览选购、在线下单，并与交易对方实时沟通。项目先以 Spring Boot 单体形式完成业务闭环，后按业务边界改造为 **Spring Cloud 微服务架构**，支持容器化一键部署。

## 功能特性

- **用户模块**：注册（支持头像上传）、登录（BCrypt 加密存储 + Redis Token 鉴权）、拦截器统一校验登录态
- **商品模块**：发布商品（支持多图上传）、首页分页浏览（Redis 缓存加速）、商品详情（多图展示）
- **订单模块**：创建订单（商品名/图/价快照 + 业务订单号）、买家/卖家双视角订单列表、订单状态流转（发货、确认收货、取消）
- **聊天模块**：基于商品发起会话、历史消息查询、WebSocket 实时推送（心跳保活 + 断线自动重连；HTTP 发送与 WebSocket 接收的混合方案，保证消息可靠落库）

## 架构设计

```
                        ┌────────────────────────────┐
 浏览器/前端(Vue3:5173) │  Spring Cloud Gateway :8080 │
        │/api /uploads  └────────────┬───────────────┘
        │ /ws                         │ lb:// 负载均衡
        ▼                             ▼
┌──────────────┐   ┌─────────────────────────────────────────┐
│  注册发现     │   │  user-service :8081   商品查询/文件服务   │
│  Nacos :8848 │   │  goods-service :8082 ← /uploads 静态资源 │
│  (5 服务注册) │   │  order-service :8083                   │
└──────────────┘   │  chat-service  :8084 ← WebSocket 握手鉴权 │
                   └───────────────┬─────────────────────────┘
                                   ▼
              ┌───────────┬─────────────┬─────────────┐
              │  MySQL 8   │  Redis 7    │  RabbitMQ   │
              │ (宿主:3306)│ (:6379)     │ (:5672)     │
              │ 共库 cstp  │ Token/缓存   │ 订单→商品MQ │
              └───────────┴─────────────┴─────────────┘
```

- **服务拆分**：按业务边界拆分为 user / goods / order / chat 四个服务 + gateway 网关，消费方自定义轻量接收模型，服务边界互不依赖
- **注册发现**：Nacos 2.3.2 统一注册中心，各服务注册后由网关 `lb://` 负载均衡转发，Nacos 控制台可见服务健康状态
- **网关路由**：`/api/**` 统一入口（StripPrefix 剥离前缀），`/uploads/**` 静态资源转发 goods-service，`/ws/**` 以 `lb:ws://` 转发 WebSocket 长连接
- **服务间调用**：OpenFeign 声明式调用（订单查商品、批量查用户），内部接口与外部接口鉴权分层
- **异步解耦**：订单创建后通过 RabbitMQ **Topic 交换机** `cstp.order.topic`（key `goods.status.change`）通知商品状态变更，消费方 goods-service 落库，消息体采用 Jackson JSON 契约，跨服务不依赖 Java 序列化
- **实时通信**：聊天 WebSocket 过网关转发，握手阶段以 `token` 参数 + Redis 校验鉴权，消息先落库再推送，断线自动重连

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.3.0、MyBatis-Plus 3.5、Spring WebSocket、Knife4j（接口文档）、Validation |
| 微服务 | Spring Cloud 2023.0.1、Spring Cloud Alibaba 2023.0.1.0（Nacos 2.3.2 注册中心）、Spring Cloud Gateway、OpenFeign、RabbitMQ |
| 前端 | Vue 3（组合式 API）、Vite、Vue Router、Element Plus、Axios |
| 存储 | MySQL 8（共库 `cstp`）、Redis 7（Token/缓存） |
| 安全 | BCrypt 密码加密、基于 Redis 的 Token 鉴权、WebSocket 握手鉴权 |
| 部署 | Docker 多阶段构建 + Docker Compose（8 容器一键编排） |

## 快速开始

### 环境要求

- JDK 17+、Maven 3.6+、Node.js 20+
- Docker + Docker Compose（一键部署推荐）
- MySQL 8（Docker 部署时运行在宿主机，容器经 `host.docker.internal` 连接）

### 1. 初始化数据库

使用 MySQL 客户端执行 `CSTP/src/main/resources/db/mysql.sql`，创建 `cstp` 库及全部表结构（微服务各模块共用该库）。

### 2. 方式 A：Docker 一键部署（推荐）

在 `cstp-cloud/` 目录下：

```bash
# 1) 配置本地敏感信息（该文件已加入 .gitignore，不会提交）
#    复制/编辑 .env，填入数据库账号密码与上传目录
#    DB_USER=root
#    DB_PASSWORD=你的密码
#    UPLOAD_DIR=C:/你的路径/uploads

# 2) 构建并启动 8 个容器（5 服务 + Nacos/Redis/RabbitMQ）
docker compose up -d --build

# 3) 验证：网关全链路
curl http://localhost:8080/api/goods/show
```

启动后：

| 组件 | 地址 |
| --- | --- |
| 网关统一入口 | <http://localhost:8080> |
| Nacos 控制台 | <http://localhost:8848/nacos> |
| RabbitMQ 管理台 | <http://localhost:15672>（guest/guest） |
| 前端页面 | 见下方"启动前端" |

### 3. 方式 B：本地 IDEA 开发调试

1. 先启动中间件：Nacos（standalone）、Redis、RabbitMQ（可复用 compose 中的容器：`docker compose up -d nacos redis rabbitmq`）
2. 在 IDEA 中依次启动 `GatewayApplication`、`UserApplication`、`GoodsApplication`、`OrderApplication`、`ChatApplication`（各模块默认读本地 `127.0.0.1`，可通过环境变量 `NACOS_ADDR/DB_HOST/DB_USER/DB_PASSWORD/REDIS_HOST/MQ_HOST` 覆盖）
3. 接口文档（Knife4j，经网关）：<http://localhost:8080/doc.html>

### 4. 启动前端

```bash
cd vue-cstp
npm install
npm run dev
```

- 访问 <http://localhost:5173>（Vite 已配置代理：`/api`、`/uploads`、`/ws` 转发到网关 `localhost:8080`）

## 项目结构

```
untitled1/
├── cstp-cloud/                   # 微服务根工程（Maven 多模块，聚合 5 个模块）
│   ├── gateway-service/          # 网关：统一路由 /api/**、/uploads/**、/ws/**
│   ├── user-service/             # 用户：注册/登录/Token 鉴权/头像上传
│   ├── goods-service/            # 商品：发布/列表/详情，静态资源访问
│   ├── order-service/            # 订单：创建/双视角列表/状态流转，Feign + MQ
│   ├── chat-service/             # 聊天：会话/消息/WebSocket 实时通道
│   ├── docker-compose.yml        # 8 容器编排（5 服务 + nacos/redis/rabbitmq）
│   ├── .env                      # 本地敏感配置（已忽略提交）
│   └── pom.xml                   # 父工程（统一依赖版本管理）
├── CSTP/                         # 历史单体版本（Spring Boot，功能已迁移至微服务）
│   └── src/main/resources/db/mysql.sql   # 建表脚本（cstp 库）
└── vue-cstp/                     # 前端（Vue 3 + Vite）
    └── src/
        ├── views/                # 页面（首页/登录/发布/详情/订单/聊天）
        ├── router/               # 路由与登录守卫
        ├── utils/request.js      # Axios 封装（Token 注入、401 统一跳转）
        └── components/           # 公共组件
```

## 接口一览（网关统一前缀 `/api`）

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 用户 | POST | /api/user/register | 注册（multipart，含头像） |
| 用户 | POST | /api/user/login | 登录，返回 Token 与用户信息 |
| 商品 | GET | /api/goods/show | 首页分页列表 |
| 商品 | POST | /api/goods/issuance | 发布商品（多图） |
| 商品 | GET | /api/goods/detail | 商品详情 |
| 订单 | POST | /api/orders/create | 创建订单 |
| 订单 | GET | /api/orders/goodsOrdersList | 订单列表（role：buy/sell/release） |
| 订单 | PUT | /api/orders/goodsOrderStatus | 更新订单/商品状态 |
| 聊天 | POST | /api/chat/create | 创建（或获取）会话 |
| 聊天 | GET | /api/chat/conversationsList | 会话列表 |
| 聊天 | GET | /api/chat/messagesList | 历史消息 |
| 聊天 | POST | /api/chat/sendMessage | 发送消息 |
| 聊天 | WS | /ws/chat?token= | WebSocket 实时消息通道 |

## 设计要点

- **鉴权方案**：登录成功后签发 UUID Token 存入 Redis（有效期 7 天），前端通过请求头 `Authorization` 携带；网关/服务内拦截器统一校验，`ThreadLocal` 传递当前用户
- **微服务边界**：各服务自带独立的实体与接收模型，Feign 调用仅传必需字段，服务之间不互相 import 实体类
- **WebSocket 鉴权**：握手阶段 URL 参数 `token` 校验身份（Redis），通过后将 `userId` 写入会话属性定向推送；连接经网关 `lb:ws://` 转发
- **消息可靠性**：发送走 HTTP 先落库，再经 WebSocket 推送；离线跳过推送，上线后历史消息接口兜底
- **订单设计**：雪花 ID 主键，业务订单号（时间戳 + 随机数）唯一索引兜底；下单冗余商品名/图/价快照
- **ID 精度处理**：雪花 ID 序列化为字符串返回前端，避免 JavaScript Number 精度丢失
- **文件上传安全**：魔数 + 扩展名白名单双重校验；上传目录环境变量可配置（Docker 部署时经卷挂载宿主目录）
- **缓存**：商品首页分页缓存 Redis（30 分钟），版本号机制失效；Redis 异常自动降级查库
- **Docker 部署**：多阶段构建（Maven 构建 + JRE 运行，`-pl 模块 -am` 从多模块根构建）；compose 服务名 DNS 互访，DB 经 `host.docker.internal` 连宿主 MySQL，healthcheck 保证 RabbitMQ 就绪

## 待完善

- 补充单元测试与接口测试
- 商品编辑、搜索、收藏等扩展功能
- 补充页面截图与在线演示地址

## 说明

- 上传的图片默认保存在用户主目录下 `cstp/uploads/`（Docker 部署由 `.env` 的 `UPLOAD_DIR` 指定，并挂载进容器 `/data/uploads`），经网关 `/uploads/**` 静态映射访问
- 本地敏感配置（数据库密码等）统一放在 `cstp-cloud/.env`，已加入 .gitignore，不会被提交
