# Overnight Brief — 西瓜溯源小程序 (RuoYi + uni-app)

> Generated 2026-10-31 Asia/Shanghai for overnight Claude Code run.
> Owner asleep; decide reasonably; **do NOT use AskUserQuestion**. Prefer working code over long docs.

## 0. Goal / 目标

Implement a **minimal complete vertical slice** of watermelon farm-traceability on THIS template:

`D:\Projects\IDEAProjects\ruoyi-fullstack-template-jdk1.8-vue3`

Requirements image: `docs/requirements-core-features.png`

### 小程序核心功能（精简版）

1. **农户端**
   - 录入农事记录
   - 填报西瓜糖度 / 果型分级
   - 生成批次溯源二维码
2. **管理端**
   - 权限管理（复用 RuoYi RBAC）
   - 品控数据统计
   - 分级标准设置
   - 数据导出归档
3. **采购 / 消费者端**
   - 扫码查看种植全过程
   - 果品分级与品牌信息
4. **基础配套**
   - 云端存数据（MySQL + 本地文件/profile 上传）
   - 离线录入同步
   - 图文材料上传

Deliver: SQL + backend APIs + admin Vue pages + uni-app farmer/consumer pages. MIT. Clean commits. When done, summarize what works and how to run.

---

## 1. Research summary（调研摘要）

### Common architecture pattern（行业共识）

- **批次为主线**（一批一码为主；一物一码可选）：基地/地块 → 种植批次 → 农事记录 → 品控（糖度/果型）→ 赋码 → 消费者扫码时间线。
- **角色分离**：农户（田间录入）、管理员（标准/统计/导出）、消费者（只读扫码）。
- **离线**：田间弱网 → 本地队列 + 客户端 UUID 幂等 → 联网重放；服务端按 `clientId` 去重。
- **不要上区块链**（本夜间任务）：MySQL 足够；Fabric 类方案过重，仅作参考。

### Key sources（去重后）

