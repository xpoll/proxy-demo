package cn.blmdz.proxy.server;

import java.util.concurrent.ExecutionException;

import cn.blmdz.proxy.interfaces.ConfigChangedListener;
import cn.blmdz.proxy.interfaces.Container;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ProxyServerContainer implements Container, ConfigChangedListener {

	
	// 线程池处理客户端的连接请求，并将accept的连接注册到另一个work线程上
	public static NioEventLoopGroup bossGroup;
	// 负责处理已建立的客户端通道上的数据读写
	public static NioEventLoopGroup workGroup;

	@Override
	public void start() {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        
        ServerBootstrap bootstrapServer = new ServerBootstrap();
        bootstrapServer.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 解码处理器
                // 编码处理器
                // 心跳检测处理器
                // 面向代理业务处理器
//              ch.pipeline().addLast(handlers);
            }
        });
        
        try {
            bootstrapServer.bind("127.0.0.1", 7788).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void stop() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
	}
	
	@Override
	public void change() {
//	    bootstrapUser();
	}

}
