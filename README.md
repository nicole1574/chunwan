# 春晚知识图谱管理系统

技术栈：Vue3 + Element Plus + Spring Boot + Neo4j

## 目录结构

- `/frontend`：前端（Vite + Vue3）
- `/backend`：后端（Spring Boot + Spring Data Neo4j + Spring Security）

## 环境要求

- Node.js 20+
- JDK 17+
- Maven 3.9+
- Neo4j 5+

## Neo4j 配置

后端读取以下环境变量（可选）：

- `NEO4J_URI`（默认 `bolt://localhost:7687`）
- `NEO4J_USERNAME`（默认 `neo4j`）
- `NEO4J_PASSWORD`（默认 `password`）

## 启动后端

```bash
cd /home/runner/work/chunwan/chunwan/backend
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

## 启动前端

```bash
cd /home/runner/work/chunwan/chunwan/frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 登录账号

- 管理员：`admin / admin123`
- 用户：`user / user123`

## 功能清单

- 管理员：演职人员、节目、年份、关系、身份、关系分类的增删改查
- 用户：知识图谱查看（只读）
- 百度百科抓取：输入姓名自动抓取并回填/入库
- 联想输入：关系管理中人员与节目支持自动补全
- 知识图谱可视化：支持拖拽、缩放、关系查看

## 主要接口

- 登录：`POST /api/auth/login`
- 演职人员：`/api/persons`（GET）, `/api/admin/persons`（POST/PUT/DELETE）
- 节目：`/api/programs`（GET）, `/api/admin/programs`（POST/PUT/DELETE）
- 年份：`/api/years`（GET）, `/api/admin/years/{year}`（POST）
- 关系：`POST|DELETE /api/admin/relations`
- 百科抓取：`POST /api/admin/persons/crawl?name=xxx`
- 图谱数据：`GET /api/graph`
