package cn.blmdz.proxy.service;

import cn.blmdz.proxy.model.ProxyChannel;
import io.netty.channel.Channel;

public interface ProxyManager {

    /**
     * 根据授权码查找用户
     */
    ProxyChannel findUserByAuthCode(String appId, Integer port);
    
    /**
     * 增加通道
     */
    boolean addChannel(String appId, Integer port, Channel channel);
}
