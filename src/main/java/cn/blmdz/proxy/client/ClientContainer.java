package cn.blmdz.proxy.client;

import java.util.Arrays;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.handler.FaceProxyChannelHandler;
import cn.blmdz.proxy.client.handler.FaceServerChannelHandler;
import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.handler.IdleStateCheckHandler;
import cn.blmdz.proxy.helper.ContainerHelper;
import cn.blmdz.proxy.interfaces.Container;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyRequestServerParam;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientContainer implements Container {

    private static NioEventLoopGroup workerGroup;
    private static Bootstrap bootstrapProxy;
    public static Bootstrap bootstrapServer;
    public static Channel channelServer;
    public static Channel channelProxy;

    public static ProxyRequestServerParam serverParam;
    
    public static String SERVER_HOST;
    public static Integer SERVER_PORT;
    public static String CLIENT_HOST;
    public static Integer CLIENT_PORT;
    
	@Override
	public void start() {
        workerGroup = new NioEventLoopGroup();
        
        bootstrapServer = new Bootstrap();
        bootstrapServer.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// 面向真实服务器业务处理器
				ch.pipeline().addLast(new FaceServerChannelHandler());
			}
        });
        
        bootstrapProxy = new Bootstrap();
        bootstrapProxy.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
                // 解码处理器
                ch.pipeline().addLast(new MessageDecoder());
                // 编码处理器
                ch.pipeline().addLast(new MessageEncoder());
                // 心跳检测处理器
                ch.pipeline().addLast(new IdleStateCheckHandler());
                // 面向服务器业务处理器
            	ch.pipeline().addLast(new FaceProxyChannelHandler());
			}
        });
        bootstrapProxy.connect(SERVER_HOST, SERVER_PORT).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channelProxy = future.channel();
                    future.channel().writeAndFlush(Message.build(MessageType.AUTH, JSON.toJSONString(serverParam)));
                } else {
                    System.out.println("连接服务器失败");
                }
            }
        });

        System.out.println("Proxy start success ...");
	}

	@Override
	public void stop() {
        workerGroup.shutdownGracefully();
	}

    public static void main(String[] args) {
        String APPID = "babababa";
        SERVER_HOST = "127.0.0.1";
//        SERVER_HOST = "blmdz.cn";
        SERVER_PORT = 7788;
        
        CLIENT_HOST = "0.0.0.0";
        CLIENT_PORT = 8080;
        
        serverParam = new ProxyRequestServerParam(APPID, CLIENT_PORT);
        ContainerHelper.start(Arrays.asList(new Container[] { new ClientContainer() }));
    }
}
