package cn.blmdz.proxy.model;

import com.alibaba.fastjson.JSON;

public class ProxyParam {
    
	/**
	 * 渠道号
	 */
	private Long id;
    /**
     * APPID
     */
    private String appId;
    /**
     * 内部HOST
     */
    private String localHost;
	/**
	 * 内部端口
	 */
	private Integer localPort;
    /**
     * 外部HOST
     */
    private String externalHost;
	/**
	 * 外部端口
	 */
	private Integer externalPort;
	
	public static ProxyParam build(String appId, String localHost, Integer localPort) {
	    ProxyParam param = new ProxyParam();
	    param.setAppId(appId);
	    param.setLocalHost(localHost);
	    param.setLocalPort(localPort);
	    return param;
	}
	

    public static ProxyParam build(String externalHost, Integer externalPort) {
        ProxyParam param = new ProxyParam();
        param.setExternalHost(externalHost);
        param.setExternalPort(externalPort);
        return param;
    }
    
    public static ProxyParam build(Long id) {
        ProxyParam param = new ProxyParam();
        param.setId(id);
        return param;
    }
	
    
	
	
	
	
	
	
	
	
	
	
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }
    public Integer getLocalPort() {
        return localPort;
    }
    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }
    public String getLocalHost() {
        return localHost;
    }
    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }
    public Integer getExternalPort() {
        return externalPort;
    }
    public void setExternalPort(Integer externalPort) {
        this.externalPort = externalPort;
    }
    public String getExternalHost() {
        return externalHost;
    }
    public void setExternalHost(String externalHost) {
        this.externalHost = externalHost;
    }
}
