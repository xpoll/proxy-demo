package cn.blmdz.proxy;

import java.util.concurrent.atomic.AtomicBoolean;

import cn.blmdz.proxy.client.ClientConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Test {

	static Bootstrap bootstrapServer = new Bootstrap();
	static NioEventLoopGroup workGroup = new NioEventLoopGroup();
	
	public static void main(String[] args) throws Exception {
	    bootstrapServer.group(workGroup).channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 面向真实服务器业务处理器
            }
        });
	    
	    for (int j = 0; j < 500; j++) {
	    	abc(j++);
		}
	}
	private static void abc(Integer i) throws Exception {
		System.out.println("开始建立连接 " + i);
    	AtomicBoolean atomic = new AtomicBoolean(false);
    	
		bootstrapServer.connect(ClientConstant.CLIENT_HOST, ClientConstant.CLIENT_PORT)
		.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					System.out.println(i + " 建立连接完成返回 " + System.currentTimeMillis());
				} else {
					System.out.println(i + " 建立连接失败返回 " + System.currentTimeMillis());
				}
				atomic.set(true);
			}
		});
        while(!atomic.get()) Thread.sleep(2);
	}
}
