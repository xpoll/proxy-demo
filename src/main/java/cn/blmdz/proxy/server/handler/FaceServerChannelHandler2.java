package cn.blmdz.proxy.server.handler;

import java.net.InetSocketAddress;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.enums.MessageType2;
import cn.blmdz.proxy.model.Message2;
import cn.blmdz.proxy.model.ProxyServerInvoke;
import cn.blmdz.proxy.server.ServerConstant;
import cn.blmdz.proxy.util.GenerateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 面向服务器
 * 
 * @author xpoll
 * @date 2019年3月19日
 */
public class FaceServerChannelHandler2 extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * Channel 建立连接的时候触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        Integer port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
        int id = GenerateUtil.id();
        ctx.channel().attr(ServerConstant.CHANNEL_ID).set(id);

        ServerConstant.ID_PORT_MAP.put(id, port);
        ServerConstant.ID_SERVER_CHANNEL_MAP.put(id, ctx.channel());

        Channel channel = ServerConstant.PORT_PROXY_CHANNEL_MAP.get(port);
        if (channel == null) {
            // 客户端未连接
            ctx.channel().writeAndFlush("客户端未连接");
            return ;
        }
        channel.writeAndFlush(Message2.build(MessageType2.CONNECT, JSON.toJSONString(new ProxyServerInvoke(id, null))));
        System.out.println(id + " 向客户端发送链接请求");
        
        super.channelActive(ctx);
    }

    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        int id = ctx.channel().attr(ServerConstant.CHANNEL_ID).get();
        
        Channel channel = ServerConstant.PORT_PROXY_CHANNEL_MAP.get(ServerConstant.ID_PORT_MAP.get(id));
        
        if (channel == null) {
            // 客户端未连接
            ctx.channel().writeAndFlush("客户端未连接");
            return ;
        }

        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        System.out.println(id + " 向代理发送数据");
        channel.writeAndFlush(Message2.build(MessageType2.TRANSFER, JSON.toJSONString(new ProxyServerInvoke(id, null)), bytes));
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);

        int id = ctx.channel().attr(ServerConstant.CHANNEL_ID).get();
//      * Map[ChannelID, Port]  --  Map<Integer, Channel> ID_PORT_MAP
//      * Map[ChannelID, ServerChannel]  --  Map<Integer, Channel> ID_SERVER_CHANNEL_MAP
//      * Map[Port, APPID_Port]  --  Map<Integer, String> PORT_APPID_PORT_MAP
//      * Map[Port, ProxyChannel]  --  Map<Integer, String> PORT_PROXY_CHANNEL_MAP
        
        int port = ServerConstant.ID_PORT_MAP.get(id);
        ServerConstant.ID_PORT_MAP.remove(id);
        ServerConstant.ID_SERVER_CHANNEL_MAP.remove(id);
        
        MessageType2 type = null;
        
        if (!ServerConstant.ID_PORT_MAP.containsValue(port)) {
            type = MessageType2.NOSOURCE;
        } else {
            type = MessageType2.DISCONNECT;
        }
        Channel channel = ServerConstant.PORT_PROXY_CHANNEL_MAP.get(port);
        if (channel != null) channel.writeAndFlush(Message2.build(type, JSON.toJSONString(new ProxyServerInvoke(id, null))));
        System.out.println(id + "向客户端发送请求，" + type);
        super.channelInactive(ctx);
    }

    /**
     * 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        cause.printStackTrace();
        ctx.close();
    }
}
