package cn.blmdz.proxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyRequestServerParam {
	/**
	 * appId
	 */
	private String appId;
	/**
	 * 代理指向的客户端本地的端口
	 */
	private Integer port;
}
