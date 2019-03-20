package cn.blmdz.proxy.server.handler;

import java.net.InetSocketAddress;

import cn.blmdz.proxy.model.ProxyChannel;
import cn.blmdz.proxy.service.ProxyManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 面向服务器
 * @author xpoll
 * @date 2019年3月19日
 */
public class FaceServerChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {
    
    private ProxyManager proxyManager;
    
    public FaceServerChannelHandler(ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    /**
     * Channel 建立连接的时候触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        Integer port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
        
        ProxyChannel proxyChannel = proxyManager.findUserByAuthCodeFaceServerPort(port);
        
    }
    
    /**
     * 每当从服务端读到客户端写入信息时
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        
    }

    /**
     * Channel 被关闭的时候触发（在断开连接的时候）
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        
    }

    /**
     * Channel 可写性已更改
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        
    }

    /**
     * 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
