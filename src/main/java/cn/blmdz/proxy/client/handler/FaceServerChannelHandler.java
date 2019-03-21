package cn.blmdz.proxy.client.handler;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.ClientContainer;
import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

public class FaceServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * Channel 建立连接的时候触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        super.channelActive(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        if (ClientContainer.channelProxy == null) {
            ctx.channel().close();
            return ;
        }
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        
        ClientContainer.channelProxy.writeAndFlush(new Message(MessageType.TRANSFER, JSON.toJSONString(ClientContainer.serverParam), bytes));
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ClientContainer.channelServer = null;
        ClientContainer.channelProxy.writeAndFlush(new Message(MessageType.UNKNOWPORT, JSON.toJSONString(ClientContainer.serverParam)));
        super.channelInactive(ctx);
    }

    /**
     * Channel 可写性已更改
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ClientContainer.channelProxy.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        super.channelWritabilityChanged(ctx);
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
