package cn.blmdz.proxy.client;

import java.util.Arrays;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.handler.FaceProxyChannelHandler1;
import cn.blmdz.proxy.client.handler.FaceServerChannelHandler;
import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.handler.IdleStateCheckHandler;
import cn.blmdz.proxy.helper.ContainerHelper;
import cn.blmdz.proxy.interfaces.Container;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientContainer implements Container {

    @Override
    public void start() {
        ClientConstant.workGroup = new NioEventLoopGroup();

        ClientConstant.bootstrapServer = new Bootstrap();
        ClientConstant.bootstrapServer.group(ClientConstant.workGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 面向真实服务器业务处理器
                        ch.pipeline().addLast(new FaceServerChannelHandler());
                    }
                });
        
        ClientConstant.bootstrapProxy = new Bootstrap();
        ClientConstant.bootstrapProxy.group(ClientConstant.workGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 解码处理器
                        ch.pipeline().addLast(new MessageDecoder());
                        // 编码处理器
                        ch.pipeline().addLast(new MessageEncoder());
                        // 心跳检测处理器
                        ch.pipeline().addLast(new IdleStateCheckHandler());
                        // 面向服务器业务处理器
                        ch.pipeline().addLast(new FaceProxyChannelHandler1());
                    }
                });
        ClientConstant.bootstrapProxy.connect(ClientConstant.SERVER_HOST, ClientConstant.SERVER_PORT).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ClientConstant.BASE_PROXY_CHANNEL = future.channel();
                    System.out.println("发送授权");
                    future.channel().writeAndFlush(Message.build(MessageType.AUTH, JSON.toJSONString(ClientConstant.requestParam)));
                } else {
                    System.out.println("连接服务器失败");
                }
            }
        });

        System.out.println("Proxy start success ...");
    }

    @Override
    public void stop() {
        ClientConstant.workGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
//        if (args == null || args.length != 3) {
//            System.out.println("args params is error.");
//        }
//        String APPID = args[0];
//        SERVER_HOST = args[1].split(":")[0];
//        SERVER_PORT = Integer.parseInt(args[1].split(":")[1]);
//
//        CLIENT_HOST = args[2].split(":")[0];
//        CLIENT_PORT = Integer.parseInt(args[2].split(":")[1]);
//
//        serverParam = new ProxyRequestServerParam(APPID, CLIENT_PORT);
        ContainerHelper.start(Arrays.asList(new Container[] { new ClientContainer() }));
    }
}
