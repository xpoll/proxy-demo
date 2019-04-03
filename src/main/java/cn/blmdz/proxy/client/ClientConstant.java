package cn.blmdz.proxy.client;

import java.util.HashMap;
import java.util.Map;

import cn.blmdz.proxy.model.ProxyParam;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.AttributeKey;

public class ClientConstant {
    
    public static NioEventLoopGroup workGroup;
    public static Bootstrap bootstrapProxy;
//    public static Bootstrap bootstrapServer;
    public static Channel BASE_PROXY_CHANNEL;

    /**
     * Map[ChannelID, ServerChannel]
     */
    public static Map<Integer, Channel> ID_SERVER_CHANNEL_MAP = new HashMap<>();

    public static final AttributeKey<Integer> CHANNEL_ID = AttributeKey.newInstance("CHANNEL_ID");
    public static final AttributeKey<Channel> CHANNEL = AttributeKey.newInstance("CHANNEL");
    
    public static String APPID = "babababa";
    public static String SERVER_HOST = "127.0.0.1";
    public static Integer SERVER_PORT = 7788;
//    public static String CLIENT_HOST = "0.0.0.0";
    public static String CLIENT_HOST = "10.40.63.18";
    public static Integer CLIENT_PORT = 80;

    public static ProxyParam requestParam = ProxyParam.build(APPID, CLIENT_HOST, CLIENT_PORT);
}
