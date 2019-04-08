package cn.blmdz.proxy.client.handler;

import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.ClientConstant;
import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.handler.IdleStateCheckHandler;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyParam;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.StringUtil;

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
			// ctx.channel().writeAndFlush(Message2.build(MessageType2.HEARTBEAT));
			break;
		}
		case ALREADY:
		case AUTHERROR: {
			System.out.println(msg.getType().description());
			ctx.channel().close();
			System.exit(-1);
			break;
		}
		case PORT: {
			System.out.println("链接成功, 地址: " + obj.getExternalHost() + ":" + obj.getExternalPort());
			break;
		}
        case CONNECT_SERVER:
        {
            // connect 是异步的
            AtomicBoolean atomicServer = new AtomicBoolean(false);
            AtomicBoolean atomicProxy = new AtomicBoolean(false);
            AtomicBoolean success = new AtomicBoolean(false);
            System.out.println(obj.getId() + " 建立连接 " + System.currentTimeMillis());

            
            System.out.println("开始连接真实");
            ClientConstant.bootstrapServer.connect(ClientConstant.CLIENT_HOST, ClientConstant.CLIENT_PORT)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture serverFuture) throws Exception {
                        	System.out.println("1");
                            if (serverFuture.isSuccess()) {
                                System.out.println(obj.getId() + " 与真实服务端建立连接成功 " + System.currentTimeMillis());
                                System.out.println("开始连接代理");
                                ClientConstant.bootstrapProxy.connect(ClientConstant.SERVER_HOST, ClientConstant.SERVER_PORT).addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture proxyFuture) throws Exception {
                                    	System.out.println("2");
                                        if (proxyFuture.isSuccess()) {
                                            System.out.println(obj.getId() + " 与代理服务端建立连接成功 " + System.currentTimeMillis());

                                            // 双向绑定
                                            proxyFuture.channel().attr(ClientConstant.CHANNEL).set(serverFuture.channel());
                                            serverFuture.channel().attr(ClientConstant.CHANNEL).set(proxyFuture.channel());
                                            
                                            obj.setAppId(ClientConstant.requestParam.getAppId());
                                            obj.setLocalHost(ClientConstant.requestParam.getLocalHost());
                                            obj.setLocalPort(ClientConstant.requestParam.getLocalPort());
                                            proxyFuture.channel().writeAndFlush(Message.build(MessageType.CONNECT_PROXY, obj.toString()));
                                            success.set(true);
                                        }
                                        atomicProxy.set(true);
                                    }
                                });
                            }
                            atomicServer.set(true);
                        }
                    });
            while(!(atomicServer.get() && atomicProxy.get())) Thread.sleep(1);
            ctx.channel().writeAndFlush(Message.build(success.get() ? MessageType.CONNECT_SERVER_SUCCESS : MessageType.CONNECT_SERVER_ERROR, msg.getParams()));
            break;
        }
        case TRANSFER:
        {
        	Channel channel = ctx.channel().attr(ClientConstant.CHANNEL).get();
            ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
            buf.writeBytes(msg.getData());
            channel.writeAndFlush(buf);
            break;
        }
      case DISCONNECT_SERVER:
      {
    	  Channel channel = ctx.channel().attr(ClientConstant.CHANNEL).get();
          if (channel != null) {
        	  System.out.println("DISCONNECT请求真实服务器关闭链接" + System.currentTimeMillis());
        	  channel.attr(ClientConstant.CHANNEL).remove();
        	  channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        	  channel.close();
          }
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
    	Channel channel = ctx.channel().attr(ClientConstant.CHANNEL).get();
    	if (channel != null) {
    		ctx.channel().attr(ClientConstant.CHANNEL).remove();
    	}
		super.channelInactive(ctx);
//		System.out.println("链接失败 。。 关闭 。。");
//		System.exit(-1);
	}

	// @Override
	// public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	// throws Exception {
	// System.out.println(System.currentTimeMillis() + ": " +
	// Thread.currentThread().getStackTrace()[1]);
	// super.exceptionCaught(ctx, cause);
	// }
}
