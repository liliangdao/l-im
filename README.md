# l-im解决方案

#### 介绍
l-im 是 lld 基于 netty 的即时通讯服务。由lld维护和支持。

# 功能和特性
- 私有化协议，可自行修改协议规则节省流量和数据包大小。
- 全平台客户端，四端同时在线（移动端，pc端，web端和小程序端），数据和状态多端完美同步。
- 支持自定义多端登录逻辑，单端登录，双端登录，三端登录，全端登录，各端登录解释详见文档。
- API功能全面，适用多场景需求。
- 拥有机器人和频道功能。
- API丰富，方便与其它服务系统的对接。
- 支持会话拓展，不限于置顶，免打扰。
- 性能强大，支持集群部署。


# TODO List
- [X] 项目重构，会剥离出im-tcp和im-server
- [X] 项目定位，明确项目定位，具体是作为im通用组件，还是作为私有的app通信项目
- [ ] 配套app开发。（进行中）



[进度](im-schedule.md ':include')

配套app正内部开发中：
![image](img/index.jpg)
![image](img/mail.jpg)
![image](img/find.jpg)
![image](img/my.jpg)


#### 编译
在安装JDK1.8以上及maven的前提下，在命令行中执行mvn clean package

