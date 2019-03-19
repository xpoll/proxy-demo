package cn.blmdz.proxy.model;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class UserProxy {

    /**
     * 用户服务端通道
     */
    private Channel server;
    /**
     * 用户客户端通道
     */
    private Channel client;
    /**
     * 服务端端口
     */
    private int serverPort;
    /**
     * 客户端端口
     */
    private int clientPort;
}
