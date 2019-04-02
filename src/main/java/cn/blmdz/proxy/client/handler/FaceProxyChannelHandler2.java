package cn.blmdz.proxy.client.handler;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.ClientConstant;
import cn.blmdz.proxy.enums.MessageType2;
import cn.blmdz.proxy.model.Message2;
import cn.blmdz.proxy.model.ProxyServerInvoke;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class FaceProxyChannelHandler2 extends SimpleChannelInboundHandler<Message2> {
    
    public static class Status {
        private boolean result = false;
        
        public void setSuccess() {
            this.result = true;
        }
        public boolean getSuccess() {
            return this.result;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ctx.fireChannelActive();
    }
    
    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message2 msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        System.out.println(JSON.toJSONString(msg));
        switch (msg.getType()) {
        case HEARTBEAT: // 心跳检测(all)
        {
            // ctx.channel().writeAndFlush(Message2.build(MessageType2.HEARTBEAT));
            break;
        }
        case AUTHERROR:
        {
            System.out.println(msg.getType().description());
            ctx.channel().close();
            System.exit(-1);
            break;
        }
        case PORT:
        {
            System.out.println("链接成功, 地址: " + ClientConstant.SERVER_HOST + ":" + JSON.parseObject(msg.getParams(), ProxyServerInvoke.class).getOther());
            break;
        }
        case CONNECT:
        {
            Status status = new Status();
            Integer id = JSON.parseObject(msg.getParams(), ProxyServerInvoke.class).getChannelId();
            System.out.println(id + " 建立连接 " + System.currentTimeMillis());
            ClientConstant.bootstrapServer.connect(ClientConstant.CLIENT_HOST, ClientConstant.CLIENT_PORT)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                future.channel().attr(ClientConstant.CHANNEL_ID).set(id);
                                ClientConstant.ID_SERVER_CHANNEL_MAP.put(id, future.channel());
                                System.out.println(id + " 建立连接完成 " + System.currentTimeMillis());
                                // 要不要返回建立连接成功 ？
                            } else {
                                ctx.channel().writeAndFlush(Message2.build(MessageType2.NOSOURCE, JSON.toJSONString(new ProxyServerInvoke(id, null))));
                                System.out.println(id + " 建立连接失败返回 " + MessageType2.NOSOURCE.description() + System.currentTimeMillis());
                            }
                            status.setSuccess();
                        }
                    });
            while(!status.getSuccess()) Thread.sleep(2);
            System.out.println(id + " 建立连接完成并返回 " + System.currentTimeMillis());
            break;
        }
        case DISCONNECT:
        {
            Integer id = JSON.parseObject(msg.getParams(), ProxyServerInvoke.class).getChannelId();
            System.out.println("DISCONNECT请求真实服务器关闭链接但并没close" + System.currentTimeMillis());
            ClientConstant.ID_SERVER_CHANNEL_MAP.get(id).writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            // 未关闭 待测试
            ClientConstant.ID_SERVER_CHANNEL_MAP.remove(id);
            break;
        }
        case TRANSFER:
        {
            Integer id = JSON.parseObject(msg.getParams(), ProxyServerInvoke.class).getChannelId();
            System.out.println(id + " 接收服务端数据");
            ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
            buf.writeBytes(msg.getData());
            ClientConstant.ID_SERVER_CHANNEL_MAP.get(id).writeAndFlush(buf);
            break;
        }
        case NOSOURCE:
        {
            System.out.println(msg.getType().description());
            System.out.println("NOSOURCE请求真实服务器关闭链接但并没close" + System.currentTimeMillis());
            synchronized (this) {
                ClientConstant.ID_SERVER_CHANNEL_MAP.values().forEach(channel -> {
                    channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                    // 未关闭 待测试
                });
                ClientConstant.ID_SERVER_CHANNEL_MAP.clear();
            }
            break;
        }
        case ALREADY:
        {
            System.out.println(msg.getType().description());
            ctx.channel().close();
            System.exit(-1);
            break;
        }
        default:
            break;
        }
    }


    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ClientConstant.ID_SERVER_CHANNEL_MAP.values().forEach(channel -> {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            // 未关闭 待测试
        });
        
        super.channelInactive(ctx);
        System.out.println("链接失败 。。 关闭 。。");
//        System.exit(-1);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        super.exceptionCaught(ctx, cause);
    }
}
