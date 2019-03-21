package cn.blmdz.proxy.server.impl;

import cn.blmdz.proxy.model.ProxyChannel;
import io.netty.channel.Channel;

public interface ServiceManager {

    ProxyChannel findByAuthCodeFaceProxyPort(String param);

    ProxyChannel findByFaceServerPort(Integer port);
    
    ProxyChannel addFaceProxyChannel(String param, Channel channel);
    
    void removeFaceProxyChannel(ProxyChannel proxy);
    
    ProxyChannel findByChannel(Channel channel);

    ProxyChannel addFaceServerChannel(ProxyChannel proxy, Channel channel);

    void removeFaceServerChannel(ProxyChannel proxy);
    
}
