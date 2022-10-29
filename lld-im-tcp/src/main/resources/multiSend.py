

import socket
import json
import struct
import threading
import time
import uuid


userId = input("请登录 : ")
toId = input("请输入和哪个用户进行聊天 : ")
imei = str(uuid.uuid1()) + "啊"

s=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
s.connect(("127.0.0.1",9000))


command = 9888
version = 1
clientType = 4
messageType = 0x0
appId = 10000



commandByte = struct.pack('>I', command)
versionByte = struct.pack('>I', version)
messageTypeByte = struct.pack('>I', messageType)
clientTypeByte = struct.pack('>I', clientType)
appIdByte = struct.pack('>I', appId)

imeiBytes = bytes(imei,"utf-8");
imeiLength = len(imeiBytes)
imeiLengthByte = struct.pack('>I', imeiLength)

data = {"userId": userId, "appId": 10000, "clientType": clientType, "imei": imei,"customClientName":"啊啊"};
jsonData = json.dumps(data)
body = bytes(jsonData, 'utf-8')
body_len = len(body)
bodyLenBytes = struct.pack('>I', body_len)
s.sendall(commandByte + versionByte + clientTypeByte + messageTypeByte + appIdByte + imeiLengthByte + bodyLenBytes + imeiBytes + body)
# for x in range(100):
#   s.sendall(commandByte + versionByte + clientTypeByte + messageTypeByte + appIdByte + imeiLengthByte + bodyLenBytes  + imeiBytes + body)    