| Source | Why useful |
|--------|------------|
| [农产品溯源小程序怎么做？批次/二维码/生产记录](https://www.whkakaxi.com/news/18399.html) | Batch coding, roles, offline note, consumer page fields |
| [农事记录微信小程序设计与实践](http://www.thearxiv.net/2025/88/dd0c202588420.html) | Uni-app + MySQL farm-log tables & pages |
| [cxyxscn/suyuan-uniapp](https://github.com/cxyxscn/suyuan-uniapp) | Vue2 + Uni-app + Spring Boot produce trace + scan |
| [RuoYi-App](https://doc.ruoyi.vip/ruoyi-app/) / [yangzongzhuan/RuoYi-App](https://github.com/yangzongzhuan/RuoYi-App) | Official uni-app shell already in this template |
| [prep-meals-blockchain-traceability](https://github.com/linyshdhhcb/prep-meals-blockchain-traceability) | RuoYi-based multi-module + miniapp (Java17; pattern only) |
| [steakliu/blockchain-trace-fabric](https://github.com/steakliu/blockchain-trace-fabric) | RuoYi PC + applets; farm process timeline UX |
| [东台西瓜分等分级 DB3209 / 一瓜一码](https://www.dongtai.gov.cn/art/2021/5/15/art_7946_3534056.html) | Sugar + shape + weight grading semantics for watermelon |
| [小程序离线队列](https://plumephp.com/miniprogram-offline-cache/) | Storage queue + client id idempotent replay |
| WeChat weak-network docs | Cache / offline UX guidelines |

### Watermelon grading hints（糖度/果型）

- Track **中心糖度(brix)**、可选边糖、**果型**（大/中/小或重量档）、外观等级（特等/一等/二等）。
- Admin configures thresholds in `wm_grade_standard`; farmer submits measured values; system auto-suggests grade.

### Offline sync approach（推荐实现）

1. Farmer form writes to `uni.setStorage` queue item: `{ clientId, type, payload, createdAt, retry }`.
2. On network restore / app show: flush queue → `POST /trace/sync/batch`.
3. Server upserts by unique `client_id`; returns accepted/rejected list.
4. Images: upload when online; offline keep local temp path and upload on sync (or defer image until online — prefer defer for MVP).

### QR content format

```
https://<host>/t/{batchCode}
```
or mini-program path query:
```
pages/consumer/trace?code=WM20261031XXXX
```
Batch code example: `WM` + `yyyyMMdd` + 4-digit seq, unique. Payload stores `batch_id` + `batch_code`. Generate PNG via ZXing (Java) or frontend `uQRCode` / canvas; store relative path under `ruoyi.profile`.

---

## 2. This template snapshot

| Layer | Path | Stack |
|-------|------|-------|
| Backend | `backend/` | RuoYi 3.9.2, Spring Boot **2.5.15**, JDK **1.8**, MyBatis, Redis, JWT |
| Admin UI | `frontend/pc-web/` | Vue3 + Vite + Element Plus + Pinia |
| Mini program | `frontend/uni-app/` | Official RuoYi-App shell (login/work/mine only — **no farm pages yet**) |
| SQL | `backend/sql/ry_20260417.sql` | Base schema |
| DB config | `application-druid.yml` | `jdbc:mysql://localhost:3306/ry-vue` user `root` / `password` |
| Upload | `ruoyi.profile` | `D:/ruoyi/uploadPath` |
| Local JDK8 | `D:\Develop\Java\jdk1.8.0_202` | **Use this**, default `java` on PATH is 25 |

**Note (infra overnight):** MySQL/Redis/Docker were **not running** when brief was written. Implement SQL + code fully; if DB unavailable, still finish code and document `how to run`. Optionally start WSL/Docker if feasible without blocking.

Git: local `master` one commit `project init`, **no remote**. Target push: **new public repo** `duo2530/watermelon-traceability-ruoyi` (name free), **MIT**. Do **not** overwrite template repo `duo2530/ruoyi-fullstack-template-jdk1.8-vue3` unless owner asked (they asked new public MIT repo for this work).

---

## 3. Recommended module map

### Backend package
`com.ruoyi.trace` (new module **or** put under `ruoyi-system` / `ruoyi-admin` controllers for speed — prefer new `ruoyi-trace` module if time; else `ruoyi-system` domain+mapper+service+controller is OK for MVP).

### Tables (prefix `wm_`)

1. `wm_plot` — 地块 (id, name, area, address, status, create_by, …)
2. `wm_batch` — 批次 (id, batch_code UNIQUE, plot_id, variety, plant_date, harvest_date, brand_name, brand_desc, qr_path, status, …)
3. `wm_farm_record` — 农事 (id, batch_id, record_type, content, record_time, weather, images JSON, client_id UNIQUE, …)
4. `wm_qc_record` — 品控 (id, batch_id, sugar_center, sugar_edge, fruit_shape, weight_kg, grade_code, sample_no, images, client_id UNIQUE, …)
5. `wm_grade_standard` — 分级标准 (id, name, variety, sugar_min, sugar_max, shape_rule, weight_min, weight_max, grade_code, sort, status)
6. `wm_brand` — 品牌信息 (optional; or fields on batch)
7. `wm_sync_log` — optional sync audit

Also add menu SQL for admin under 西瓜溯源.

### Roles
- Reuse `admin`
- Add role `farmer` (农户) — menu/API for farm+qc+qr
- Consumer scan API **anonymous** (`@Anonymous` or permitAll) for `GET /trace/public/{batchCode}`

### Admin Vue pages (`frontend/pc-web/src/views/trace/`)
- plot, batch, farmRecord, qcRecord, gradeStandard, stats (ECharts sugar distribution / grade counts), export (Excel via RuoYi Excel util)

### uni-app pages
**Farmer (auth):**
- `pages/farmer/index` 工作台
- `pages/farmer/farm-record` 农事录入
- `pages/farmer/qc-submit` 糖度/果型
- `pages/farmer/batch-list` / `batch-create`
- `pages/farmer/qr` 生成/展示二维码
- `pages/farmer/sync` 离线队列状态

**Consumer (public):**
- `pages/consumer/scan` 扫码
- `pages/consumer/trace` 全过程时间线 + 分级 + 品牌

Wire tabBar or workbench entries. Keep existing RuoYi login.

### APIs (sketch)
- CRUD `/trace/plot`, `/trace/batch`, `/trace/farm`, `/trace/qc`, `/trace/grade`
- `POST /trace/batch/{id}/qr` generate
- `GET /trace/public/{code}` full timeline
- `GET /trace/stats/...`
- `POST /trace/export/...`
- `POST /trace/sync/batch` offline flush
- reuse `/common/upload` for images

---

## 4. Implementation order（严格按此）

1. SQL migration `backend/sql/wm_trace.sql` (+ menu/role dict)
2. Domain / Mapper / Service / Controller
3. Admin Vue api + views + router/menu SQL
4. uni-app farmer + consumer pages + offline queue util
5. QR generation end-to-end
6. Stats + export
7. Root README: how to run (JDK8, MySQL import both SQL files, Redis, backend, pc-web, HBuilderX uni-app)
8. Ensure `.gitignore` covers `.env`, keys, `node_modules`, `target`, `unpackage`, upload paths
9. Commit logically; prepare for `gh repo create watermelon-traceability-ruoyi --public --license mit --source=. --remote=origin --push`

### Hard rules for overnight
- **No AskUserQuestion.** Pick sensible defaults.
- Prefer end-to-end thin slice that compiles over perfect abstraction.
- Do not commit secrets.
- Chinese UI labels OK; code/comments can be bilingual.
- Use `JAVA_HOME=D:\Develop\Java\jdk1.8.0_202` for Maven.

### Done criteria
- Farmer can create batch + farm record + QC (API + UI)
- Admin can set grade standard + see stats + export
- Consumer can open trace by batch code / QR payload
- Offline queue exists on mini-program (even if only partially tested)
- README + MIT

---

## 5. Stuck / push policy

If blocked 3 times or feature set reasonably done → push public MIT repo and stop waiting for owner.
