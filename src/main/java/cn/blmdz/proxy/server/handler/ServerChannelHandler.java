package cn.blmdz.proxy.server.handler;

import org.fengfei.lanproxy.protocol.Constants;
import org.fengfei.lanproxy.protocol.ProxyMessage;
import org.fengfei.lanproxy.server.ProxyChannelManager;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerChannelHandler extends SimpleChannelInboundHandler<Message> {

	/**
	 * 每当从服务端读到客户端写入信息时
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
		switch (msg.getType()) {
		case AUTH:
			break;
		case CONNECT:
			break;
		case DISCONNECT:
			break;
		case HEARTBEAT:
			heartbeatMessageHandler(ctx);
			break;
		case TRANSFER:
			break;
		default:
			break;
		}
	}
	
	private void authMessageHandler(ChannelHandlerContext ctx, Message msg) {
		// 根据key找到用户
		// 找到用户的端口映射表
		
		// 端口没有直接关闭本channel
		// channel已建立直接关闭本channel
		
		// 封装本channel为端口映射点
	}
	
	private void heartbeatMessageHandler(ChannelHandlerContext ctx) {
		// 返回心跳包
		ctx.channel().writeAndFlush(new Message(MessageType.HEARTBEAT));
	}
	

    /**
     * channel可写性已更改
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel userChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if (userChannel != null) {
            userChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }

    /**
     * channel被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel userChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if (userChannel != null && userChannel.isActive()) {
            String clientKey = ctx.channel().attr(Constants.CLIENT_KEY).get();
            String userId = ctx.channel().attr(Constants.USER_ID).get();
            Channel cmdChannel = ProxyChannelManager.getCmdChannel(clientKey);
            if (cmdChannel != null) {
                ProxyChannelManager.removeUserChannelFromCmdChannel(cmdChannel, userId);
            } else {
                logger.warn("null cmdChannel, clientKey is {}", clientKey);
            }

            // 数据发送完成后再关闭连接，解决http1.0数据传输问题
            userChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            userChannel.close();
        } else {
            ProxyChannelManager.removeCmdChannel(ctx.channel());
        }

        super.channelInactive(ctx);
    }

}
