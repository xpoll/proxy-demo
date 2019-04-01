package cn.blmdz.proxy.model;

public class ProxyServerInvoke {
	/**
	 * 渠道号
	 */
	private Integer channelId;
    /**
     * 其他数据
     */
    private String other;
    
    public Integer getChannelId() {
        return channelId;
    }
    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }
    public String getOther() {
        return other;
    }
    public void setOther(String other) {
        this.other = other;
    }
    
    public ProxyServerInvoke(Integer channelId, String other) {
        this.channelId = channelId;
        this.other = other;
    }
}
