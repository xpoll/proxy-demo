package cn.blmdz.proxy.client.handler;

import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.client.ClientConstant;
import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.handler.IdleStateCheckHandler;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyParam;
import cn.blmdz.proxy.protocol.MessageDecoder;
import cn.blmdz.proxy.protocol.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.StringUtil;

public class FaceProxyChannelHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Integer id = ctx.channel().attr(ClientConstant.CHANNEL_ID).get();
        if (id != null && ClientConstant.ID_SERVER_CHANNEL_MAP.get(id) != null) {
            ClientConstant.ID_SERVER_CHANNEL_MAP.get(id).config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }
        super.channelWritabilityChanged(ctx);
    }
    
    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
        System.out.println(JSON.toJSONString(Message.build(msg.getType(), msg.getParams())));
        ProxyParam obj = StringUtil.isNullOrEmpty(msg.getParams()) ? null : JSON.parseObject(msg.getParams(), ProxyParam.class);
        switch (msg.getType()) {
        case HEARTBEAT: // 心跳检测(all)
        {
            // ctx.channel().writeAndFlush(Message2.build(MessageType2.HEARTBEAT));
            break;
        }
        case ALREADY:
        case AUTHERROR:
        {
            System.out.println(msg.getType().description());
            ctx.channel().close();
            System.exit(-1);
            break;
        }
        case PORT:
        {
            System.out.println("链接成功, 地址: " + obj.getExternalHost() + ":" + obj.getExternalPort());
            break;
        }
        case CONNECT_SERVER:
        {
            // connect 是异步的
            AtomicBoolean atomicServer = new AtomicBoolean(false);
            AtomicBoolean atomicProxy = new AtomicBoolean(false);
            System.out.println(obj.getId() + " 建立连接 " + System.currentTimeMillis());

            Bootstrap bootstrapServer = new Bootstrap();
            FaceServerChannelHandler handler = new FaceServerChannelHandler();
            bootstrapServer.group(ClientConstant.workGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 面向真实服务器业务处理器
                            ch.pipeline().addLast(handler);
                        }
                    });
            boolean success = false;
            
            
            bootstrapServer.connect(ClientConstant.CLIENT_HOST, ClientConstant.CLIENT_PORT)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture serverFuture) throws Exception {
                            if (serverFuture.isSuccess()) {

                                Bootstrap bootstrapProxy = new Bootstrap();
                                bootstrapProxy.group(ClientConstant.workGroup).channel(NioSocketChannel.class)
                                        .handler(new ChannelInitializer<SocketChannel>() {
                                            @Override
                                            protected void initChannel(SocketChannel ch) throws Exception {
                                                // 解码处理器
                                                ch.pipeline().addLast(new MessageDecoder());
                                                // 编码处理器
                                                ch.pipeline().addLast(new MessageEncoder());
                                                // 心跳检测处理器
                                                ch.pipeline().addLast(new IdleStateCheckHandler());
                                                // 面向服务器业务处理器
                                                ch.pipeline().addLast(new FaceProxyChannelHandler());
                                            }
                                        });
                                
                                bootstrapProxy.connect(ClientConstant.SERVER_HOST, ClientConstant.SERVER_PORT).addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture proxyFuture) throws Exception {
                                        if (proxyFuture.isSuccess()) {
//                                            handler.setChannel(proxyFuture.channel());

                                            // 双向绑定
                                            proxyFuture.channel().attr(ClientConstant.CHANNEL).set(serverFuture.channel());
                                            serverFuture.channel().attr(ClientConstant.CHANNEL).set(proxyFuture.channel());
                                            
                                            obj.setAppId(ClientConstant.requestParam.getAppId());
                                            obj.setLocalHost(ClientConstant.requestParam.getLocalHost());
                                            obj.setLocalPort(ClientConstant.requestParam.getLocalPort());
                                            proxyFuture.channel().writeAndFlush(Message.build(MessageType.CONNECT_PROXY, obj.toString()));
                                            success = true;
                                        }
                                        atomicProxy.set(true);
                                    }
                                });
                            }
                            atomicServer.set(true);
                        }
                    });
            while(!(atomicServer.get() && atomicProxy.get())) Thread.sleep(1);
            ctx.channel().writeAndFlush(Message.build(success ? MessageType.CONNECT_SERVER_SUCCESS : MessageType.CONNECT_SERVER_ERROR, msg.getParams()));
            break;
        }
        case CONNECT_PROXY_SUCCESS:
        {
            
            break;
        }
        case CONNECT_PROXY_ERROR:
        {
            break;
        }
        case TRANSFER:
        {
            Integer id = JSON.parseObject(msg.getParams(), ProxyParam.class).getChannelId();
            System.out.println(id + " 接收服务端数据");
            ByteBuf buf = ctx.alloc().buffer(msg.getData().length);
            buf.writeBytes(msg.getData());
            ClientConstant.ID_SERVER_CHANNEL_MAP.get(id).writeAndFlush(buf);
            break;
        }
      case DISCONNECT_SERVER:
      {
          Integer id = JSON.parseObject(msg.getParams(), ProxyParam.class).getChannelId();
          Channel channel = ClientConstant.ID_SERVER_CHANNEL_MAP.get(id);
          if (channel != null) {
        	  System.out.println("DISCONNECT请求真实服务器关闭链接" + System.currentTimeMillis());
        	  channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
          }
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
        System.exit(-1);
    }

//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
//        super.exceptionCaught(ctx, cause);
//    }
}
