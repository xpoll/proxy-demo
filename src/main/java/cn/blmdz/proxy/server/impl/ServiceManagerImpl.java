package cn.blmdz.proxy.server.impl;

import java.net.DatagramSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSON;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import cn.blmdz.proxy.model.ProxyChannel;
import cn.blmdz.proxy.model.ProxyRequestServerParam;
import cn.blmdz.proxy.server.ServerContainer;
import cn.blmdz.proxy.server.handler.FaceServerChannelHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

public class ServiceManagerImpl implements ServiceManager {
    
    private static final AttributeKey<Integer> CHANNEL_ID = AttributeKey.newInstance("CHANNEL_ID");
    
    // port
    private AtomicInteger portGenerate = new AtomicInteger(33333);
	
	// 代理端端口与 ProxyChannel 的映射
	private Map<String, ProxyChannel> FACE_PROXY_MAP = new HashMap<>();
	
	// 服务器端口与 ProxyChannel 的映射
    private Map<Integer, ProxyChannel> FACE_SERVER_MAP = new HashMap<>();
    
    /**
     * 组合key => appId_port
     */
    private String getKey(ProxyRequestServerParam param) {
        return param.getAppId() + "_" + param.getPort();
    }
    
    private synchronized Integer generatePort() {
    	int port = portGenerate.incrementAndGet();

    	try {
    		Socket s = new Socket("0.0.0.0", port);
    		s.close();
    	} catch (Exception e) {
    		return generatePort();
    	}
    	
    	try {
    		DatagramSocket s = new DatagramSocket(port);
    		s.close();
    	} catch (Exception e) {
    		return generatePort();
    	}
    	
    	return port;
    }
	

    @Override
    public ProxyChannel findByAuthCodeFaceProxyPort(String param) {
        return FACE_PROXY_MAP.get(getKey(JSON.parseObject(param, ProxyRequestServerParam.class)));
    }
    
    @Override
    public ProxyChannel findByFaceServerPort(Integer port) {
        return FACE_SERVER_MAP.get(port);
    }

    @Override
    public synchronized ProxyChannel addFaceProxyChannel(String param, Channel channel) {
    	ProxyRequestServerParam parseObject = JSON.parseObject(param, ProxyRequestServerParam.class);
        ProxyChannel proxy = FACE_PROXY_MAP.get(getKey(parseObject));
        if (proxy != null) {
            return null;
        }
        int faceServerPort = generatePort();// 分配一个端口
        ServerBootstrap bootstrap = faceServerBootstrap();
        try {
            bootstrap.bind(faceServerPort).get();
            proxy = ProxyChannel.buildFaceProxy(parseObject.getAppId(), parseObject.getPort(), channel, faceServerPort);
            FACE_SERVER_MAP.put(faceServerPort, proxy);
            FACE_PROXY_MAP.put(getKey(parseObject), proxy);
            channel.attr(CHANNEL_ID).set(faceServerPort);
            return proxy;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public synchronized void removeFaceProxyChannel(ProxyChannel proxy) {
        if (proxy == null) {
            System.out.println("proxy is null");
            return ;
        }
        FACE_SERVER_MAP.remove(proxy.getFaceServerPort());
        FACE_PROXY_MAP.remove(getKey(new ProxyRequestServerParam(proxy.getAppId(), proxy.getFaceProxyPort())));
        if (proxy.getFaceProxyChannel() != null) {
            proxy.getFaceProxyChannel().attr(CHANNEL_ID).remove();
            proxy.getFaceProxyChannel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            proxy.getFaceProxyChannel().close();
            if (proxy.getFaceServerChannel() != null) {
                proxy.getFaceServerChannel().attr(CHANNEL_ID).remove();
                proxy.getFaceServerChannel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//                proxy.getFaceServerChannel().close();
            }
        }
        // 如何退出并关闭
    }

    @Override
    public ProxyChannel findByChannel(Channel channel) {
        return FACE_SERVER_MAP.get(channel.attr(CHANNEL_ID).get());
    }

    
    private ServerBootstrap faceServerBootstrap() {
        ServerBootstrap bootstrapUser = new ServerBootstrap();
        bootstrapUser.group(ServerContainer.bossGroup, ServerContainer.workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 计算流量以及调用次数
//              ch.pipeline().addLast(handlers);
                // 面向用户业务处理器
            	ch.pipeline().addLast(new FaceServerChannelHandler(ServerContainer.proxyManager));
            }
        });
        return bootstrapUser;
    }


	@Override
	public ProxyChannel addFaceServerChannel(ProxyChannel proxy, Channel channel) {
		channel.config().setOption(ChannelOption.AUTO_READ, false);
    	proxy.getFaceProxyChannel().writeAndFlush(Message.build(MessageType.CONNECT));
		proxy.setFaceServerChannel(channel);
		channel.attr(CHANNEL_ID).set(proxy.getFaceServerPort());
		return proxy;
	}


	@Override
	public void removeFaceServerChannel(ProxyChannel proxy) {
	    proxy.getFaceProxyChannel().writeAndFlush(Message.build(MessageType.DESTORYCONNECT));
		proxy.getFaceServerChannel().attr(CHANNEL_ID).remove();
		proxy.setFaceServerChannel(null);
	}
}
