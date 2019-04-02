package cn.blmdz.proxy.client.handler;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.ClientConstant;
import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyServerInvoke;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FaceServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        int id = ctx.channel().attr(ClientConstant.CHANNEL_ID).get();
        byte[] bytes = new byte[msg.readableBytes()];
        msg.readBytes(bytes);
        System.out.println(id + " 向服务端发送数据");
        ClientConstant.channelProxy.writeAndFlush(Message.build(MessageType.TRANSFER, JSON.toJSONString(new ProxyServerInvoke(id, null)), bytes));
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        int id = ctx.channel().attr(ClientConstant.CHANNEL_ID).get();
        System.out.println(id + " 向代理发送channel关闭提示");
        // 异常
        ClientConstant.channelProxy.writeAndFlush(Message.build(MessageType.DISCONNECT, JSON.toJSONString(new ProxyServerInvoke(id, null))));
        ClientConstant.ID_SERVER_CHANNEL_MAP.remove(id);
        super.channelInactive(ctx);
    }

//    /**
//     * 异常
//     */
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
//        ctx.close();
//    }

}
