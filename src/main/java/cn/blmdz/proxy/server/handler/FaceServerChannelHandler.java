package cn.blmdz.proxy.server.handler;

import java.net.InetSocketAddress;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyParam;
import cn.blmdz.proxy.server.ServerConstant;
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
public class FaceServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    
    private Channel BASE_PROXY_CHANNEL;
    
    public FaceServerChannelHandler(Channel base) {
        this.BASE_PROXY_CHANNEL = base;
    }

    /**
     * Channel 建立连接的时候触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        long id = GenerateUtil.id();
        ctx.channel().attr(ServerConstant.CHANNEL_ID).set(id);

        
        // 设置不可读 并暂时加入
        ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);
//        ServerConstant.ID_PORT_MAP.put(id, port);
        ServerConstant.ID_SERVER_CHANNEL_MAP.put(id, ctx.channel());// 暂时加进去
        BASE_PROXY_CHANNEL.writeAndFlush(Message.build(MessageType.CONNECT_SERVER, ProxyParam.build(id).toString()));
        System.out.println(id + " 向客户端发送链接请求");
        super.channelActive(ctx);
    }

    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        Channel channel = ctx.attr(ServerConstant.CHANNEL).get();
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);

        System.out.println(ctx.attr(ServerConstant.CHANNEL_ID).get() + " 向代理发送数据");
        channel.writeAndFlush(Message.build(MessageType.TRANSFER, bytes));
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);

        int id = ctx.channel().attr(ServerConstant.CHANNEL_ID).get();
        
        int port = ServerConstant.ID_PORT_MAP.get(id);
        ServerConstant.ID_PORT_MAP.remove(id);
        ServerConstant.ID_SERVER_CHANNEL_MAP.remove(id);
        
        Channel channel = ServerConstant.PORT_PROXY_CHANNEL_MAP.get(port);
        channel.writeAndFlush(Message.build(MessageType.DISCONNECT_SERVER, JSON.toJSONString(new ProxyParam(id, null))));
        System.out.println(id + "向客户端发送请求，" + MessageType.DISCONNECT_SERVER);
        super.channelInactive(ctx);
    }

//    /**
//     * 异常
//     */
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
//        cause.printStackTrace();
//        ctx.close();
//    }
}
