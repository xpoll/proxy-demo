package cn.blmdz.proxy.server.handler;

import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyParam;
import cn.blmdz.proxy.server.ServerConstant;
import cn.blmdz.proxy.util.GenerateUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.StringUtil;

/**
 * 面向代理(真实服务器上一层)
 * 
 * @author xpoll
 * @date 2019年3月19日
 */
public class FaceProxyChannelHandler1 extends SimpleChannelInboundHandler<Message> {

	/**
	 * 每当从服务端读到客户端写入信息时
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
		System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
		System.out.println(JSON.toJSONString(Message.build(msg.getType(), msg.getParams())));
		ProxyParam obj = StringUtil.isNullOrEmpty(msg.getParams()) ? null
				: JSON.parseObject(msg.getParams(), ProxyParam.class);
		switch (msg.getType()) {
		case HEARTBEAT: // 心跳检测(all)
		{
			ctx.channel().writeAndFlush(Message.build(MessageType.HEARTBEAT));
			break;
		}
		case AUTH: // 授权请求(client)
		{
			String appIDPort = obj.getAppId() + "_" + obj.getLocalPort();
			if (ServerConstant.PORT_APPID_PORT_MAP.containsValue(appIDPort)) {// 是否已经在线
				ctx.channel().writeAndFlush(Message.build(MessageType.ALREADY));
				// 这里没有关闭，让client关闭
				return;
			} else if (!ServerConstant.APPID_AUTH_SET.contains(obj.getAppId())) {// 授权是否存在
				ctx.channel().writeAndFlush(Message.build(MessageType.AUTHERROR));
				// 这里没有关闭，让client关闭
				return;
			}

			synchronized (this) {

				int port = GenerateUtil.port();// 分配一个端口
				ServerBootstrap bootstrap = new ServerBootstrap();
				bootstrap.group(ServerConstant.bossGroup, ServerConstant.workGroup)
						.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
							@Override
							protected void initChannel(SocketChannel ch) throws Exception {
								// 计算流量以及调用次数
								// ch.pipeline().addLast(handlers);
								// 面向用户业务处理器
								ch.pipeline().addLast(new FaceServerChannelHandler(ctx.channel()));
							}
						});

				try {
					bootstrap.bind(port).get();
					ctx.channel().attr(ServerConstant.OUT_SERVER_PORT).set(port);
					ServerConstant.PORT_APPID_PORT_MAP.put(port, appIDPort);
					// 传输Channel和暴露端口号
					ctx.channel().writeAndFlush(
							Message.build(MessageType.PORT, JSON.toJSONString(ProxyParam.build("127.0.0.1", port))));

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					ctx.channel().close();
					return;
				}
			}
			break;
		}
        case CONNECT_PROXY:
        {
        	Channel channel = ServerConstant.ID_SERVER_CHANNEL_MAP.get(obj.getId());
            // 双向绑定
            channel.attr(ServerConstant.CHANNEL).set(ctx.channel());
            ctx.channel().attr(ServerConstant.CHANNEL).set(channel);
            System.out.println("双向完成");
            // 设置可读
            channel.config().setOption(ChannelOption.AUTO_READ, true);
            break;
        }
        case TRANSFER: // 数据传输(all)
        {
        	Channel channel = ctx.channel().attr(ServerConstant.CHANNEL).get();
            ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
            buf.writeBytes(msg.getData());
            channel.writeAndFlush(buf);
            break;
        }
        case DISCONNECT_SERVER: // 中断链接(all)
        {
        	Channel channel = ctx.channel().attr(ServerConstant.CHANNEL).get();
            if (channel != null) {
            	channel.attr(ServerConstant.CHANNEL).remove();
	            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
	            channel.close();
            }
            break;
        }
        case CONNECT_SERVER_SUCCESS:
        {
            
            break;
        }
        case CONNECT_SERVER_ERROR:
        {
            ServerConstant.ID_SERVER_CHANNEL_MAP.get(obj.getId()).writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            ServerConstant.ID_SERVER_CHANNEL_MAP.remove(obj.getId());
            break;
        }
		default:
			break;
		}
	}

	/**
	 * Channel 被关闭的时候触发（在断开连接的时候）
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
		Integer port = ctx.channel().attr(ServerConstant.OUT_SERVER_PORT).get();
		if (port != null) {
			ServerConstant.PORT_APPID_PORT_MAP.remove(ctx.channel().attr(ServerConstant.OUT_SERVER_PORT).get());
			ctx.channel().attr(ServerConstant.CHANNEL).remove();
		}
		super.channelInactive(ctx);
	}

	// @Override
	// public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	// throws Exception {
	// System.out.println(System.currentTimeMillis() + ": " +
	// Thread.currentThread().getStackTrace()[1]);
	// super.exceptionCaught(ctx, cause);
	// }
}
