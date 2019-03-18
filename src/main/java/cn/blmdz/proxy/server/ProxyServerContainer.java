package cn.blmdz.proxy.server;

import cn.blmdz.proxy.interfaces.ConfigChangedListener;
import cn.blmdz.proxy.interfaces.Container;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ProxyServerContainer implements Container, ConfigChangedListener {

	
	// 线程池处理客户端的连接请求，并将accept的连接注册到另一个work线程上
	private NioEventLoopGroup bossGroup;
	// 负责处理已建立的客户端通道上的数据读写
	private NioEventLoopGroup workGroup;
	
	public ProxyServerContainer() {
		bossGroup = new NioEventLoopGroup();
		workGroup = new NioEventLoopGroup();
	}

	@Override
	public void start() {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// 解码处理器
				// 编码处理器
				// 心跳检测处理器
				// 业务处理器
//				ch.pipeline().addLast(handlers);
			}
		});
		
		bootstrap.bind("127.0.0.1", 7788);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void change() {
		// TODO Auto-generated method stub
		
	}

}
