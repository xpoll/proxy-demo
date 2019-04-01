package cn.blmdz.proxy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerateUtil {

    private static AtomicInteger GENERATE_PORT = new AtomicInteger(33336);
    private static AtomicInteger GENERATE_ID = new AtomicInteger(0);

    public static Integer id() {
        return GENERATE_ID.incrementAndGet();
    }
    
    public static Integer port() {
        int port = GENERATE_PORT.incrementAndGet();
        String cmd = null;
        if (System.getProperty("os.name").toLowerCase().contains("window")) {
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
                return port();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return port();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return port;
    }
}
