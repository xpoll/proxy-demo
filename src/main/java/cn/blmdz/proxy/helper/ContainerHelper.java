package cn.blmdz.proxy.helper;

import java.util.List;

import cn.blmdz.proxy.interfaces.Container;

/**
 * 容器服务类
 * @author xpoll
 */
public class ContainerHelper {

    private static volatile boolean running = true;
    
//    private static Object obj = new Object();

    private static List<Container> containers;
    
    /**
     * 容器启动
     * @param containers
     */
    public static void start(List<Container> containers) {
        ContainerHelper.containers = containers;
        
        start();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                synchronized (ContainerHelper.class) {
                    ContainerHelper.stop();
                    ContainerHelper.running = false;
                    ContainerHelper.class.notify();
                }
            }
        });
        
        synchronized (ContainerHelper.class) {
            while (running) {
                try {
                	ContainerHelper.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 启动所有容器
     */
    private static void start() {
        containers.forEach(item -> item.start());
    }
    
    /**
     * 关闭所有容器
     */
    private static void stop() {
        containers.forEach(item -> item.stop());
    }
}
