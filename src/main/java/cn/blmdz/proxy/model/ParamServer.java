package cn.blmdz.proxy.model;

import lombok.Data;

@Data
public class ParamServer {
	/**
	 * 
	 */
	private String appId;
	/**
	 * 代理端开启的监控实际服务端端口
	 */
	private Integer port;
}
