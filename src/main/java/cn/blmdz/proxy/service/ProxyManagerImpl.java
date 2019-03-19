package cn.blmdz.proxy.service;

import java.util.Map;

import com.google.common.collect.Maps;

import cn.blmdz.proxy.model.ProxyChannel;
import io.netty.channel.Channel;

public class ProxyManagerImpl implements ProxyManager {
	
	private Map<String, Map<Integer, ProxyChannel>> CHANNEL = Maps.newConcurrentMap();

	@Override
	public ProxyChannel findUserByAuthCode(String appId, Integer port) {
		Map<Integer, ProxyChannel> proxyMap = CHANNEL.get(appId);
		if (proxyMap == null) return null;
		return proxyMap.get(port);
	}

	@Override
	public boolean addChannel(String appId, Integer port, Channel channel) {
		Map<Integer, ProxyChannel> proxyMap = CHANNEL.get(appId);
		if (proxyMap == null) {
			CHANNEL.put(appId, Maps.newConcurrentMap());
			proxyMap = CHANNEL.get(appId);
		}
		
		ProxyChannel proxyChannel = proxyMap.get(port);
		
		if (proxyChannel == null) {
			proxyChannel = ProxyChannel.buildFaceServer(port, channel);
			proxyMap.put(port, proxyChannel);
			
			return true;
		}
		
		return false;
	}

}
