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
     * appId
     */
    private String appId;
    /**
     * 面向代理端端端口
     */
    private Integer faceProxyPort;
    /**
     * 面向代理端通道
     */
    private Channel faceProxyChannel;
    /**
     * 面向用户端端口
     */
    private Integer faceServerPort;
    /**
     * 面向用户端通道
     */
    private Channel faceServerChannel;

    
    public static ProxyChannel buildFaceProxy(String appId, int faceProxyPort, Channel faceProxyChannel, int faceServerPort) {
    	return new ProxyChannel(appId, faceProxyPort, faceProxyChannel, faceServerPort, null);
    }
}
