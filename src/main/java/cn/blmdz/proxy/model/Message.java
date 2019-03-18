package cn.blmdz.proxy.model;

import cn.blmdz.proxy.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
	
//	private long serialNumber;
	
	public Message(MessageType type) {
		this.type = type;
	}
}
