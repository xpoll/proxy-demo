package cn.blmdz.proxy.server;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import cn.blmdz.proxy.handler.IdleStateCheckHandler;
import cn.blmdz.proxy.helper.ContainerHelper;
import cn.blmdz.proxy.interfaces.Container;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import cn.blmdz.proxy.server.handler.FaceProxyChannelHandler2;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerContainer2 implements Container {

    @Override
    public void start() {
        ServerConstant.bossGroup = new NioEventLoopGroup();
        ServerConstant.workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrapServer = new ServerBootstrap();
        bootstrapServer.group(ServerConstant.bossGroup, ServerConstant.workGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 解码处理器
                        ch.pipeline().addLast(new MessageDecoder());
                        // 编码处理器
                        ch.pipeline().addLast(new MessageEncoder());
                        // 心跳检测处理器
                        ch.pipeline().addLast(new IdleStateCheckHandler());
                        // 面向代理业务处理器
                        ch.pipeline().addLast(new FaceProxyChannelHandler2());
                    }
                });

        try {
            bootstrapServer.bind(ServerConstant.SERVER_HOST, ServerConstant.SERVER_PORT).get();
            System.out.println("Server start success ...");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Server start error ...");
            this.stop();
        }
    }

    @Override
    public void stop() {
        ServerConstant.bossGroup.shutdownGracefully();
        ServerConstant.workGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        ServerConstant.SERVER_HOST = "0.0.0.0";
        ServerConstant.SERVER_PORT = 7788;
         
        ContainerHelper.start(Arrays.asList(new Container[] { new ServerContainer2() }));
    }
}
