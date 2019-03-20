package cn.blmdz.proxy.client;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.interfaces.Container;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyRequestServerParam;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import cn.blmdz.proxy.server.handler.IdleStateCheckHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientContainer implements Container {

    private NioEventLoopGroup workerGroup;
    private Bootstrap bootstrapServer;
    private Bootstrap bootstrapProxy;
    private Channel channel;

    private static final int MAX_FRAME_LENGTH = 2 * 1024 * 1024;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final int LENGTH_ADJUSTMENT = 0;
    
	@Override
	public void start() {
        workerGroup = new NioEventLoopGroup();
        
        bootstrapServer = new Bootstrap();
        bootstrapServer.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// 面向真实服务器业务处理器
//				ch.pipeline().addLast(handlers);
			}
        });
        bootstrapServer.bind("127.0.0.1", 7788).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				channel = future.channel();
				future.channel().writeAndFlush(new Message(MessageType.AUTH, JSON.toJSONString(new ProxyRequestServerParam("ababab", 8080))));
			}
        	
        });
        
        bootstrapProxy = new Bootstrap();
        bootstrapProxy.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
                // 解码处理器
                ch.pipeline().addLast(new MessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
                // 编码处理器
                ch.pipeline().addLast(new MessageEncoder());
                // 心跳检测处理器
                ch.pipeline().addLast(new IdleStateCheckHandler(IdleStateCheckHandler.READ_IDLE_TIME, IdleStateCheckHandler.WRITE_IDLE_TIME, 0));
                // 面向服务器业务处理器
//            	ch.pipeline().addLast(handlers);
			}
        });
		
	}

	@Override
	public void stop() {
        workerGroup.shutdownGracefully();
	}

}
