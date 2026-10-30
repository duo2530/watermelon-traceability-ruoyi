# 西瓜溯源（RuoYi 版）

基于 [RuoYi-Vue 3.9.2](https://gitee.com/y_project/RuoYi-Vue)（Spring Boot 2.5 + JDK 8）、RuoYi-Vue3 管理端和 RuoYi-App（uni-app）实现的西瓜种植溯源系统。一批一码，农户在田间录入，管理端做品控统计，消费者扫码查看种植全过程、等级和品牌。

## 功能

| 端 | 功能 |
| --- | --- |
| 农户端（uni-app） | 工作台；新建地块/批次并自动生成二维码；农事记录（整地、定植、施肥、灌溉、植保、授粉、整枝、采收）；糖度/果型/单果重填报并实时预判等级；**离线录入**，联网后自动同步；现场图片上传 |
| 管理端（pc-web） | 复用 RuoYi 用户/角色/菜单权限；地块、批次（二维码预览、重新生成、消费者视角预览）、农事、品控管理；等级标准设置；品控统计（等级分布、糖度分布、月度趋势、品种/批次汇总）；多 Sheet 导出与批次归档 |
| 消费者端（uni-app H5 / 小程序） | 扫码或输入溯源码，免登录查看品牌、果品等级、糖度、抽检明细、产地和种植时间线 |
| 基础 | MySQL 存储；图片与二维码存于 `ruoyi.profile` 目录并通过 `/profile/**` 访问；农户数据按本人隔离 |

## 目录

```
backend/                 RuoYi 后端（Maven 多模块）
  ruoyi-trace/           溯源业务模块（domain / mapper / service / controller）
  sql/ry_20260417.sql    RuoYi 基础表
  sql/quartz.sql         定时任务表
  sql/wm_trace.sql       溯源表、字典、菜单、农户角色与示例账号（可重复执行）
  bin/trace-smoke-test.sh  API 冒烟测试
frontend/pc-web/         管理端（Vue3 + Element Plus + Vite），页面在 src/views/trace/
frontend/uni-app/        农户端 + 消费者端（uni-app Vue3），页面在 pages/farmer/、pages/consumer/
docs/                    需求说明
```

## 本地运行

### 1. 环境

- JDK 1.8、Maven 3.6+
- MySQL 5.7 / 8.0
- Redis 5+
- Node.js 16+（管理端）
- HBuilderX 3.x+（uni-app），微信小程序需微信开发者工具

可以用 Docker 起中间件：

```bash
docker run -d --name wm-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=<你的密码> mysql:8.0 \
  --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
docker run -d --name wm-redis -p 6379:6379 redis:7
```

### 2. 初始化数据库

按顺序导入三个脚本（库名默认 `ry-vue`）：

```bash
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS \`ry-vue\` DEFAULT CHARSET utf8mb4"
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < backend/sql/ry_20260417.sql
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < backend/sql/quartz.sql
mysql -uroot -p --default-character-set=utf8mb4 ry-vue < backend/sql/wm_trace.sql
```

### 3. 启动后端

修改 `backend/ruoyi-admin/src/main/resources/application-druid.yml` 中的数据库地址和账号密码、`application.yml` 中的 Redis 配置与上传目录 `ruoyi.profile`（默认 `D:/ruoyi/uploadPath`）。部署到公网前务必更换 `token.secret`。

```bash
cd backend
mvn clean package -DskipTests      # 必须使用 JDK 8
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

后端监听 `http://localhost:8080`。

二维码内容前缀由 `trace.qr-prefix` 控制，默认 `http://localhost:9090/#/pages/consumer/trace?code=`（本地 H5 消费者页）。上线时在 `application.yml` 中加入：

```yaml
trace:
  qr-prefix: https://your-domain/#/pages/consumer/trace?code=
```

修改后在批次管理里点“重新生成二维码”即可。

### 4. 启动管理端

```bash
cd frontend/pc-web
npm install
npm run dev
```

浏览器打开 `http://localhost:80`，菜单“西瓜溯源”下是全部业务页面。

### 5. 运行 uni-app

1. 用 HBuilderX 打开 `frontend/uni-app`。
2. 按需修改 `config.js` 中的 `baseUrl`（默认 `http://localhost:8080`）。
3. 运行到浏览器（H5 开发端口 9090）或运行到微信小程序。小程序真机需要 HTTPS 合法域名，并在 `manifest.json` 中替换为自己的 appid。

消费者入口：`#/pages/consumer/scan`（扫码 / 输入溯源码）或 `#/pages/consumer/trace?code=批次号`，无需登录。

### 默认账号

| 账号 | 角色 | 说明 |
| --- | --- | --- |
| admin | 超级管理员 | RuoYi 初始账号，密码见 RuoYi 文档 |
| farmer | 农户 | `wm_trace.sql` 创建，密码与 admin 初始密码相同，**上线前请修改** |

RuoYi 默认开启登录验证码。如需脚本测试，可在“系统管理 → 参数设置”临时关闭 `sys.account.captchaEnabled`。

## 关键设计

### 批次与二维码

批次号格式 `WM + yyyyMMdd + 4 位流水`，例如 `WM202610310001`。新建批次时用 ZXing 生成 PNG，保存到 `{profile}/qrcode/{批次号}.png`。

### 等级判定

`wm_grade_standard` 按排序从高到低匹配，同时检查中心糖度、边糖、单果重区间和允许的果型；指定品种的标准优先于通用标准；阈值为空表示不限制；全部不满足时判为“等外”。初始数据为特等果、一等果、二等果三档，可在管理端调整。

### 离线录入与同步

- 农户端提交农事或品控时，记录先写入本地队列（`uni.setStorage`，键 `wm_offline_queue`），每条带客户端生成的唯一 `clientId`。
- 在线时立即同步；离线时保留在本机。App 启动、回到前台、网络恢复时自动同步，也可在“离线同步”页手动同步或重试。
- 离线拍的图片先 `uni.saveFile` 保存到本地，同步前再上传。
- 后端 `POST /trace/sync/batch` 每次最多 200 条，`wm_farm_record.client_id` 和 `wm_qc_record.client_id` 有唯一索引，重复提交返回 `duplicate`，不会产生重复数据。校验失败（批次不存在、无权限、已归档、糖度越界）返回 `rejected` 并给出原因。每次同步写入 `wm_sync_log`。

### 数据权限

农户只能看到和操作自己的地块、批次和记录；管理员或拥有 `trace:data:all` 权限的角色可以看全部。

## 主要接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET/POST/PUT/DELETE | `/trace/plot` | 地块 |
| GET/POST/PUT/DELETE | `/trace/batch` | 批次；`POST /trace/batch/{id}/qrcode` 重新生成二维码 |
| GET/POST/PUT/DELETE | `/trace/farm` | 农事记录 |
| GET/POST/PUT/DELETE | `/trace/qc` | 品控记录；`GET /trace/qc/evaluate` 预判等级 |
| GET/POST/PUT/DELETE | `/trace/grade` | 等级标准 |
| POST | `/trace/sync/batch` | 离线批量同步 |
| GET | `/trace/stats` | 品控统计 |
| POST | `/trace/archive/export` | 导出（批次 / 农事 / 品控三个 Sheet） |
| PUT | `/trace/archive/{batchIds}` | 批次归档 |
| GET | `/trace/public/{batchCode}` | 消费者溯源查询（匿名，按 IP 限流 60 次/分钟） |

## 测试

```bash
cd backend && mvn test -pl ruoyi-trace -am        # 等级匹配单元测试
bash backend/bin/trace-smoke-test.sh             # 需后端运行且临时关闭验证码；密码用环境变量 PW 指定
```

## 许可证

[MIT](LICENSE)。RuoYi 框架部分沿用其原有 MIT 许可。
