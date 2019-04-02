package cn.blmdz.proxy.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.AttributeKey;

public class ServerConstant {


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

    public static String SERVER_HOST;
    public static Integer SERVER_PORT;
}
