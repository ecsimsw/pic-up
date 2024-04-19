package ecsimsw.picup.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class PortUtils {

    public static boolean checkPortAvailable(int port) {
        try(
            ServerSocket ss  = new ServerSocket(port);
            DatagramSocket ds = new DatagramSocket(port);
        ) {
            ss.setReuseAddress(true);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
