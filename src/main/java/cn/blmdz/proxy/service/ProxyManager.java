package cn.blmdz.proxy.service;

import cn.blmdz.proxy.model.ProxyChannel;
import cn.blmdz.proxy.model.ProxyRequestServerParam;
import io.netty.channel.Channel;

public interface ProxyManager {

    /**
     * 根据授权码查找用户
     */
    ProxyChannel findUserByAuthCodeFaceProxyPort(ProxyRequestServerParam param);

    /**
     * 根据授权码查找用户
     */
    ProxyChannel findUserByAuthCodeFaceServerPort(Integer port);
    
    /**
     * 增加通道
     */
    ProxyChannel addFaceProxyChannel(ProxyRequestServerParam param, Channel channel);
    
    void removeFaceProxyChannel(ProxyChannel proxy);
    
    /**
     * 
     */
    ProxyChannel findUserByChannel(Channel channel);
    
    
}
