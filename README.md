# 物流托盘押金循环归还系统

## 系统概述

围绕发货方、承运商、财务三方的物流托盘押金循环归还管理系统。

## 核心业务流程

1. **发货方**：缴纳押金 → 领取托盘（校验可用押金）
2. **承运商**：归还托盘（选择托盘状态：正常/破损/丢失）
3. **财务**：核算押金、扣款管理、账期管理

## 业务规则

- ⚠️ **未缴押金不能领用托盘**（系统校验可用押金金额）
- 💰 **破损托盘按押金50%扣款，丢失按100%扣款**
- 🔒 **账期关闭后，归还记录、领用记录、扣款记录均不能修改/删除**

## 角色权限

| 角色 | 功能 |
|------|------|
| 发货方 SHIPPER | 缴纳押金、查看余额、领用托盘 |
| 承运商 CARRIER | 归还托盘（含正常/破损/丢失） |
| 财务 FINANCE | 全部功能，含托盘管理、账期管理、扣款管理 |

## 快速开始

### 方式一：Docker Compose 一键启动（推荐）

```bash
cd 1099
docker-compose up -d --build
```

- 前端：http://localhost:3000
- 后端API：http://localhost:8080/api
- H2控制台：http://localhost:3000/h2-console/

### 方式二：本地开发

**后端：**
```bash
cd backend
mvn spring-boot:run
```

**前端：**
```bash
cd frontend
npm install --registry=https://registry.npmmirror.com
npm run dev
```

访问 http://localhost:3000

## 测试账号

| 用户编码 | 角色 | 密码 |
|----------|------|------|
| SHIPPER001 | 发货方（华东发货仓） | 123456 |
| SHIPPER002 | 发货方（华南发货仓） | 123456 |
| CARRIER001 | 承运商（顺丰物流） | 123456 |
| CARRIER002 | 承运商（圆通速递） | 123456 |
| FINANCE001 | 财务 | 123456 |

## 功能页面

| 页面 | 说明 |
|------|------|
| 工作台 Dashboard | 托盘统计、押金概览、最近操作记录 |
| 托盘管理 Pallet | 托盘档案维护（财务） |
| 押金管理 Deposit | 押金缴纳、余额查询、缴纳记录 |
| 托盘领用 Pickup | 发货方领用托盘、校验押金 |
| 托盘归还 Return | 承运商归还托盘、自动计算扣款 |
| 扣款管理 Deduction | 扣款记录、人工扣款（财务） |
| 账期管理 Period | 账期开关、控制记录修改权限（财务） |

## 技术栈

- **后端**：Spring Boot 2.7 + MyBatis-Plus + H2/MySQL + Lombok + Validation
- **前端**：Vue 3 + Vite + Element Plus + Vue Router + Axios
- **容器**：Docker + Docker Compose + Nginx
