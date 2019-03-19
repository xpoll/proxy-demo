package cn.blmdz.proxy.model;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyChannel {

    /**
     * 面向代理端通道
     */
    private Channel serverChannel;
    /**
     * 面向用户端通道
     */
    private Channel userChannel;
    /**
     * 面向代理端端端口
     */
    private Integer serverPort;
    /**
     * 面向用户端端口
     */
    private Integer userPort;

    public static ProxyChannel buildFaceUser(int userPort, Channel userChannel) {
    	return new ProxyChannel(null, userChannel, null, userPort);
    }
    
    public static ProxyChannel buildFaceServer(int serverPort, Channel serverChannel) {
    	return new ProxyChannel(serverChannel, null, serverPort, null);
    }
}
