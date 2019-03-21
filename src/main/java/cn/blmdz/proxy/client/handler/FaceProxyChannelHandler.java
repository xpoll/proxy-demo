package cn.blmdz.proxy.client.handler;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.ClientContainer;
import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

public class FaceProxyChannelHandler extends SimpleChannelInboundHandler<Message> {

    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        System.out.println(JSON.toJSONString(msg));
        switch (msg.getType()) {
        case CONNECT:
            connectMessageHandler(ctx, msg);
            break;
        case DISCONNECT:
            disconnectMessageHandler(ctx, msg);
            break;
        case TRANSFER:
            transferMessageHandler(ctx, msg);
            break;
        case PORT:
            portMessageHandler(ctx, msg);
            break;
        case DESTORYCONNECT:
            distoryconnectMessageHandler(ctx, msg);
        default:
            break;
        }
    }

    private void connectMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ClientContainer.bootstrapServer.connect("0.0.0.0", ClientContainer.serverParam.getPort()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    ClientContainer.channelServer = future.channel();
                    ClientContainer.channelProxy.writeAndFlush(Message.build(MessageType.CONNECT, JSON.toJSONString(ClientContainer.serverParam)));
                    future.channel().config().setOption(ChannelOption.AUTO_READ, true);
                } else {
                    ClientContainer.channelServer = null;
                    ClientContainer.channelProxy.writeAndFlush(Message.build(MessageType.UNKNOWPORT, JSON.toJSONString(ClientContainer.serverParam)));
                }
            }
        });
    }

    private void disconnectMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        if (ClientContainer.channelServer != null) {
            ClientContainer.channelServer.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            ClientContainer.channelServer.close();
        }
    }

    private void transferMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
        buf.writeBytes(msg.getData());
        ClientContainer.channelServer.writeAndFlush(buf);
    }

    private void portMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        System.out.println("外部端口为: " + msg.getParams());
    }
    
    private void distoryconnectMessageHandler(ChannelHandlerContext ctx, Message msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        if (ClientContainer.channelServer != null) {
            ClientContainer.channelServer.close();
            ClientContainer.channelServer = null;
        }
    }

    /**
     * Channel 可写性已更改
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        if (ClientContainer.channelServer != null) {
            ClientContainer.channelServer.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ClientContainer.channelServer.close();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        super.exceptionCaught(ctx, cause);
    }
}
