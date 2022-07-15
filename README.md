# l-im解决方案

#### 介绍
l-im 是 lld 基于 netty 的即时通讯服务。由lld维护和支持。

# 功能和特性
- 私有化协议，可自行修改协议规则节省流量和数据包大小。
- 全平台客户端，四端同时在线（移动端，pc端，web端和小程序端），数据和状态多端完美同步。
- API功能全面，适用多场景需求。（待完善）
- 拥有机器人和频道功能。（待完善）

# TODO List
- [ ] 项目重构，会剥离出im-tcp和im-server
- [ ] 项目定位，明确项目定位，具体是作为im通用组件，还是作为私有的app通信项目
- [ ]   ....

[进度](im-schedule.md ':include')



## 用户

| 接口                         | 作用             | 备注                                     | 进 度 |
| ---------------------------- | ---------------- | ---------------------------------------- | :---: |
| <font color=red>/user/importUser</font>             | 导入用户         |                                          | 100%  |
| /user/deleteUser             | 删除用户         |                                          | 100%  |
| /user/login                  | 登录             | 返回服务地址                             | 100%  |
| /user/data/getUserInfo       | 获取用户         | 获取用户拓展信息，根据字段获取信息未完成 |  50%  |
| /user/data/getSingleUserInfo | 获取单个用户     | 获取所有的服务列表                       | 100%  |
| /user/data/modifyUserInfo    | 修改用户信息     | 发送tcp通知未完成                        |  90%  |
| /user/data/syncUserData      | 增量获取用户信息 |                                          |  0%   |
| /user/qeuryOnlineStatus      | 查询用户在线状态 |                                          |  0%   |

## 好友
| 接口                                    | 作用                 | 备注                | 进 度 |
| --------------------------------------- | -------------------- | ------------------- | :---: |
| /friendship/add                         | 添加好友             | 发送tcp通知未完成   |  90%  |
| /friendship/delete                      | 删除好友             | 发送tcp通知未完成   |  90%  |
| /friendship/sync                        | 增量获取好友信息     |                     |  0%   |
| /friendship/update                      | 更新好友资料         | 发送tcp通知未完成   |  90%  |
| /friendship/getRelation                 | 获取单个好友关系信息 |                     |  0%   |
| /friendship/check                       | 校验是否为好友       |                     |  0%   |
| /friendship/get                         | 拉取好友             | 分页拉取，按照seq拉 |  0%   |
| /blackList/add                          | 添加黑名单           |                     |  0%   |
| /blackList/delete                       | 删除黑名单           |                     |  0%   |
| /blackList/sync                         | 增量获取黑名单信息   |                     |  0%   |
| /friendshipRequest/getFriendRequest     | 获取好友请求列表     |                     | 100%  |
| /friendshipRequest/readAllFriendRequest |                      | 发送tcp通知未完成   |  90%  |


## 群组
| 接口                      | 作用                   | 备注                            | 进 度 |
| ------------------------- | ---------------------- | ------------------------------- | :---: |
| /group/createGroup        | 创建群                 | 发送tcp通知未完成               |  90%  |
| /group/get                | 获取群组资料           | 群资料+群成员信息               |  0%   |
| /group/getAll             | 获取所有群组           | 只支持根据群类型过滤            |  0%   |
| /group/getGroupMember     | 获取群成员详细资料     | 分页返回                        |  0%   |
| /group/update            | 修改群基础资料         | tcp通知未完成                      |  90%   |
| /group/addMember          | 增加群成员             | 发送tcp通知未完成，添加静默加人 |  80%  |
| /group/deleteMember       | 删除群成员             |                                 |  0%   |
| /group/updateMember       | 修改群成员资料         | 修改单个                        |  0%   |
| /group/destroyGroup       | 解散群                 | 需要发送tcp通知未完成                 |  90%   |
| /group/getJoinedGroup     | 获取用户所加入的群组   | 支持群类型过滤，分页拉取，分页返回小问题日后修复        |  95%   |
| /group/getRoleInGroup     | 查询用户在群组中的身份 | 支持批量拉取                    |  0%   |
| /group/forbidSendMsg      | 批量禁言/解禁          | ShutUpTime为0表示解禁言         |  0%   |
| /group/getGroupShuttedUin | 获取被禁言群成员列表   | 单个群                          |  0%   |
| /group/changeGroupOwner   | 转让群主               | 转让后的群主必须在群内          |  0%   |
| /group/import             | 导入群                 | 不会发tcp通知，不会触发回调     |  0%   |
| /group/importMember       | 导入群成员             | 不会触发回调、不会下发通知。    |  0%   |

## 会话

| 接口                 | 作用             | 备注                                       | 进 度 |
| -------------------- | ---------------- | ------------------------------------------ | :---: |
| /conversation/get    | 获取会话列表     |                                            |  0%   |
| /conversation/sync   | 增量同步会话列表 |                                            |  0%   |
| /conversation/update | 修改会话设置     | 置顶/静音                                  |  0%   |
| /conversation/delete | 删除会话         | 支持服务端删除/客户端删除（仅发送tcp通知） |  0%   |

## 消息

| 接口                       | 作用         | 备注 | 进 度 |
| -------------------------- | ------------ | ---- | :---: |
| /message/sendMsg           | 发送单聊消息 |      |  0%   |
| /message/getMessage        | 查询历史消息 |      |  0%   |
| /message/getOfflineMessage | 获取离线消息 |      |  0%   |
| /message/getHistoryMessage | 获取历史消息 |      |  0%   |
| /message/withdrawMessage   | 撤回单聊消息 |      |  0%   |

## TCP

| 接口 | 作用 | 备注 | 进 度 |
| ---- | ---- | ---- | :---: |
|      |      |      |       |
|      |      |      |       |
|      |      |      |       |
|      |      |      |       |
|      |      |      |       |



#### 编译
在安装JDK1.8以上及maven的前提下，在命令行中执行mvn clean package

