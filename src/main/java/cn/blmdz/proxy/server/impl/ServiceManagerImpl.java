package cn.blmdz.proxy.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

public class ServiceManagerImpl implements ServiceManager {
    
    private static final AttributeKey<String> CHANNEL_ID = AttributeKey.newInstance("CHANNEL_ID");
    
    // port
    private static AtomicInteger portGenerate = new AtomicInteger(33336);
    private static AtomicInteger id = new AtomicInteger(1);
	
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
    
    private static synchronized Integer generatePort() {
        int port = portGenerate.incrementAndGet();

        String cmd = null;
        if (System.getProperty("os.name").toLowerCase().contains("window")) {
            // cmd = "netstat -an | findstr " + port;// 无效
            cmd = "netstat -an";
        } else {
            cmd = "netstat -an --ip | grep " + port;
        }
        BufferedReader br = null;
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            if (sb.toString().contains(":" + port)) {
                return generatePort();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return generatePort();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            channel.attr(CHANNEL_ID).set(String.valueOf(faceServerPort));
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
//                proxy.getFaceServerChannel().attr(CHANNEL_ID).remove();
                proxy.getFaceServerChannel().forEach(item -> item.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE));
            }
        }
    }

    @Override
    public ProxyChannel findByChannel(Channel channel) {
        return FACE_SERVER_MAP.get(Integer.parseInt(channel.attr(CHANNEL_ID).get() == null ? String.valueOf(0) : channel.attr(CHANNEL_ID).get().split("_")[0]));
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
//		channel.config().setOption(ChannelOption.AUTO_READ, false); // TODO
	    if (proxy.getFaceServerChannel() == null || proxy.getFaceServerChannel().isEmpty())
	        proxy.getFaceProxyChannel().writeAndFlush(Message.build(MessageType.CONNECT));
		proxy.setFaceServerChannel(channel);
		channel.attr(CHANNEL_ID).set(proxy.getFaceServerPort() + "_" + id.incrementAndGet());
		return proxy;
	}


	@Override
	public void removeFaceServerChannel(ProxyChannel proxy, Channel channel) {
	    if (proxy.getFaceServerChannel().size() <= 1) {
	        proxy.getFaceProxyChannel().writeAndFlush(Message.build(MessageType.DESTORYCONNECT));
//		proxy.getFaceServerChannel().attr(CHANNEL_ID).remove();
	        proxy.setFaceServerChannel(null);
	    } else {
	        proxy.getFaceServerChannel().remove(channel);
	    }
	}
}
