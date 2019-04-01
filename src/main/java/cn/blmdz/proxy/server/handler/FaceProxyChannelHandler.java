package cn.blmdz.proxy.server.handler;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyChannel;
import cn.blmdz.proxy.server.impl.ServiceManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 面向代理(真实服务器上一层)
 * 
 * @author xpoll
 * @date 2019年3月19日
 */
public class FaceProxyChannelHandler extends SimpleChannelInboundHandler<Message> {

    private ServiceManager proxyManager;

    public FaceProxyChannelHandler(ServiceManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        System.out.println(JSON.toJSONString(msg));
        switch (msg.getType()) {
        case AUTH:
            authMessageHandler(ctx, msg);
            break;
        case CONNECT:
            connectMessageHandler(ctx, msg);
            break;
//        case DISCONNECT:
//            disconnectMessageHandler(ctx, msg);
//            break;
        case HEARTBEAT:
            heartbeatMessageHandler(ctx);
            break;
        case TRANSFER:
            transferMessageHandler(ctx, msg);
            break;
        case UNKNOWPORT:
            unknowportMessageHandler(ctx, msg);
            break;
        default:
            break;
        }
    }

    private void authMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ProxyChannel proxy = proxyManager.findByAuthCodeFaceProxyPort(msg.getParams());
        if (proxy != null) {
            ctx.channel().writeAndFlush(Message.build(MessageType.ALREADY));
            ctx.channel().close();
        }
        proxy = proxyManager.addFaceProxyChannel(msg.getParams(), ctx.channel());
        if (proxy == null) {
            ctx.channel().close();
            return;
        }
        proxy.getFaceProxyChannel()
                .writeAndFlush(Message.build(MessageType.PORT, String.valueOf(proxy.getFaceServerPort())));
    }

    private void connectMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ProxyChannel proxy = proxyManager.findByAuthCodeFaceProxyPort(msg.getParams());
        if (proxy == null || proxy.getFaceProxyChannel() == null) {
            ctx.channel().close();
            return;
        }
        // if (proxy.getFaceServerChannel() != null) {
        // proxy.getFaceServerChannel().config().setOption(ChannelOption.AUTO_READ,
        // true); // TODO
        // }
    }

    private void disconnectMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        proxyManager.removeFaceProxyChannel(proxyManager.findByAuthCodeFaceProxyPort(msg.getParams()));
    }

    private void heartbeatMessageHandler(ChannelHandlerContext ctx) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ctx.channel().writeAndFlush(Message.build(MessageType.HEARTBEAT));
    }

    private void transferMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ProxyChannel proxy = proxyManager.findByChannel(ctx.channel());
        if (proxy.getFaceServerChannel() == null)
            return;

        ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
        buf.writeBytes(msg.getData());
        proxy.getFaceServerChannel().forEach(item -> item.writeAndFlush(buf.copy()));
    }

    private void unknowportMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        System.out.println(msg.getType().description());
        ProxyChannel proxy = proxyManager.findByChannel(ctx.channel());
        if (proxy.getFaceServerChannel() == null || proxy.getFaceServerChannel().isEmpty())
            return;
        proxy.getFaceServerChannel().forEach(item -> item.close());
    }

    /**
     * Channel 可写性已更改
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ProxyChannel proxy = proxyManager.findByChannel(ctx.channel());
        if (proxy != null && proxy.getFaceServerChannel() != null) {

            proxy.getFaceServerChannel()
                    .forEach(item -> item.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable()));
        }
        super.channelWritabilityChanged(ctx);
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        proxyManager.removeFaceProxyChannel(proxyManager.findByChannel(ctx.channel()));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        super.exceptionCaught(ctx, cause);
    }
}
