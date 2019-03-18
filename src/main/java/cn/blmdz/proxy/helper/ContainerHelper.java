package cn.blmdz.proxy.helper;

import java.util.List;

import cn.blmdz.proxy.interfaces.Container;

public class ContainerHelper {

    private static volatile boolean running = true;
    
    private static Object obj = new Object();

    private static List<Container> containers;
    
    public static void start(List<Container> containers) {
        ContainerHelper.containers = containers;
        
        start();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                synchronized (obj) {
                    ContainerHelper.stop();
                    ContainerHelper.running = false;
                    obj.getClass().notify();
                }
            }
        });
        
        synchronized (obj) {
            while (running) {
                try {
                    obj.getClass().wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static void start() {
        containers.forEach(item -> item.start());
    }
    
    private static void stop() {
        containers.forEach(item -> item.stop());
    }
}
