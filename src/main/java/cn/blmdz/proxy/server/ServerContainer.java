package cn.blmdz.proxy.server;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import cn.blmdz.proxy.helper.ContainerHelper;
import cn.blmdz.proxy.interfaces.Container;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import cn.blmdz.proxy.server.handler.FaceProxyChannelHandler;
import cn.blmdz.proxy.server.handler.IdleStateCheckHandler;
import cn.blmdz.proxy.service.ProxyManager;
import cn.blmdz.proxy.service.ProxyManagerImpl;
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
	
	public static ProxyManager proxyManager;

    private static final int MAX_FRAME_LENGTH = 2 * 1024 * 1024;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final int LENGTH_ADJUSTMENT = 0;

	@Override
	public void start() {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        proxyManager = new ProxyManagerImpl();
        
        ServerBootstrap bootstrapServer = new ServerBootstrap();
        bootstrapServer.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 解码处理器
                ch.pipeline().addLast(new MessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
                // 编码处理器
                ch.pipeline().addLast(new MessageEncoder());
                // 心跳检测处理器
                ch.pipeline().addLast(new IdleStateCheckHandler(IdleStateCheckHandler.READ_IDLE_TIME, IdleStateCheckHandler.WRITE_IDLE_TIME, 0));
                // 面向代理业务处理器
            	ch.pipeline().addLast(new FaceProxyChannelHandler(proxyManager));
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
	
	public static void main(String[] args) {
		ContainerHelper.start(Arrays.asList(new Container[] { new ServerContainer() }));
	}
}
