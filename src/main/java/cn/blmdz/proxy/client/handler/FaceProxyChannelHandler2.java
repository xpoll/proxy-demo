//package cn.blmdz.proxy.client.handler;
//
//import com.alibaba.fastjson.JSON;
//
//import cn.blmdz.proxy.client.ClientConstant;
//import cn.blmdz.proxy.model.Message;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//
//public class FaceProxyChannelHandler2 extends SimpleChannelInboundHandler<Message> {
//    
//    /**
//     * 每当从服务端读到客户端写入信息时
//     */
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
//        System.out.println(JSON.toJSONString(Message.build(msg.getType(), msg.getParams())));
//        Channel channel = ctx.channel().attr(ClientConstant.CHANNEL).get();
//        switch (msg.getType()) {
//        case HEARTBEAT: // 心跳检测(all)
//        {
//            // ctx.channel().writeAndFlush(Message2.build(MessageType2.HEARTBEAT));
//            break;
//        }
//        case TRANSFER:
//        {
//            ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
//            buf.writeBytes(msg.getData());
//            channel.writeAndFlush(buf);
//            break;
//        }
//      case DISCONNECT_SERVER:
//      {
//          if (channel != null) {
//        	  System.out.println("DISCONNECT请求真实服务器关闭链接" + System.currentTimeMillis());
//        	  channel.attr(ClientConstant.CHANNEL).remove();
//        	  channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//        	  channel.close();
//          }
//          break;
//      }
//        default:
//            break;
//        }
//    }
//
//
//    /**
//     * Channel 被关闭的时候触发（在断开连接的时候）
//     */
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        ctx.channel().attr(ClientConstant.CHANNEL).remove();
//        super.channelInactive(ctx);
//    }
//
////    @Override
////    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
////        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
////        super.exceptionCaught(ctx, cause);
////    }
//}
