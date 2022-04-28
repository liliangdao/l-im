
from re import S
import socket
import json
import struct
import threading

def task(scoket):
    while True:
        # datab = scoket.recv(4)
        num = struct.unpack('>I', scoket.recv(4))[0] # 接受包大小并且解析
        command = struct.unpack('>I', scoket.recv(4))[0] # 接受command并且解析
        msgPack = scoket.recv(num)
        # msgPack = msgPack.decode("ascii");
        ftm = str(num) + 's';
        msgPack = struct.unpack(ftm,msgPack)[0]
        print(type(msgPack))
        msgPack = str(msgPack, encoding = "utf-8")
        msg = json.loads(msgPack) # 通过长度获取消息体
        print('\n接收到',msg["fromId"] , '用户发来的数据 ： ', msg["msgBody"])

fromId = input("请登录 : ")
toId = input("请输入和哪个用户进行聊天 : ")

s=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
s.connect(("127.0.0.1",9000))


command = 1000
data={"fromId":fromId,"toId":toId}
jsonData = json.dumps(data)

body = bytes(jsonData, 'utf-8')
body_len = len(jsonData)

lenByte = struct.pack('>I', body_len)
commandByte = struct.pack('>I', command)
s.sendall(lenByte + commandByte + body)


# 创建一个线程专门接收服务端数据并且打印
t = threading.Thread(target=task,args=(s,))
t.start()

while True:
    msgBody = input("请输入要发送的内容 : ")
    command = 8888
    data["msgBody"] = msgBody;
    jsonData = json.dumps(data)
    body = bytes(jsonData, 'utf-8')
    body_len = len(jsonData)
    lenByte = struct.pack('>I', body_len)
    commandByte = struct.pack('>I', command)
    s.sendall(lenByte + commandByte + body)




# print(datab) # hello

# 关闭 socket
# s.close()


# if __name__ == '__main__':
#     client = socket(AF_INET, SOCK_STREAM)
#     # 连接服务端
#     client.connect(('127.0.0.1', 8082))

#     book = Book('成都', 2021)

#     # 将对象book转成JSON格式的数据
#     json = json.dumps(dict(book))
#     # 将JSON格式数据转成字节类型,用于网络传输
#     body = bytes(json, 'utf-8')
#     # 计算实际有效数据(body)的长度
#     body_len = len(body)

#     # head中存body的长度
#     # I表示整型,即用一个整型大小空间存储body的长度; 而>表示按照大端存储, 至于什么是大端小端存储可以Google
#     head = struct.pack('>I', body_len)


#     # 按照我们自定义的协议格式,将字节数据发送到服务端
#     client.sendall(head + body)



def mysend(sk,dic,encoding='utf-8'):
    str_dic = json.dumps(dic)                # 先把dic转成str
    bdic = str_dic.encode(encoding)          # str转成bytes
    dic_len = len(bdic)                      # 计算bytes_dic长度
    bytes_len = struct.pack('i',dic_len)     # 用pack将dic长度变成4
    sk.send(bytes_len)                       # 发送长度
    sk.send(bdic)                            # 发送dic
