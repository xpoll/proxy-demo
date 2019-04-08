//package cn.blmdz.proxy.server.handler;
//
//import com.alibaba.fastjson.JSON;
//
//import cn.blmdz.proxy.enums.MessageType;
//import cn.blmdz.proxy.model.Message;
//import cn.blmdz.proxy.server.ServerConstant;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.SimpleChannelInboundHandler;
//
///**
// * 面向代理(真实服务器上一层)
// * 
// * @author xpoll
// * @date 2019年3月19日
// */
//public class FaceProxyChannelHandler2 extends SimpleChannelInboundHandler<Message> {
//
//
//    /**
//     * 每当从服务端读到客户端写入信息时
//     */
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
//        System.out.println(JSON.toJSONString(Message.build(msg.getType(), msg.getParams())));
//        Channel channel = ctx.attr(ServerConstant.CHANNEL).get();
//        switch(msg.getType()) {
//        case HEARTBEAT: // 心跳检测(all)
//        {
//            ctx.channel().writeAndFlush(Message.build(MessageType.HEARTBEAT));
//            break;
//        }
//        case CONNECT_PROXY:
//        {
//            // 双向绑定
//            channel.attr(ServerConstant.CHANNEL).set(ctx.channel());
//            ctx.channel().attr(ServerConstant.CHANNEL).set(channel);
//            // 设置可读
//            channel.config().setOption(ChannelOption.AUTO_READ, true);
//            break;
//        }
//        case TRANSFER: // 数据传输(all)
//        {
//            ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
//            buf.writeBytes(msg.getData());
//            channel.writeAndFlush(buf);
//            break;
//        }
//        case DISCONNECT_SERVER: // 中断链接(all)
//        {
//            if (channel != null) {
//            	channel.attr(ServerConstant.CHANNEL).remove();
//	            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//	            channel.close();
//            }
//            break;
//        }
//        default:
//            break;
//        }
//    }
//
//    /**
//     * Channel 被关闭的时候触发（在断开连接的时候）
//     */
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
//        super.channelInactive(ctx);
//    }
//
////    @Override
////    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
////        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
////        super.exceptionCaught(ctx, cause);
////    }
//}
