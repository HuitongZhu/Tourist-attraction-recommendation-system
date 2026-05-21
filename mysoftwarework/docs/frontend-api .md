# VoyageLink 前端对接文档

## 1. 基础信息

- 后端基础地址：`http://localhost:8080`
- Swagger 地址：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 2. 鉴权规则

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/sms/send`

以上接口无需登录。

除上面两个接口外，其他 `/api/**` 接口都需要在请求头中携带：

```http
Authorization: Bearer <token>
```

管理员接口还要求当前用户角色为 `ADMIN`：

- `/api/admin/**`

默认管理员账号：

- 用户名：`admin`
- 密码：`admin123`

## 3. 通用响应格式

所有接口都返回统一 JSON：

```json
{
  "success": true,
  "message": "Login success.",
  "data": {}
}
```

字段说明：

- `success`：是否成功
- `message`：提示信息
- `data`：业务数据，失败时通常为 `null`

## 4. 分页响应格式

列表分页接口的 `data` 结构如下：

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0
}
```

字段说明：

- `content`：当前页数据
- `page`：当前页码，从 `0` 开始
- `size`：每页条数
- `totalElements`：总条数
- `totalPages`：总页数

## 5. 枚举值

### 5.1 审核状态

- `未审核`
- `审核通过`
- `审核未通过`

### 5.2 互动目标类型 `InteractionTargetType`

- `LANDSCAPE`
- `POST`

## 6. 错误处理

常见状态码：

- `200`：请求成功
- `400`：参数错误或业务校验失败
- `401`：未登录或 token 无效
- `403`：无权限
- `405`：请求方法错误
- `500`：服务端异常

常见错误响应示例：

```json
{
  "success": false,
  "message": "Authentication required.",
  "data": null
}
```

```json
{
  "success": false,
  "message": "Access denied.",
  "data": null
}
```

```json
{
  "success": false,
  "message": "phone: must match \"^1\\d{10}$\"",
  "data": null
}
```

## 7. 接口清单

---

## 7.1 认证模块

### 7.1.1 注册

- 方法：`POST`
- 路径：`/api/auth/register`
- 鉴权：否

请求体：

```json
{
  "username": "test001",
  "phone": "13800138000",
  "password": "123456",
  "confirmPassword": "123456",
  "smsCode": "123456",
  "realName": "张三",
  "idNumber": "110101199901011234",
  "gender": "男",
  "birthday": "1999-01-01"
}
```

字段说明：

- `username`：必填，最大 50
- `phone`：必填，必须是 `1` 开头的 11 位手机号
- `password`：必填，长度 6 到 64
- `confirmPassword`：必填，长度 6 到 64，必须与 `password` 一致
- `smsCode`：必填，6 位短信验证码，有效期 5 分钟
- `realName`：可选
- `idNumber`：可选
- `gender`：可选
- `birthday`：可选，格式 `yyyy-MM-dd`

成功响应：

```json
{
  "success": true,
  "message": "Register success.",
  "data": {
    "userId": "U20260518143000123",
    "username": "test001",
    "phone": "13800138000",
    "role": "USER",
    "token": "eyJ..."
  }
}
```

### 7.1.2 发送短信验证码

- 方法：`POST`
- 路径：`/api/auth/sms/send`
- 鉴权：否

请求体：

```json
{
  "phone": "13800138000"
}
```

说明：

- 后端生成 6 位数字验证码
- 验证码有效期 5 分钟
- 注册使用成功后验证码立即失效

成功响应：

```json
{
  "success": true,
  "message": "SMS sent.",
  "data": {
    "phone": "13800138000",
    "smsCode": "123456",
    "expiresInSeconds": 300
  }
}
```

### 7.1.3 登录

- 方法：`POST`
- 路径：`/api/auth/login`
- 鉴权：否

请求体：

密码登录：

```json
{
  "account": "test001",
  "password": "123456"
}
```

短信验证码登录：

```json
{
  "account": "13800138000",
  "smsCode": "123456"
}
```

说明：

- 密码登录时，`account` 为用户名，必须传 `password`
- 短信验证码登录时，`account` 为手机号，必须传 `smsCode`
- 两种方式只能选一种，不要同时传 `password` 和 `smsCode`
- `smsCode` 需要先调用 `POST /api/auth/sms/send` 获取
- 登录成功后短信验证码立即失效

成功响应：

```json
{
  "success": true,
  "message": "Login success.",
  "data": {
    "userId": "U20260518143000123",
    "username": "test001",
    "phone": "13800138000",
    "role": "USER",
    "token": "eyJ..."
  }
}
```

---

## 7.2 用户信息模块

对应活动图：查看用户信息 → 修改个人资料 / 修改密码（当前密码或短信验证码二选一验证身份）。

### 7.2.1 查看用户信息

- 方法：`GET`
- 路径：`/api/users/me`
- 鉴权：是

成功响应：

```json
{
  "success": true,
  "message": "查询成功。",
  "data": {
    "userId": "U20260518143000123",
    "username": "test001",
    "phone": "13800138000",
    "realName": "张三",
    "idNumber": "110101199901011234",
    "gender": "男",
    "birthday": "1999-01-01",
    "registerTime": "2026-05-18T14:30:00"
  }
}
```

### 7.2.2 修改个人资料

- 方法：`PUT`
- 路径：`/api/users/me`
- 鉴权：是

请求体（字段均可选，只更新传入的字段）：

```json
{
  "realName": "张三",
  "idNumber": "110101199901011234",
  "gender": "男",
  "birthday": "1999-01-01"
}
```

成功响应：`message` 为 `信息更新成功。`，`data` 为更新后的用户资料（结构同 7.2.1）。

### 7.2.3 发送修改密码验证码

- 方法：`POST`
- 路径：`/api/users/me/sms/send`
- 鉴权：是

说明：

- 向当前用户绑定的手机号发送 6 位验证码，有效期 5 分钟
- 用于修改密码时的「手机验证」分支
- 也可继续使用公开的 `POST /api/auth/sms/send`（需自行传入绑定手机号）

成功响应结构与 7.1.2 相同。

### 7.2.4 修改密码

- 方法：`POST`
- 路径：`/api/users/me/password`
- 鉴权：是

**方式一：当前密码验证**

```json
{
  "currentPassword": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}
```

**方式二：短信验证码验证**

```json
{
  "smsCode": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}
```

说明：

- `currentPassword` 与 `smsCode` 只能二选一
- `newPassword`、`confirmPassword` 长度均为 6 到 64，且必须一致
- 短信验证码需先调用 7.2.3 或 7.1.2 获取；验证成功后立即失效

常见业务错误：

| 场景 | `message` |
|------|-----------|
| 当前密码错误 | `密码错误` |
| 验证码错误或不存在 | `验证码错误` |
| 验证码过期 | `验证码已过期` |
| 新密码长度不符 | `密码格式错误` |
| 两次新密码不一致 | `两次密码不一致` |
| 未选择验证方式或同时传两种 | `当前密码验证和短信验证码验证只能选择一种方式。` |

成功响应：

```json
{
  "success": true,
  "message": "密码修改成功。",
  "data": null
}
```

---

## 7.3 景点模块

### 7.2.1 新建景点

- 方法：`POST`
- 路径：`/api/landscapes`
- 鉴权：是

请求体：

```json
{
  "title": "西湖",
  "content": "适合春天游玩",
  "address": "浙江省杭州市西湖区",
  "latitude": 30.243,
  "longitude": 120.150,
  "contactPhone": "0571-12345678",
  "openingTime": "08:00-18:00",
  "level": "5A"
}
```

字段说明：

- `title`：必填
- `content`：必填
- `address`：必填
- `latitude`：可选
- `longitude`：可选
- `contactPhone`：可选
- `openingTime`：可选
- `level`：可选

成功响应中的 `data` 为 `LandscapeResponse`。

### 7.2.2 查询景点列表

- 方法：`GET`
- 路径：`/api/landscapes`
- 鉴权：是

查询参数：

- `keyword`：可选，关键字搜索
- `page`：可选，默认 `0`
- `size`：可选，默认 `10`

示例：

```http
GET /api/landscapes?keyword=西湖&page=0&size=10
```

成功响应中的 `data` 为 `PageResponse<LandscapeResponse>`。

### 7.2.3 景点详情

- 方法：`GET`
- 路径：`/api/landscapes/{id}`
- 鉴权：是

### 7.2.4 删除景点

- 方法：`DELETE`
- 路径：`/api/landscapes/{id}`
- 鉴权：是

### 7.2.5 修改景点

- 方法：`PUT`
- 路径：`/api/landscapes/{id}`
- 鉴权：是
- 权限：只有景点创建者或管理员可以修改

请求体：

```json
{
  "title": "西湖",
  "content": "适合春天游玩",
  "address": "浙江省杭州市西湖区",
  "latitude": 30.243,
  "longitude": 120.150,
  "contactPhone": "0571-12345678",
  "openingTime": "08:00-18:00",
  "level": "5A"
}
```

字段说明：
- 所有字段均可选
- 只更新传入的字段
- 如果传入 `address`，且 `latitude` 或 `longitude` 为 `null`，或二者都为 `0`，后端会根据 `address` 自动补充坐标
- 修改成功后景点状态重置为 `未审核`，需要管理员重新审核

成功响应中的 `data` 为更新后的 `LandscapeResponse`。

---

## 7.3 帖子模块

### 7.3.1 新建帖子

- 方法：`POST`
- 路径：`/api/posts`
- 鉴权：是

请求体：

```json
{
  "landscapeId": "L202605180001",
  "title": "西湖一日游攻略",
  "tag": "攻略",
  "content": "早上先去断桥...",
  "imageUrls": [
    "https://example.com/1.jpg",
    "https://example.com/2.jpg"
  ]
}
```

字段说明：

- `landscapeId`：可选
- `title`：必填
- `tag`：可选
- `content`：必填
- `imageUrls`：可选，字符串数组

成功响应中的 `data` 为 `PostResponse`。

### 7.3.2 查询帖子列表

- 方法：`GET`
- 路径：`/api/posts`
- 鉴权：是

查询参数：

- `page`：可选，默认 `0`
- `size`：可选，默认 `10`

成功响应中的 `data` 为 `PageResponse<PostResponse>`。

### 7.3.3 帖子详情

- 方法：`GET`
- 路径：`/api/posts/{id}`
- 鉴权：是

### 7.3.4 删除帖子

- 方法：`DELETE`
- 路径：`/api/posts/{id}`
- 鉴权：是

---

## 7.4 评论模块

### 7.4.1 发表评论

- 方法：`POST`
- 路径：`/api/comments`
- 鉴权：是

请求体：

景点评论：

```json
{
  "landscapeId": "L202605180001",
  "postId": null,
  "content": "风景很好"
}
```

帖子评论：

```json
{
  "landscapeId": null,
  "postId": "P202605180001",
  "content": "这篇攻略很实用"
}
```

字段说明：

- `landscapeId` 和 `postId` 二选一
- `content`：必填

成功响应中的 `data` 为 `CommentResponse`。

### 7.4.2 查询评论列表

- 方法：`GET`
- 路径：`/api/comments`
- 鉴权：是

查询参数：

- `landscapeId`：可选
- `postId`：可选
- `page`：可选，默认 `0`
- `size`：可选，默认 `10`

说明：

- 一般传 `landscapeId` 或 `postId` 其中一个

成功响应中的 `data` 为 `PageResponse<CommentResponse>`。

### 7.4.3 删除评论

- 方法：`DELETE`
- 路径：`/api/comments/{id}`
- 鉴权：是

---

## 7.5 点赞收藏模块

### 7.5.1 添加收藏

- 方法：`POST`
- 路径：`/api/interactions/favorites`
- 鉴权：是

请求体：

收藏景点：

```json
{
  "targetType": "LANDSCAPE",
  "landscapeId": "L202605180001",
  "postId": null,
  "linkUrl": "http://localhost:5173/landscapes/L202605180001"
}
```

收藏帖子：

```json
{
  "targetType": "POST",
  "landscapeId": null,
  "postId": "P202605180001",
  "linkUrl": "http://localhost:5173/posts/P202605180001"
}
```

成功响应中的 `data` 为：

```json
{
  "id": "F202605180001",
  "targetType": "LANDSCAPE",
  "landscapeId": "L202605180001",
  "postId": null,
  "linkUrl": "http://localhost:5173/landscapes/L202605180001"
}
```

### 7.5.2 取消收藏

- 方法：`DELETE`
- 路径：`/api/interactions/favorites/{id}`
- 鉴权：是

说明：

- `{id}` 是收藏记录 ID，不是景点或帖子 ID

### 7.5.3 添加点赞

- 方法：`POST`
- 路径：`/api/interactions/likes`
- 鉴权：是

请求体与收藏一致：

```json
{
  "targetType": "POST",
  "landscapeId": null,
  "postId": "P202605180001",
  "linkUrl": "http://localhost:5173/posts/P202605180001"
}
```

成功响应中的 `data` 为：

```json
{
  "id": "K202605180001",
  "targetType": "POST",
  "landscapeId": null,
  "postId": "P202605180001",
  "linkUrl": "http://localhost:5173/posts/P202605180001"
}
```

### 7.5.4 取消点赞

- 方法：`DELETE`
- 路径：`/api/interactions/likes/{id}`
- 鉴权：是

说明：

- `{id}` 是点赞记录 ID，不是景点或帖子 ID

---

## 7.6 地图模块

### 7.6.1 地址转坐标 GET

- 方法：`GET`
- 路径：`/api/maps/geocode`
- 鉴权：是

查询参数：

- `address`：必填

示例：

```http
GET /api/maps/geocode?address=浙江省杭州市西湖区
```

成功响应：

```json
{
  "success": true,
  "message": "Geocode success.",
  "data": {
    "address": "浙江省杭州市西湖区",
    "latitude": 30.243,
    "longitude": 120.150,
    "provider": "AMAP"
  }
}
```

### 7.6.2 地址转坐标 POST

- 方法：`POST`
- 路径：`/api/maps/geocode`
- 鉴权：是

请求体：

```json
{
  "address": "浙江省杭州市西湖区"
}
```

---

## 7.7 管理员模块

以下接口均要求：

- 已登录
- 当前用户角色为 `ADMIN`

### 7.7.1 普通用户列表

- 方法：`GET`
- 路径：`/api/admin/users`

说明：仅返回普通用户（`userType = 2`），不包含管理员账号。

查询参数：

- `page`：可选，默认 `0`
- `size`：可选，默认 `10`

成功响应 `data` 为分页结构，`content` 元素结构同 7.2.1 用户资料。

### 7.7.2 查看普通用户详情

- 方法：`GET`
- 路径：`/api/admin/users/{id}`

路径参数：

- `id`：普通用户 `userId`

成功响应 `data` 结构同 7.2.1。

### 7.7.3 修改普通用户资料

- 方法：`PUT`
- 路径：`/api/admin/users/{id}`

请求体（字段均可选，只更新传入的字段）：

```json
{
  "realName": "张三",
  "idNumber": "110101199901011234",
  "gender": "男",
  "birthday": "1999-01-01"
}
```

成功响应：`message` 为 `用户信息更新成功。`，`data` 为更新后的用户资料。

### 7.7.4 景点列表（全部审核状态）

- 方法：`GET`
- 路径：`/api/admin/landscapes`

说明：返回数据库中全部景点，包含 `未审核`、`审核通过`、`审核未通过` 等所有审核状态。

查询参数：

- `page`：可选，默认 `0`
- `size`：可选，默认 `10`

成功响应 `data` 为分页结构，`content` 元素结构同 8.2 `LandscapeResponse`。

### 7.7.5 推荐帖列表（全部审核状态）

- 方法：`GET`
- 路径：`/api/admin/posts`

说明：返回数据库中全部推荐帖，包含所有审核状态。

查询参数：

- `page`：可选，默认 `0`
- `size`：可选，默认 `10`

成功响应 `data` 为分页结构，`content` 元素结构同 8.3 `PostResponse`。

### 7.7.6 审核景点

- 方法：`PATCH`
- 路径：`/api/admin/landscapes/{id}/audit`

请求体：

```json
{
  "approved": true,
  "remark": "审核通过"
}
```

审核后 `auditState` 为 `审核通过` 或 `审核未通过`。

### 7.7.7 审核推荐帖

- 方法：`PATCH`
- 路径：`/api/admin/posts/{id}/audit`

请求体：

```json
{
  "approved": false,
  "remark": "内容需要修改"
}
```

审核后 `auditState` 为 `审核通过` 或 `审核未通过`。

---

## 8. 主要响应对象结构

### 8.1 `UserSummary`

```json
{
  "id": "U202605180001",
  "username": "test001",
  "role": "USER"
}
```

### 8.2 `LandscapeResponse`

```json
{
  "id": "L202605180001",
  "title": "西湖",
  "content": "适合春天游玩",
  "address": "浙江省杭州市西湖区",
  "latitude": 30.243,
  "longitude": 120.150,
  "contactPhone": "0571-12345678",
  "openingTime": "08:00-18:00",
  "level": "5A",
  "status": "审核通过",
  "auditRemark": "通过",
  "publishedAt": "2026-05-18T14:30:00",
  "auditedAt": "2026-05-18T14:40:00",
  "creator": {
    "id": "U202605180001",
    "username": "test001",
    "role": "USER"
  }
}
```

### 8.3 `PostResponse`

```json
{
  "id": "P202605180001",
  "title": "西湖一日游攻略",
  "tag": "攻略",
  "content": "早上先去断桥...",
  "imageUrls": [
    "https://example.com/1.jpg"
  ],
  "status": "审核通过",
  "auditRemark": "通过",
  "publishedAt": "2026-05-18T14:30:00",
  "auditedAt": "2026-05-18T14:40:00",
  "landscapeId": "L202605180001",
  "author": {
    "id": "U202605180001",
    "username": "test001",
    "role": "USER"
  }
}
```

### 8.4 `CommentResponse`

```json
{
  "id": "C202605180001",
  "content": "风景很好",
  "targetType": "LANDSCAPE",
  "status": "审核通过",
  "auditRemark": "通过",
  "publishedAt": "2026-05-18T14:30:00",
  "auditedAt": "2026-05-18T14:40:00",
  "landscapeId": "L202605180001",
  "postId": null,
  "author": {
    "id": "U202605180001",
    "username": "test001",
    "role": "USER"
  }
}
```

## 9. 前端对接建议

- 建议统一封装 `baseURL = http://localhost:8080`
- 建议在请求拦截器中自动附加 `Authorization`
- 建议统一判断 `success`，不要只看 HTTP 状态码
- 分页页码从 `0` 开始，不是从 `1` 开始
- 发表评论、点赞、收藏时，目标对象通常是二选一，不要同时传 `landscapeId` 和 `postId`
