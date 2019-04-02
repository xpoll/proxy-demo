package cn.blmdz.proxy.enums;

/**
 * 消息类型
 */
public enum MessageType2 {

	HEARTBEAT( (byte) 0x01, "心跳检测(all)"),
	AUTH(      (byte) 0x02, "授权请求(client)"),
	AUTHERROR( (byte) 0x03, "授权失败(server)"),
	PORT(      (byte) 0x04, "传输端口(server)"),
	TRANSFER(  (byte) 0x05, "数据传输(all)"),
	NOSOURCE(  (byte) 0x06, "无接受源(all)"),
	DISCONNECT((byte) 0x07, "中断链接(server)"),
	ALREADY(   (byte) 0x08, "已经存在(server)"),
    CONNECT(   (byte) 0x09, "建立链接(server)"),
	;
    private byte value;
    
    private String description;
	
	MessageType2(byte value, String description) {
        this.value = value;
        this.description = description;
    }

    public byte value() {
        return this.value;
    }
    public String description() {
        return this.description;
    }
	
	public static MessageType2 conversion(byte value) {
		for (MessageType2 item : MessageType2.values()) {
			if (item.value == value) return item;
		}
		return null;
	}
}
