package cn.blmdz.proxy.model;

import cn.blmdz.proxy.enums.MessageType;

/**
 * 消息体
 */
public class Message {

	/**
	 * 消息类型
	 */
	private MessageType type;
	
	/**
	 * 请求参数
	 */
	private String params;
	
	/**
	 * 消息数据
	 */
	private byte[] data;

	public MessageType getType() {
		return type;
	}
	
	public String getParams() {
		return params;
	}
	
	public byte[] getData() {
		return data;
	}

	public static Message build(MessageType type) {
		return new Message(type, null, null);
	}

	public static Message build(MessageType type, String params) {
		return new Message(type, params, null);
    }
    
//	public static Message2 build(MessageType2 type, byte[] data) {
//		return new Message2(type, null, data);
//    }
	
	public static Message build(MessageType type, String params, byte[] data) {
		return new Message(type, params, data);
	}
    
	private Message(MessageType type, String params, byte[] data) {
		this.type = type;
		this.params = params;
		this.data = data;
	}
}
