package cn.blmdz.proxy.server.handler;

import java.net.InetSocketAddress;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyChannel;
import cn.blmdz.proxy.server.ServerContainer2;
import cn.blmdz.proxy.server.impl.ServiceManager;
import cn.blmdz.proxy.util.GenerateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 面向服务器
 * 
 * @author xpoll
 * @date 2019年3月19日
 */
public class FaceServerChannelHandler2 extends SimpleChannelInboundHandler<ByteBuf> {

    private ServiceManager proxyManager;

    public FaceServerChannelHandler2(ServiceManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    /**
     * Channel 建立连接的时候触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        Integer port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
        int id = GenerateUtil.id();
        ctx.channel().attr(ServerContainer2.CHANNEL_ID).set(id);
        

        ServerContainer2.ID_PORT_MAP.put(id, port);
        ServerContainer2.ID_SERVER_CHANNEL_MAP.put(id, ctx.channel());
        super.channelActive(ctx);
    }

    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
//      * Map[ChannelID, Port]  --  Map<Integer, Channel> ID_PORT_MAP
//      * Map[ChannelID, ServerChannel]  --  Map<Integer, Channel> ID_SERVER_CHANNEL_MAP
//      * Map[Port, APPID_Port]  --  Map<Integer, String> PORT_APPID_PORT_MAP
//      * Map[Port, ProxyChannel]  --  Map<Integer, String> PORT_PROXY_CHANNEL_MAP
        
        int id = ctx.channel().attr(ServerContainer2.CHANNEL_ID).get();
        
        Channel channel = ServerContainer2.PORT_PROXY_CHANNEL_MAP.get(ServerContainer2.ID_PORT_MAP.get(id));
        
        ProxyChannel proxy = proxyManager.findByChannel(ctx.channel());
        if (proxy == null) {
            ctx.channel().close();
            return;
        }

        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        proxy.getFaceProxyChannel().writeAndFlush(Message.build(MessageType.TRANSFER, bytes));
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ProxyChannel proxy = proxyManager.findByChannel(ctx.channel());
        if (proxy == null) {
            ctx.channel().close();
        } else {
            proxyManager.removeFaceServerChannel(proxy, ctx.channel());
        }
        super.channelInactive(ctx);
    }

    /**
     * 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ctx.close();
    }
}
