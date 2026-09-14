# 校园二手交易平台（CSTP）

一个前后端分离的校园二手交易平台：学生可以发布闲置商品、浏览选购、在线下单，并与交易对方实时沟通。

## 功能特性

- **用户模块**：注册（支持头像上传）、登录（BCrypt 加密存储 + Redis Token 鉴权）、拦截器统一校验登录态
- **商品模块**：发布商品（支持多图上传）、首页分页浏览（Redis 缓存加速）、商品详情（多图展示）
- **订单模块**：创建订单（商品名/图/价快照 + 业务订单号）、买家/卖家双视角订单列表、订单状态流转（发货、确认收货、取消）
- **聊天模块**：基于商品发起会话、历史消息查询、WebSocket 实时推送（心跳保活 + 断线自动重连；HTTP 发送与 WebSocket 接收的混合方案，保证消息可靠落库）

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.3、MyBatis-Plus 3.5、Spring WebSocket、Knife4j（接口文档）、Validation |
| 前端 | Vue 3（组合式 API）、Vite、Vue Router、Element Plus、Axios |
| 存储 | MySQL 8、Redis |
| 安全 | BCrypt 密码加密、基于 Redis 的 Token 鉴权 |

## 设计要点

- **鉴权方案**：登录成功后签发 UUID Token 并存入 Redis（有效期 7 天），前端通过请求头 `Authorization` 携带；后端拦截器统一校验，并通过 `ThreadLocal` 向业务层传递当前用户
- **WebSocket 鉴权**：握手阶段通过 URL 参数 `token` 校验身份，校验通过后将 `userId` 写入会话属性，用于消息定向推送
- **消息可靠性**：发送消息走 HTTP 接口先落库，再通过 WebSocket 推送给对方；对方离线时跳过推送，上线后通过历史消息接口兜底
- **订单设计**：雪花 ID 作为主键，业务订单号（时间戳 + 随机数）加唯一索引兜底；下单时冗余商品名/图/价快照，避免商品信息变更影响历史订单
- **ID 精度处理**：后端雪花 ID 序列化为字符串返回前端，避免 JavaScript Number 精度丢失
- **文件上传安全**：魔数（文件头）+ 扩展名白名单双重校验，防止伪装文件上传；上传目录可配置（默认用户主目录，支持环境变量覆盖）
- **缓存**：商品首页分页数据缓存至 Redis（30 分钟），采用**版本号机制**失效：发布 / 上下架等写操作仅自增版本号即可一次性失效所有分页（旧 key 靠 TTL 自然淘汰）；结合事务同步器在事务提交后才推进版本，避免并发下旧数据回填新缓存；Redis 读写异常时自动降级查库，缓存故障不影响主流程

## 快速开始

### 环境要求

- JDK 17+、Maven 3.6+
- Node.js 20+
- MySQL 8、Redis（本地默认端口即可）

### 1. 初始化数据库

使用 MySQL 客户端执行 `CSTP/src/main/resources/db/mysql.sql`，会创建 `cstp` 库和全部表结构。

### 2. 配置后端本地信息

复制 `CSTP/src/main/resources/application-local.yml.example` 为 `application-local.yml`，填入本机数据库密码（该文件已被 .gitignore 忽略，不会被提交）。

### 3. 启动后端

在 IDEA 中直接运行 `CstpApplication`，或在 `CSTP` 目录执行：

```bash
mvn spring-boot:run
```

- 后端端口：`8080`
- 接口文档（Knife4j）：<http://localhost:8080/doc.html>

### 4. 启动前端

```bash
cd vue-cstp
npm install
npm run dev
```

- 访问 <http://localhost:5173>（Vite 已配置代理：`/api`、`/uploads`、`/ws` 转发到 `localhost:8080`）

## 项目结构

```
untitled1/
├── CSTP/                          # 后端（Spring Boot）
│   └── src/main/
│       ├── java/org/example/cstp/
│       │   ├── config/            # 拦截器注册、WebSocket、MyBatis-Plus、安全配置
│       │   ├── controller/        # 接口层
│       │   ├── service/           # 业务层
│       │   ├── mapper/            # 数据访问层
│       │   ├── entity/dto/vo/     # 实体与传输对象
│       │   ├── interceptor/       # 登录与 WebSocket 鉴权拦截器
│       │   └── ws/                # WebSocket 处理器
│       └── resources/
│           ├── mapper/            # MyBatis XML 映射
│           ├── db/mysql.sql       # 建表脚本
│           └── application*.yml   # 应用配置
└── vue-cstp/                      # 前端（Vue 3 + Vite）
    └── src/
        ├── views/                 # 页面（首页/登录/发布/详情/订单/聊天）
        ├── router/                # 路由与登录守卫
        ├── utils/request.js       # Axios 封装（Token 注入、401 统一跳转）
        └── components/            # 公共组件
```

## 接口一览

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 用户 | POST | /user/register | 注册（multipart，含头像） |
| 用户 | POST | /user/login | 登录，返回 Token 与用户信息 |
| 商品 | GET | /goods/show | 首页分页列表 |
| 商品 | POST | /goods/issuance | 发布商品（多图） |
| 商品 | GET | /goods/detail | 商品详情 |
| 订单 | POST | /orders/create | 创建订单 |
| 订单 | GET | /orders/goodsOrdersList | 订单列表（role：buy/sell/release） |
| 订单 | PUT | /orders/goodsOrderStatus | 更新订单/商品状态 |
| 聊天 | POST | /chat/create | 创建（或获取）会话 |
| 聊天 | GET | /chat/conversationsList | 会话列表 |
| 聊天 | GET | /chat/messagesList | 历史消息 |
| 聊天 | POST | /chat/sendMessage | 发送消息 |
| 聊天 | WS | /ws/chat?token= | WebSocket 实时消息通道 |

## 待完善

- 补充单元测试与接口测试
- 商品编辑、搜索、收藏等扩展功能
- 补充页面截图与在线演示地址
- 部署上线（Docker 化 / 云服务器）

## 说明

- 上传的图片默认保存在用户主目录下 `cstp/uploads/`（可用环境变量 `CSTP_UPLOAD_DIR` 覆盖），并通过 `/uploads/**` 静态映射访问（已加入 .gitignore）
- 本地敏感配置（数据库密码等）存放在 `application-local.yml`，不提交到仓库；仓库中仅保留 `.example` 模板
