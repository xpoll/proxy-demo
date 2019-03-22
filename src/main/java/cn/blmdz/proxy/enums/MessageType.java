package cn.blmdz.proxy.enums;

/**
 * 消息类型
 */
public enum MessageType {

	HEARTBEAT((byte) 0x01, "心跳检测"),
	AUTH((byte) 0x02, "授权"),
	CONNECT((byte) 0x03, "连接"),
	DISCONNECT((byte) 0x04, "连接中断"),
    TRANSFER((byte) 0x05, "传输数据"),
    PORT((byte) 0x06, "暴露的端口"),
    UNKNOWPORT((byte) 0x07, "代理端访问端口未开启"),
    DESTORYCONNECT((byte) 0x08, "代理端端口无人访问，现请求关闭"),
    ALREADY((byte) 0x09, "该链接已经建立"),
	;
    private byte value;
    
    private String description;
	
	MessageType(byte value, String description) {
        this.value = value;
        this.description = description;
    }

    public byte value() {
        return this.value;
    }
    public String description() {
        return this.description;
    }
	
	public static MessageType conversion(byte value) {
		for (MessageType item : MessageType.values()) {
			if (item.value == value) return item;
		}
		return null;
	}
}
