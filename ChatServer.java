package neww;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private ArrayList<String> userList = new ArrayList<>();
    private Hashtable<Socket, DataOutputStream> outputStreams = new Hashtable<>();
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(8000);
            System.out.println("✅ Server started on port 8000");
            System.out.println("Waiting for clients...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket);
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                outputStreams.put(socket, dout);
                new ServerThread(this, socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized void sendToAll(String message) {
        for (DataOutputStream dout : outputStreams.values()) {
            try {
                dout.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized void dispatchUserList() {
        sendToAll("UserList:" + userList.toString());
    }

    synchronized void removeUser(String userName, Socket socket) {
        userList.remove(userName);
        outputStreams.remove(socket);
        sendToAll("🔴 " + userName + " left the chat.");
        dispatchUserList();
        System.out.println("❌ " + userName + " disconnected.");
    }

    class ServerThread extends Thread {
        private ChatServer server;
        private Socket socket;
        private String userName;
        private boolean userJoined = false;

        public ServerThread(ChatServer server, Socket socket) {
            this.server = server;
            this.socket = socket;
        }

        public void run() {
            try {
                DataInputStream din = new DataInputStream(socket.getInputStream());
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    if (!userJoined) {
                        userName = din.readUTF();
                        synchronized (server) {
                            if (userList.contains(userName)) {
                                dout.writeUTF("Username already taken");
                            } else {
                                userList.add(userName);
                                dout.writeUTF("Accepted");
                                userJoined = true;
                                sendToAll("SYS:" + userName + " joined the chat.");
                                server.dispatchUserList();
                                System.out.println("👤 " + userName + " joined.");
                            }
                        }
                    } else {
                        String msg = din.readUTF();
                        server.sendToAll(userName + ": " + msg);
                        System.out.println(userName + ": " + msg);
                    }
                }
            } catch (IOException e) {
                server.removeUser(userName, socket);
            }
        }
    }
}
