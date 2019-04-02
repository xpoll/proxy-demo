package cn.blmdz.proxy.client.handler;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.ClientConstant;
import cn.blmdz.proxy.enums.MessageType2;
import cn.blmdz.proxy.model.Message2;
import cn.blmdz.proxy.model.ProxyServerInvoke;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FaceServerChannelHandler2 extends SimpleChannelInboundHandler<ByteBuf> {

    /**
     * Channel 建立连接的时候触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        System.out.println(ctx.channel().attr(ClientConstant.CHANNEL_ID).get() + " 向服务端发送数据");
        ClientConstant.channelProxy.writeAndFlush(Message2.build(MessageType2.TRANSFER, JSON.toJSONString(new ProxyServerInvoke(ctx.channel().attr(ClientConstant.CHANNEL_ID).get(), null)), bytes));
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        System.out.println("向代理发送channel关闭请求");
        ClientConstant.channelProxy.writeAndFlush(Message2.build(MessageType2.DISCONNECT, JSON.toJSONString(new ProxyServerInvoke(ctx.channel().attr(ClientConstant.CHANNEL_ID).get(), null))));
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
