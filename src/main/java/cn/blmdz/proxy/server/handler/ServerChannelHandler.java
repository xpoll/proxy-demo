package cn.blmdz.proxy.server.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.User;
import cn.blmdz.proxy.model.UserProxy;
import cn.blmdz.proxy.service.ProxyManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 面向代理(真实服务器上一层)
 * @author xpoll
 * @date 2019年3月19日
 */
public class ServerChannelHandler extends SimpleChannelInboundHandler<Message> {
    
    private ProxyManager proxyManager;
    
    public ServerChannelHandler(ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }

	/**
	 * 每当从服务端读到客户端写入信息时
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
		switch (msg.getType()) {
		case AUTH:
		    authMessageHandler(ctx, msg);
			break;
		case CONNECT:
		    connectMessageHandler(ctx, msg);
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
	    String appId = msg.getParams();
	    int port = 2222;// TODO
	    UserProxy proxy = proxyManager.findUserByAuthCode(appId, port);
	    if (proxy == null) {
	        ctx.channel().close();
	        return ;
	    }
		// 封装本channel为端口映射点
	    if (!proxyManager.addChannel(appId, port, ctx.channel())) {
	        ctx.channel().close();
	    }
	}

    private void connectMessageHandler(ChannelHandlerContext ctx, Message msg) {
        
    }
	
	private void heartbeatMessageHandler(ChannelHandlerContext ctx) {
		// 返回心跳包
		ctx.channel().writeAndFlush(new Message(MessageType.HEARTBEAT));
	}
	

    /**
     * Channel 可写性已更改
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
     * Channel 被关闭的时候触发（在断开连接的时候）
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
