package cn.blmdz.proxy.server.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.enums.MessageType2;
import cn.blmdz.proxy.model.Message2;
import cn.blmdz.proxy.model.ProxyRequestServerParam;
import cn.blmdz.proxy.model.ProxyServerInvoke;
import cn.blmdz.proxy.server.ServerConstant;
import cn.blmdz.proxy.util.GenerateUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 面向代理(真实服务器上一层)
 * 
 * @author xpoll
 * @date 2019年3月19日
 */
public class FaceProxyChannelHandler2 extends SimpleChannelInboundHandler<Message2> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        ctx.fireChannelActive();
    }

    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message2 msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        System.out.println(JSON.toJSONString(msg));
        switch(msg.getType()) {
        case HEARTBEAT: // 心跳检测(all)
        {
            ctx.channel().writeAndFlush(Message2.build(MessageType2.HEARTBEAT));
            break;
        }
        case AUTH: // 授权请求(client)
        {
            ProxyRequestServerParam obj = JSON.parseObject(msg.getParams(), ProxyRequestServerParam.class);
            String appIDPort = obj.getAppId() + "_" + obj.getPort();
            if (ServerConstant.PORT_APPID_PORT_MAP.containsValue(appIDPort)) {
                ctx.channel().writeAndFlush(Message2.build(MessageType2.ALREADY));
                // 这里没有关闭，让client关闭
                return ;
            } else if (!ServerConstant.APPID_AUTH_SET.contains(obj.getAppId())) {
                ctx.channel().writeAndFlush(Message2.build(MessageType2.AUTHERROR));
                // 这里没有关闭，让client关闭
                return ;
            }
            
            synchronized (this) {

                int faceServerPort = GenerateUtil.port();// 分配一个端口
                ServerBootstrap bootstrap = faceServerBootstrap();
                try {
                    bootstrap.bind(faceServerPort).get();
                    ctx.channel().attr(ServerConstant.OUT_SERVER_PORT).set(faceServerPort);
                    ServerConstant.PORT_PROXY_CHANNEL_MAP.put(faceServerPort, ctx.channel());
                    ServerConstant.PORT_APPID_PORT_MAP.put(faceServerPort, appIDPort);
                    // 传输Channel和暴露端口号
                    ctx.channel().writeAndFlush(Message2.build(MessageType2.PORT, JSON.toJSONString(new ProxyServerInvoke(null, String.valueOf(faceServerPort)))));
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    ctx.channel().close();
                    return;
                }
            }
            break;
        }
        case TRANSFER: // 数据传输(all)
        {
            
            Channel channel = ServerConstant.ID_SERVER_CHANNEL_MAP.get(JSON.parseObject(msg.getParams(), ProxyServerInvoke.class).getChannelId());
            if (channel == null) {
                ctx.channel().writeAndFlush(Message2.build(MessageType2.NOSOURCE));
                // 这里没有关闭，让client关闭
            } else {
                ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
                buf.writeBytes(msg.getData());
                channel.writeAndFlush(buf);
                System.out.println(JSON.parseObject(msg.getParams(), ProxyServerInvoke.class).getChannelId() + " 接收真是服务器的数据并发给浏览器");
            }
            break;
        }
        case NOSOURCE: // 无接受源(all)
        {
            int id = JSON.parseObject(msg.getParams(), ProxyServerInvoke.class).getChannelId();
            Channel channel = ServerConstant.ID_SERVER_CHANNEL_MAP.get(id);
            if (channel != null) {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                // 让服务端关闭
//                channel.close();
//                ServerConstant.ID_SERVER_CHANNEL_MAP.remove(id);
            }
            break;
        }
        case DISCONNECT: // 中断链接(all)
        {
            int id = JSON.parseObject(msg.getParams(), ProxyServerInvoke.class).getChannelId();
            Channel channel = ServerConstant.ID_SERVER_CHANNEL_MAP.get(id);
            if (channel != null) {
//                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                channel.close();
                ServerConstant.ID_SERVER_CHANNEL_MAP.remove(id);
            }
            break;
        }
        default:
            break;
        }
    }

    private ServerBootstrap faceServerBootstrap() {
        ServerBootstrap bootstrapUser = new ServerBootstrap();
        bootstrapUser.group(ServerConstant.bossGroup, ServerConstant.workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 计算流量以及调用次数
//              ch.pipeline().addLast(handlers);
                // 面向用户业务处理器
                ch.pipeline().addLast(new FaceServerChannelHandler2());
            }
        });
        return bootstrapUser;
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        int faceServerPort = ctx.channel().attr(ServerConstant.OUT_SERVER_PORT).get();
        
        synchronized (this) {
            // 查出来需要关闭的server
            
            List<Integer> channelIds = new ArrayList<>();
            
            ServerConstant.ID_PORT_MAP.keySet().forEach(id -> {if (ServerConstant.ID_PORT_MAP.get(id).equals(faceServerPort)) channelIds.add(id);});
            
            channelIds.forEach(id -> {
                ServerConstant.ID_SERVER_CHANNEL_MAP.get(id).writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                // 需要测试需不需要关闭
                // ServerConstant.ID_SERVER_CHANNEL_MAP.get(id).close()
                ServerConstant.ID_SERVER_CHANNEL_MAP.remove(id);
                });
            ServerConstant.PORT_APPID_PORT_MAP.remove(faceServerPort);
            ServerConstant.PORT_PROXY_CHANNEL_MAP.remove(faceServerPort);
        }
        // 需不需要发送关闭待测
        // ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        super.exceptionCaught(ctx, cause);
    }
}
