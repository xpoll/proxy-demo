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
    PORT((byte) 0x06, "告诉代理端暴露的端口"),
	;
	private byte value;
	
	MessageType(byte value, String description) {}
	
	public byte value() {
		return this.value;
	}
	
	public static MessageType conversion(byte value) {
		for (MessageType item : MessageType.values()) {
			if (item.value == value) return item;
		}
		return null;
	}
}
