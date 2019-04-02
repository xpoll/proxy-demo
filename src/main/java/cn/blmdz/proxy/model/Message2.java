package cn.blmdz.proxy.model;

import cn.blmdz.proxy.enums.MessageType2;

/**
 * 消息体
 */
public class Message2 {

	/**
	 * 消息类型
	 */
	private MessageType2 type;
	
	/**
	 * 请求参数
	 */
	private String params;
	
	/**
	 * 消息数据
	 */
	private byte[] data;

	public MessageType2 getType() {
		return type;
	}
	
	public String getParams() {
		return params;
	}
	
	public byte[] getData() {
		return data;
	}

	public static Message2 build(MessageType2 type) {
		return new Message2(type, null, null);
	}

	public static Message2 build(MessageType2 type, String params) {
		return new Message2(type, params, null);
    }
    
//	public static Message2 build(MessageType2 type, byte[] data) {
//		return new Message2(type, null, data);
//    }
	
	public static Message2 build(MessageType2 type, String params, byte[] data) {
		return new Message2(type, params, data);
	}
    
	private Message2(MessageType2 type, String params, byte[] data) {
		this.type = type;
		this.params = params;
		this.data = data;
	}
}
