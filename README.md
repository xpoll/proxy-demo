# proxy-demo
proxy-demo
one by one 

## 一对一的链接
一个客户端代理的端口只能有一个访问，其他访问暴露的端口会直接断掉

支持同一个appid不同的代理端口请求服务端

### 服务端启动
``
java -cp proxy-demo-0.0.1-SNAPSHOT.jar cn.blmdz.proxy.server.ServerContainer 0.0.0.0:port
``

    0.0.0.0 -> 绑定本地的ip
    port -> 对外提供的端口

### 客户端启动
``
java -cp proxy-demo-0.0.1-SNAPSHOT.jar cn.blmdz.proxy.client.ClientContainer babababa server.com:port 0.0.0.0:port
``

    babababa -> appid唯一标识
    server.com -> 服务端地址
    port[0] -> 服务端暴露的端口
    0.0.0.0 -> 绑定本地的ip
    port[1] -> 需要代理的端口

