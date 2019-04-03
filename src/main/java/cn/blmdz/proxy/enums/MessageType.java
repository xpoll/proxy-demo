package cn.blmdz.proxy.enums;

/**
 * 消息类型
 */
public enum MessageType {

	HEARTBEAT(                  (byte) 0x01, "心跳检测"),
	AUTH(                       (byte) 0x02, "授权请求"),
	AUTHERROR(                  (byte) 0x03, "授权失败"),
	PORT(                       (byte) 0x04, "传输端口"),
	TRANSFER(                   (byte) 0x05, "数据传输"),
    DISCONNECT_PROXY(           (byte) 0x06, "中断链接"),
    DISCONNECT_SERVER(          (byte) 0x07, "中断链接"),
	ALREADY(                    (byte) 0x08, "已经存在"),
    CONNECT_PROXY(              (byte) 0x09, "建立链接-proxy"),
    CONNECT_SERVER(             (byte) 0x10, "建立链接-server"),
//    CONNECT_PROXY_SUCCESS(      (byte) 0x11, "建立成功-proxy"),
//    CONNECT_PROXY_ERROR(        (byte) 0x12, "建立失败-proxy"),
    DISCONNECT_SERVER_SUCCESS(  (byte) 0x13, "建立成功-server"),
    DISCONNECT_SERVER_ERROR(    (byte) 0x14, "建立失败-server"),
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
