package cn.blmdz.proxy.server;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import cn.blmdz.proxy.handler.IdleStateCheckHandler;
import cn.blmdz.proxy.helper.ContainerHelper;
import cn.blmdz.proxy.interfaces.Container;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import cn.blmdz.proxy.server.handler.FaceProxyChannelHandler;
import cn.blmdz.proxy.server.impl.ServiceManager;
import cn.blmdz.proxy.server.impl.ServiceManagerImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerContainer implements Container {
	
	// 线程池处理客户端的连接请求，并将accept的连接注册到另一个work线程上
	public static NioEventLoopGroup bossGroup;
	// 负责处理已建立的客户端通道上的数据读写
	public static NioEventLoopGroup workGroup;
	
	public static ServiceManager proxyManager;
	
    public static String SERVER_HOST;
    public static Integer SERVER_PORT;

	@Override
	public void start() {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        proxyManager = new ServiceManagerImpl();
        
        ServerBootstrap bootstrapServer = new ServerBootstrap();
        bootstrapServer.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 解码处理器
                ch.pipeline().addLast(new MessageDecoder());
                // 编码处理器
                ch.pipeline().addLast(new MessageEncoder());
                // 心跳检测处理器
                ch.pipeline().addLast(new IdleStateCheckHandler());
                // 面向代理业务处理器
            	ch.pipeline().addLast(new FaceProxyChannelHandler(proxyManager));
            }
        });
        
        try {
            bootstrapServer.bind(SERVER_HOST, SERVER_PORT).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Server start success ...");
    }

	@Override
	public void stop() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
	}
	
	public static void main(String[] args) {
//        SERVER_HOST = "0.0.0.0";
//        SERVER_PORT = 7788;
	    
        if (args == null || args.length != 1) {
            System.out.println("args params is error.");
        }
        SERVER_HOST = args[0].split(":")[0];
        SERVER_PORT = Integer.parseInt(args[0].split(":")[1]);
        
		ContainerHelper.start(Arrays.asList(new Container[] { new ServerContainer() }));
	}
}
