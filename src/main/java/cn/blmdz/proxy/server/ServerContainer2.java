package cn.blmdz.proxy.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import cn.blmdz.proxy.handler.IdleStateCheckHandler;
import cn.blmdz.proxy.helper.ContainerHelper;
import cn.blmdz.proxy.interfaces.Container;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import cn.blmdz.proxy.server.handler.FaceProxyChannelHandler2;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

public class ServerContainer2 implements Container {

    // 线程池处理客户端的连接请求，并将accept的连接注册到另一个work线程上
    public static NioEventLoopGroup bossGroup;
    // 负责处理已建立的客户端通道上的数据读写
    public static NioEventLoopGroup workGroup;

    /**
     * Map[ChannelID, Port]
     */
    public static Map<Integer, Integer> ID_PORT_MAP = new HashMap<>();
    /**
     * Map[ChannelID, ServerChannel]
     */
    public static Map<Integer, Channel> ID_SERVER_CHANNEL_MAP = new HashMap<>();
    /**
     * Map[Port, APPID_Port]
     */
    public static Map<Integer, String> PORT_APPID_PORT_MAP = new HashMap<>();
    /**
     * Map[Port, ProxyChannel]
     */
    public static Map<Integer, Channel> PORT_PROXY_CHANNEL_MAP = new HashMap<>();
    /**
     * Set[APPID]
     */
    public static Set<String> APPID_AUTH_SET = new HashSet<>();

    public static final AttributeKey<Integer> CHANNEL_ID = AttributeKey.newInstance("CHANNEL_ID");
    public static final AttributeKey<Integer> OUT_SERVER_PORT = AttributeKey.newInstance("OUT_SERVER_PORT");
    
    static {
        APPID_AUTH_SET.add("babababa");
    }

    private static String SERVER_HOST;
    private static Integer SERVER_PORT;

    @Override
    public void start() {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrapServer = new ServerBootstrap();
        bootstrapServer.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
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
            bootstrapServer.bind(SERVER_HOST, SERVER_PORT).get();
            System.out.println("Server start success ...");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Server start error ...");
            this.stop();
        }
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
         SERVER_HOST = "0.0.0.0";
         SERVER_PORT = 7788;
         
        ContainerHelper.start(Arrays.asList(new Container[] { new ServerContainer2() }));
    }
}
