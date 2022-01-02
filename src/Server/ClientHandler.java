package Server;

import Client.ChatGUI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    public Socket socket = null;
    public String username;
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();
    public static ArrayList<RoomChat> roomsChat = new ArrayList<>();
    public static ArrayList<ClientHandler> queue = new ArrayList<>();
    private BufferedReader in = null;
    private BufferedWriter out = null;
    private boolean isChatting = false;
    private RoomChat room;

    public ClientHandler(Socket socket) {
        this.socket = socket;

    }

    @Override
    public void run() {
        try {
            while (true) {

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String command = in.readLine();
                ExecuteCommand(command);
            }
        } catch (IOException ex) {
            System.out.println("Lỗi : " + ex);
        }
    }

    private void ExecuteCommand(String command) {
        StringTokenizer stk = new StringTokenizer(command, "||");
        String action = stk.nextToken();
        String value = stk.nextToken();
        System.out.println(this.username + ":" + command);
        switch (action) {
            case "Disconnect"->{
                clientHandlers.remove(this);
            }
            case "SendUserName" -> {
                boolean isValid = isValidUserName(value);
                if (isValid==false) {
                    Core.Send(out, "SendUserName||DuplicateUserName");

                } else {
                    String size = String.valueOf(clientHandlers.size());
                    Core.Send(out, "SendUserName||Success||" + size);
                    this.username = value;
                    clientHandlers.add(this);
                }
            }
            case "JoinGroup" -> {
                switch (value) {
                    case "Waiting" -> {
                        if (clientHandlers.size() == 1) {
                            Core.Send(out, "JoinGroup||NotEnough");
                        }
                        if (clientHandlers.size() >= 2) {
                            ClientHandler tmp = getClientHandler();
                            if (tmp == null) {
                                Core.Send(out, "JoinGroup||NotEnough");
                            }
                            if (tmp != null) {
                                Core.Send(out, "JoinGroup||Found||" + tmp.username);
                            }
                        }
                    }
                    case "JoinQueue" -> {
                        if (!queue.contains(this)) {
                            queue.add(this);
                        }

                    }
                    case "Yes" -> {
                        String user = stk.nextToken();
                        joinRoom(user);
                    }
                    case "No" -> {
                        String user = stk.nextToken();
                        room = getRoom(user);
                        // không một ai đồng ý nên phòng trống trơn
                        if (room == null) {
                            ClientHandler client = getClientHandler(user);
                            Core.Send(out, "JoinGroup||Fail||Đã từ chối");
                            Core.Send(client.out, "JoinGroup||Fail||null");
                            queue.remove(this);
                        } else {
                            // một người vào rồi nên khác null
                            ClientHandler client = room.getClient(0);
                            Core.Send(client.out, "JoinGroup||Fail||" + user + " đã từ chối lời mời");
                            Core.Send(out, "JoinGroup||Refuse||null");
                            client.isChatting = false;
                            queue.remove(client);
                            room.clear();
                            roomsChat.remove(room);
                        }
                    }
                }
            }
            
            case "SendMessage" -> {
                switch (value) {
                    case "Success" -> {
                        String fromUser = stk.nextToken();
                        String message = stk.nextToken();
                        if (command != null && message != null && fromUser != null) {
                            RoomChat roomchat = this.getRoom(fromUser);
                            if (roomchat != null) {
                                if (message.equals("Disconnected")) {
                                    roomchat.getArray().forEach(clientHandler -> {
                                        if (clientHandler.getUsername().equals(fromUser)) {
                                            clientHandlers.remove(clientHandler);
                                            try {
                                                clientHandler.socket.close();
                                            } catch (IOException ex) {
                                                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        } else {
                                            clientHandler.isChatting = false;
                                            queue.add(clientHandler);
                                            Core.Send(clientHandler.out, "Response||" + fromUser + "||" + message);
                                        }
                                    });
                                    roomchat.clear();
                                    roomsChat.remove(roomchat);
                                } else {
                                    roomchat.getArray().forEach(clientHandler -> {
                                        Core.Send(clientHandler.out, "Response||" + fromUser + "||" + message);
                                    });
                                }
                            }
                        }
                    }
                    case "Waiting" -> {
                        Core.Send(out, "Waiting");
                    }

                }
            }
            
        }
    }

    private void joinRoom(String username) {
        room = getRoom(username);
        if (room == null) {
            room = new RoomChat();
            queue.remove(this);
            room.Add(this);
            System.out.println("User : " + this.getUsername() + "đã tạo phòng");
            roomsChat.add(room);
           

        } else {
            if (room.isFull()) {
            } else {
                room.Add(this);
                System.out.println("User : " + this.getUsername() + "tham gia phòng");
                queue.remove(this);
            }
        }
        if (room.isFull()) {
            ClientHandler client1 = room.getClient(0);
            ClientHandler client2 = room.getClient(1);
            Core.Send(client1.out, "JoinGroup||Success||" + client2.getUsername());
            Core.Send(client2.out, "JoinGroup||Success||" + client1.getUsername());
        }
    }

    private RoomChat getRoom(String username) {
        for (RoomChat roomchat : roomsChat) {
            for (ClientHandler client : roomchat.getArray()) {
                if (client.getUsername().equals(username)) {
                    return roomchat;
                }
            }
        }
        return null;
    }

    private void closeAll(Socket socket, BufferedReader in, BufferedWriter out) {
        try {
            socket.close();
            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ClientHandler getClientHandler() {
        ClientHandler tmp = null;
        for (ClientHandler ele : queue) {
            if (!ele.equals(this)) {
                if (ele.isChatting == false) {
                    tmp = ele;
                }
            }
        }
        return tmp;
    }

    private ClientHandler getClientHandler(String username) {
        ClientHandler tmp = null;
        for (ClientHandler ele : clientHandlers) {
            if (!ele.equals(this)) {
                if (ele.isChatting == false) {
                    tmp = ele;
                }
            }
        }
        return tmp;
    }

    private boolean isValidUserName(String usernameClientInput) {
        if (clientHandlers.isEmpty()) {
            return true;
        } else {
            for (ClientHandler client : clientHandlers) {
                if (client.getUsername().equals(usernameClientInput)) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getUsername() {
        return username;
    }

    public boolean isChatting() {
        return isChatting;
    }
}
