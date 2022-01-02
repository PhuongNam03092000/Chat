package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {

    private ServerSocket server;
    private Socket socket;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    public MainServer(int port) {
        try {
            server = new ServerSocket(port);
            ExecutorService executor = Executors.newCachedThreadPool();
            while (true) {
                socket = server.accept();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Runnable handle = new ClientHandler(socket);
                executor.execute(handle);
            }
        } catch (IOException ex) {
            System.err.println("Lỗi : " + ex);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    server.close();
                } catch (IOException ex) {
                    System.out.println("Lỗi đống Socket hoặc đóng Server: " + ex);
                }
            }
        }
    }

    public static void main(String[] args) {
        MainServer main = new MainServer(5000);
    }
}
