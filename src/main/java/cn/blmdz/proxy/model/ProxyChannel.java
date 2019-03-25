package cn.blmdz.proxy.model;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;

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
    private List<Channel> faceServerChannel;

    
    public static ProxyChannel buildFaceProxy(String appId, int faceProxyPort, Channel faceProxyChannel, int faceServerPort) {
    	return new ProxyChannel(appId, faceProxyPort, faceProxyChannel, faceServerPort, null);
    }


	public String getAppId() {
		return appId;
	}


	public Integer getFaceProxyPort() {
		return faceProxyPort;
	}


	public Channel getFaceProxyChannel() {
		return faceProxyChannel;
	}


	public Integer getFaceServerPort() {
		return faceServerPort;
	}


	public List<Channel> getFaceServerChannel() {
		return faceServerChannel;
	}


	public void setFaceServerChannel(Channel faceServerChannel) {
	    if (this.faceServerChannel == null) this.faceServerChannel = new ArrayList<>();
	    this.faceServerChannel.add(faceServerChannel);
	}


	private ProxyChannel(String appId, Integer faceProxyPort, Channel faceProxyChannel, Integer faceServerPort, List<Channel> faceServerChannel) {
		this.appId = appId;
		this.faceProxyPort = faceProxyPort;
		this.faceProxyChannel = faceProxyChannel;
		this.faceServerPort = faceServerPort;
		this.faceServerChannel = faceServerChannel;
	}
}
