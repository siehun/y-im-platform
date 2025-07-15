# **分布式IM即时通讯系统**



## 项目简介

[IM在线地址](http://im.siehun.top)

[文档](https://www.yuque.com/seihun/pax21g)

分布式IM即时通讯系统本质上就是对线上聊天和用户的管理，针对聊天本身来说，最核心的需求就是：**发送文字、表情、图片、文件、语音、消息缓存、消息存储、消息未读、已读、撤回，离线消息、历史消息、单聊、群聊，多端同步。**

对用户管理来说，存在的需求包含：**添加好友、查看还有列表、删除好友、查看好友信息、创建群聊、加入群聊、查看群成员信息、@群成员、退出群聊、修改群昵称、拉人进群、踢人出群、解散群聊、填写群公告、修改群备注以及其他用户相关的需求等。**

## 技术选型

- 开发框架：SpringBoot、SpringCloud、SpringCloud Alibaba、Dubbo。
- 缓存：Redis分布式缓存
- 数据库：MySQL
- 持久层框架：MyBatis、Mybatis-Plus。
- 服务配置、服务注册与发现：Nacos。
- 消息中间件：RocketMQ。
- 网络通信：Netty。
- 文件存储：Minio。

## 功能页面展示

#### 文字、表情、图片、文件的发送

![image.png](https://cdn.nlark.com/yuque/0/2025/png/50672796/1752573787753-4b0e5e46-269a-4d54-86f1-d293cbede6f4.png?x-oss-process=image%2Fformat%2Cwebp)

#### 单聊展示

![image.png](https://cdn.nlark.com/yuque/0/2025/png/50672796/1752573686891-cf0aadc3-55ee-425b-acb0-98865cd8dde2.png?x-oss-process=image%2Fformat%2Cwebp)

#### 群聊展示

![image.png](https://cdn.nlark.com/yuque/0/2025/png/50672796/1752573573133-8645b1da-b8e2-4310-b0f3-4a514efe84e9.png?x-oss-process=image%2Fformat%2Cwebp)