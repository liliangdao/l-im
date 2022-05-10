

import socket
import json
import struct
import threading
import time
import uuid


userId = input("请登录 : ")
toId = input("请输入和哪个用户进行聊天 : ")
imei = str(uuid.uuid1())


def doPing(scoket):
    command = 9998
    data={"userId":"123"}
    jsonData = json.dumps(data)
    body = bytes(jsonData, 'utf-8')
    body_len = len(jsonData)
    lenByte = struct.pack('>I', body_len)
    commandByte = struct.pack('>I', command)
    scoket.sendall(lenByte + commandByte + body)

def ping(scoket):
    while True:
        time.sleep(3)
        doPing(scoket)


def task(s):
    # print("task开始")
    while True:
        # datab = scoket.recv(4)
        num = struct.unpack('>I', s.recv(4))[0] # 接受包大小并且解析
        command = struct.unpack('>I', s.recv(4))[0] # 接受command并且解析
        print(command)
        if command == 2000 :
            print("收到下线通知")
            s.close()
            break

        msgPack = s.recv(num)
        # print(msgPack)
        # msgPack = msgPack.decode("ascii");
        ftm = str(num) + 's'
        msgPack = struct.unpack(ftm,msgPack)[0]
        msgPack = str(msgPack, encoding = "utf-8")
        msg = json.loads(msgPack) # 通过长度获取消息体

        if(msg["userId"] == "system"):
            print('\n系统 ： ', msg["data"])
        elif (msg["userId"] == userId):
            msgBody = json.loads(msg["data"])
            print('\n自己：', msgBody["msgBody"])
            # print(msg["data"])
        else:
            msgBody = json.loads(msg["data"])
            print('\n',msg["userId"] , '： ', msgBody["msgBody"])


s=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
s.connect(("127.0.0.1",9000))


command = 1000
data={"userId":userId,"toId":toId,"appId":10000,"clientType":4,"imei":imei}
dataObj = data
data["data"] = {"userId":userId,"toId":toId,"appId":10000,"clientType":4,"imei":imei}

jsonData = json.dumps(data)

body = bytes(jsonData, 'utf-8')
body_len = len(jsonData)

lenByte = struct.pack('>I', body_len)
commandByte = struct.pack('>I', command)
s.sendall(lenByte + commandByte + body)

t1 = threading.Thread(target=task,args=(s,))
# 创建一个线程专门接收服务端数据并且打印
t2 = threading.Thread(target=ping,args=(s,))

t1.start()
t2.start()

while True:
    msgBody = input("请输入要发送的内容 : ")
    command = 8888
    data["data"]["msgBody"] = msgBody
    jsonData = json.dumps(data)
    body = bytes(jsonData, 'utf-8')
    body_len = len(jsonData)
    lenByte = struct.pack('>I', body_len)
    commandByte = struct.pack('>I', command)
    s.sendall(lenByte + commandByte + body)



