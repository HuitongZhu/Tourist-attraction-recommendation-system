# 旅游景点推荐系统

# 1. 项目介绍

这是一款基于位置服务的旅游推荐平台，提供 **Web 端**和 **App 端**双端应用，为用户提供景点发现、游记分享、互动评论等完整旅行体验
**旅游书**是一款 Java + Kotlin 的前后端分离的旅游推荐平台。主要优点是开发、部署简单快捷、界面设计友好、代码结构清晰。支持 Web 端 和 Android App 端 ，能覆盖到 PC 机和手机等设备，为用户提供景点发现、推荐帖分享、互动评论等完整旅行体验。

## 普通用户模块功能

### 普通用户模块
| 功能 | 介绍 |
| --- | --- |
|用户认证|注册（手机号验证码）、登录（密码/短信验证码）、忘记密码|
| 密码管理 | 修改密码（支持当前密码验证或短信验证码验证） | 
| 个人资料 | 查看/编辑个人资料（姓名、身份证号、性别、生日） | 
| 景点浏览 | 查看景点列表、搜索景点、查看景点详情 |
| 景点发布 | 发布新景点（标题、内容、地址、联系方式、开放时间、等级） | 
| 景点管理 | 编辑已发布的景点、删除自己发布的景点 | 
| 推荐帖浏览 | 查看推荐帖列表、查看推荐帖详情 | 
| 推荐帖发布 | 发布新推荐帖（关联景点、标题、标签、内容、图片） | 
| 推荐帖管理 | 编辑/删除自己发布的推荐帖 | | 互动功能 | 收藏景点/帖子、点赞帖子、发表评论（景点/帖子） |

### 管理员模块
| 功能 | 介绍 |
| ---- | ---- | 
| 用户管理 | 查看普通用户列表、查看用户详情、修改用户资料 | 
| 景点审核 | 查看全部景点（包含未审核/审核通过/审核未通过）、审核景点通过或拒绝 | 
| 帖子审核 | 查看全部推荐帖（包含各种审核状态）、审核推荐帖通过或拒绝 | 
| 评论审核 | 评论删除 |

# 2. 快速开始
## 2.1 环境要求

**后端 / Web 端**

- JDK 24+

- Maven 3.6+

- MySQL 8.0+

- Redis

**App 端**

- Android Studio Hedgehog 或更高版本

- Android 设备或模拟器（API 24+）
## 2.2 运行步骤
### 1. 启动后端服务（同步启动redis）
```bash
cd  travel-web

mvn  spring-boot:run
```
后端服务地址：`http://localhost:8080`

### 2. 访问 Web 端

打开浏览器访问：`http://localhost:8080`
  
### 3. 运行 App 端
  
1. 使用 Android Studio 打开 `mysoftwarework` 文件夹

2. 等待 Gradle 同步完成

3. 连接 Android 设备或启动模拟器

4. 运行 `app` 模块

---
## 2.3 配置文件说明

### application.yml 配置详解
配置文件路径：`travel-web/src/main/resources/application.yml`

```yaml
application:

name: travel-web  # 应用名称

datasource:

url: jdbc:mysql://localhost:3306/travel?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&connectTimeout=10000&socketTimeout=30000&allowPublicKeyRetrieval=true

# MySQL 连接地址，指向本地数据库 travel

username: 数据库用户名

password: 数据库密码


server:

port: 服务端口


aliyun:

sms:

enabled: true  # 是否启用短信服务

access-key-id: 阿里云 AccessKey ID

access-key-secret: 阿里云 AccessKey Secret

sign-name: 速通互联验证码  # 签名名称（来自控制台"赠送模板配置"）

template-code: 100001  # 模板 CODE（来自控制台"赠送模板配置"）
```
