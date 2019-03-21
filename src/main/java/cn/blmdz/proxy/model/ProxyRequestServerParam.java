package cn.blmdz.proxy.model;

public class ProxyRequestServerParam {
	/**
	 * appId
	 */
	private String appId;
	/**
	 * 代理指向的客户端本地的端口
	 */
	private Integer port;
	
	public String getAppId() {
		return appId;
	}
	public Integer getPort() {
		return port;
	}
	
	public ProxyRequestServerParam(String appId, Integer port) {
		this.appId = appId;
		this.port = port;
	}
}
