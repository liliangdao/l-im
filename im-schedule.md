用户

| 接口                         | 作用             | 备注                                     | 进 度 |
| ---------------------------- | ---------------- | ---------------------------------------- | :---: |
| /user/importUser             | 导入用户         |                                          | 100%  |
| /user/deleteUser             | 删除用户         |                                          | 100%  |
| /user/login                  | 登录             | 返回服务地址                             | 100%  |
| /user/data/getUserInfo       | 获取用户         | 获取用户拓展信息，根据字段获取信息未完成 |  50%  |
| /user/data/getSingleUserInfo | 获取单个用户     | 获取所有的服务列表                       | 100%  |
| /user/data/modifyUserInfo    | 修改用户信息     | 发送tcp通知未完成                        |  100%  |
| /user/qeuryOnlineStatus      | 查询用户在线状态 |                                          |  0%   |

## 好友

| 接口                                    | 作用                 | 备注                | 进 度 |
| --------------------------------------- | -------------------- | ------------------- | :---: |
| /friendship/add                         | 添加好友             | 发送tcp通知未完成   |  100%  |
| /friendship/delete                      | 删除好友             | 发送tcp通知未完成   |  100%  |
| /friendship/update                      | 更新好友资料         | 发送tcp通知未完成   |  100%  |
| /friendship/getRelation                 | 获取单个好友关系信息 |                     |  100%   |
| /friendship/checkFriend                       | 校验是否为好友,支持单向和双向校验       |                     |  100%   |
| /friendship/get                         | 拉取好友             | 分页拉取，按照seq拉 |  0%   |
| /friendship/addBlack                          | 添加黑名单           |                     |  100%   |
| /friendship/deleteBlack                       | 删除黑名单           |                     |  100%   |
| /friendshipRequest/getFriendRequest     | 获取好友请求列表     |                     | 100%  |
| /friendshipRequest/readAllFriendRequest |                      | 发送tcp通知未完成   |  90%  |

## 群组

| 接口                      | 作用                   | 备注                                             | 进 度 |
| ------------------------- | ---------------------- | ------------------------------------------------ | :---: |
| /group/createGroup        | 创建群                 | 发送tcp通知未完成，群的tcp通知要发给所有群成员                                |  90%  |
| /group/get                | 获取群组资料           | 群资料+群成员信息                                |  0%   |
| /group/getAll             | 获取所有群组           | 只支持根据群类型过滤                             |  0%   |
| /group/getGroupMember     | 获取群成员详细资料     | 分页返回                                         |  0%   |
| /group/update             | 修改群基础资料         | tcp通知未完成                                    |  90%  |
| /group/addMember          | 增加群成员             | 发送tcp通知未完成，群的tcp通知要发给所有群成员，添加静默加人                  |  80%  |
| /group/deleteMember       | 删除群成员             |  群的tcp通知要发给所有群成员                                                |  0%   |
| /group/updateMember       | 修改群成员资料         | 修改单个，群的tcp通知要发给所有群成员                                         |  0%   |
| /group/destroyGroup       | 解散群                 | 需要发送tcp通知未完成                            |  90%  |
| /group/getJoinedGroup     | 获取用户所加入的群组   | 支持群类型过滤，分页拉取，分页返回小问题日后修复 |  95%  |
| /group/getRoleInGroup     | 查询用户在群组中的身份 | 支持批量拉取                                     |  0%   |
| /group/forbidSendMsg      | 批量禁言/解禁          | ShutUpTime为0表示解禁言                          |  0%   |
| /group/getGroupShuttedUin | 获取被禁言群成员列表   | 单个群                                           |  0%   |
| /group/changeGroupOwner   | 转让群主               | 转让后的群主必须在群内                           |  0%   |
| /group/import             | 导入群                 | 不会发tcp通知，不会触发回调                      |  0%   |
| /group/importMember       | 导入群成员             | 不会触发回调、不会下发通知。                     |  0%   |

## 会话

| 接口                 | 作用             | 备注                                       | 进 度 |
| -------------------- | ---------------- | ------------------------------------------ | :---: |
| /conversation/get    | 获取会话列表     |                                            |  0%   |
| /conversation/update | 修改会话设置     | 置顶/静音                                  |  0%   |
| /conversation/delete | 删除会话         | 支持服务端删除/客户端删除（仅发送tcp通知） |  0%   |

## 消息

| 接口                       | 作用         | 备注                                                  | 进 度 |
| -------------------------- | ------------ | ----------------------------------------------------- | :---: |
| /message/sendMessage           | 发送单聊消息 |                                                       |  0%   |
| /message/getMessage        | 查询历史消息 |                                                       |  0%   |
| /message/getHistoryMessage | 获取历史消息 |                                                       |  0%   |
| /message/withdrawMessage   | 撤回单聊消息 |                                                       |  0%   |
| 单聊消息发送               |              |                                                       |  100%   |
| 单聊消息已读               |              | 更新会话已读seq，并分发给同步端（视情况分发给接收方） |  100%   |
| 单聊消息接收确认           |              |                                                       |  0%   |

## 群消息

| 接口                       | 作用         | 备注                                                  | 进 度 |
| -------------------------- | ------------ | ----------------------------------------------------- | :---: |
| /group/sendMsg           | 发送单聊消息 |                                                       |  0%   |
| /group/withdrawMessage   | 撤回群聊消息 |                                                       |  0%   |
| 群聊消息发送               |              |                                                       |  100%   |
| 群聊消息已读               |              | 更新会话已读seq，并分发给同步端（视情况分发给接收方） |  0%   |
| 群聊消息接收确认           |              |                                                       |  0%   |

## 综合

| 功能                      | 备注                                    | 进 度 |
| ------------------------- | --------------------------------------- | :---: |
| 单聊发送消息双方校验      | 校验双方是否禁言，禁用，好友，关系链... |  100%  |
| 单聊消息Ack               | ack方法封装                             |  100%  |
| 发送消息给tcp服务方法封装 |                                         |  100%   |
| 消息持久化 |         包含在线和离线消息                                |  100%   |
| 工具类封装 | session工具类，messageProdecer工具...                                        |  100%   |
| 消息服务和tcp服务通信 | mq                                        |  100%   |

## 增量同步

| 功能                      | 备注                                    | 进 度 |
| ------------------------- | --------------------------------------- | :---: |
| friendship/syncFriendShipList      | 增量同步好友 |  100%  |
| friendship/syncFriendShipRequestList               | 增量同步好友申请                             |  0%  |
| friendship/syncBlackList |    增量同步黑名单                                     |  0%   |
| conversation/syncConversationList |        增量同步会话列表                                 |  100%   |
| group/syncJoinedGroup | 增量同步加入的群聊                                        |  100%   |
| message/syncOfflineMessage | 增量同步离线消息                                        |  100%   |





