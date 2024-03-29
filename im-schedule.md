用户

| 接口                         | 作用             | 备注                                     | 进 度 |
| ---------------------------- | ---------------- | ---------------------------------------- | :---: |
| /user/importUser             | 导入用户         |                                          | 100%  |
| /user/deleteUser             | 删除用户         |                                          | 100%  |
| /user/login                  | 登录             | 返回服务地址                             | 100%  |
| /user/data/getUserInfo       | 获取用户         | 获取用户拓展信息 |  100%  |
| /user/data/getSingleUserInfo | 获取单个用户     | 获取所有的服务列表                       | 100%  |
| /user/data/modifyUserInfo    | 修改用户信息     | 发送tcp通知未完成                        |  100%  |
| /user/qeuryOnlineStatus      | 查询用户在线状态 |                                          |  100%   |

## 好友

| 接口                                    | 作用                 | 备注                | 进 度 |
| --------------------------------------- | -------------------- | ------------------- | :---: |
| /friendship/add                         | 添加好友             |    |  100%  |
| /friendship/delete                      | 删除好友             |    |  100%  |
| /friendship/update                      | 更新好友资料         |    |  100%  |
| /friendship/getRelation                 | 获取单个好友关系信息 |                     |  100%   |
| /friendship/checkFriend                       | 校验是否为好友,支持单向和双向校验       |                     |  100%   |
| /friendship/addBlack                          | 添加黑名单           |                     |  100%   |
| /friendship/deleteBlack                       | 删除黑名单           |                     |  100%   |
| /friendshipRequest/getFriendRequest     | 获取好友请求列表     |                     | 100%  |
| /friendshipRequest/readAllFriendRequest |                      |    |  100%  |
| /friendship/group/add |  添加好友分组                    |    |  100%  |
| /friendship/group/del |  删除好友分组                    |    |  100%  |
| /friendship/group/member/add |  好友分组添加用户                    |    |  100%  |
| /friendship/group/member/del |  好友分组删除用户                    |    |  100%  |


## 群组

| 接口                      | 作用                   | 备注                                             | 进 度 |
| ------------------------- | ---------------------- | ------------------------------------------------ | :---: |
| /group/createGroup        | 创建群                 | 发送tcp通知未完成，群的tcp通知要发给所有群成员                                |  100%  |
| /group/get                | 获取群组资料           | 群资料+群成员信息                                |  100%   |
| /group/getAll             | 获取所有群组           | 只支持根据群类型过滤                             |  0%   |
| /group/getGroupMember     | 获取群成员详细资料     | 分页返回                                         |  0%   |
| /group/update             | 修改群基础资料         |                                    |  100%  |
| /group/addMember          | 增加群成员             |                   |  100%  |
| /group/deleteMember       | 删除群成员             |  群的tcp通知要发给所有群成员                                                |  100%   |
| /group/updateMember       | 修改群成员资料         | 修改单个                                         |  100%   |
| /group/destroyGroup       | 解散群                 | 需要发送tcp通知未完成                            |  100%  |
| /group/getJoinedGroup     | 获取用户所加入的群组   | 支持群类型过滤，分页拉取，分页返回小问题日后修复 |  100%  |
| /group/getRoleInGroup     | 查询用户在群组中的身份 | 支持批量拉取                                     |  0%   |
| /group/forbidSendMessage      | 批量禁言/解禁          | ShutUpTime为0表示解禁言                          |  100%   |
| /group/getGroupShuttedMember | 获取被禁言群成员列表   | 单个群                                           |  0%   |
| /group/transferGroup   | 转让群主               | 转让后的群主必须在群内                           |  100%   |
| /group/import             | 导入群                 | 不会发tcp通知，不会触发回调                      |  100%   |
| /group/importMember       | 导入群成员             | 不会触发回调、不会下发通知。                     |  100%   |

## 会话

| 接口                 | 作用             | 备注                                       | 进 度 |
| -------------------- | ---------------- | ------------------------------------------ | :---: |
| /conversation/get    | 获取会话列表     |                                            |  0%   |
| /conversation/update | 修改会话设置     | 置顶/静音                                  |  0%   |
| /conversation/delete | 删除会话         | 支持服务端删除/客户端删除（仅发送tcp通知） |  0%   |

## 消息

| 接口                       | 作用         | 备注                                                  | 进 度 |
| -------------------------- | ------------ | ----------------------------------------------------- | :---: |
| /message/sendMessage           | 发送单聊消息 |                                                       |  100%   |
| /message/getMessage        | 查询历史消息 |                                                       |  0%   |
| /message/getHistoryMessage | 获取历史消息 |                                                       |  0%   |
| /message/withdrawMessage   | 撤回单聊消息 |                                                       |  100%   |
| 单聊消息发送               |              |                                                       |  100%   |
| 单聊消息已读               |              | 更新会话已读seq，并分发给同步端（视情况分发给接收方） |  100%   |
| 单聊消息接收确认           |              |                                                       |  100%   |

## 群消息

| 接口                       | 作用         | 备注                                                  | 进 度 |
| -------------------------- | ------------ | ----------------------------------------------------- | :---: |
| /group/sendMessage           | 发送群聊消息 |                                                       |  100%   |
| /group/withdrawMessage   | 撤回群聊消息 |                                                       |  100%   |
| 群聊消息发送               |              |                                                       |  100%   |

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
| friendship/syncFriendShipRequestList               | 增量同步好友申请                             |  100%  |
| friendship/syncBlackList |    增量同步黑名单                                     |  0%   |
| conversation/syncConversationList |        增量同步会话列表                                 |  100%   |
| group/syncJoinedGroup | 增量同步加入的群聊                                        |  100%   |
| message/syncOfflineMessage | 增量同步离线消息                                        |  100%   |





