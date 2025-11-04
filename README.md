# 💬 Java Chat Application (Client–Server)

A real-time **Chat Application** built in Java using **Socket Programming** and **Swing GUI**, enabling multiple clients to connect, send, and receive messages simultaneously.

---

## 🚀 Features

- 📡 Real-time messaging between multiple clients  
- 🖥️ Beautiful Swing-based user interface  
- 🌐 Client–Server communication using sockets  
- 🔄 Automatic message broadcast to all connected users  
- 🧾 Timestamped message history  
- 💻 Lightweight and easy to run  

---

## 🛠️ Tech Stack

- **Language:** Java  
- **Libraries:** Swing, AWT, FlatLaf  
- **Networking:** Java Sockets  
- **Tools:** IntelliJ IDEA / Eclipse  

---

## 📷 Screenshots



Example:<img width="410" height="273" alt="image" src="https://github.com/user-attachments/assets/1311398b-6638-4a11-b74b-0acc8c06d6be" />
<img width="411" height="131" alt="image" src="https://github.com/user-attachments/assets/77f50086-44f3-454d-820d-6969050f2718" />
<img width="424" height="289" alt="image" src="https://github.com/user-attachments/assets/08cd729b-fbf1-498d-bd0d-e32f86721200" />
<img width="427" height="304" alt="image" src="https://github.com/user-attachments/assets/0a4a2241-50d6-4436-81b0-493e0146fbf5" />


---

## 🧩 How It Works

1. The **Server** listens on port `8000` for incoming client connections.  
2. Each **Client** connects to the server and joins the chat room.  
3. Messages sent by one client are **broadcasted** to all others in real-time.  
4. Server maintains a list of connected users and active sockets.

---

## 🧠 Key Code Snippets

### 📡 Server Listening for Clients
```java
ServerSocket serverSocket = new ServerSocket(8000);
System.out.println("✅ Server started on port 8000");

while (true) {
    Socket socket = serverSocket.accept();
    System.out.println("🔗 Client connected: " + socket);
    new ClientHandler(socket).start();
}

---

## 👨‍💻 Contributors
- [Hitesh Kandpal](https://github.com/Hitesh20377)
- [Yuvraj](https://github.com/yuvi-yuvraj)




