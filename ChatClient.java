import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class ChatClient extends JFrame {

    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField nameField;
    private JButton joinButton, sendButton, exitButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;

    private String userName;
    private boolean joined = false;

    public ChatClient() {
        // ----- Frame setup -----
        setTitle("💬 Modern Chat App");
        setSize(750, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(25, 25, 25));

        // ----- Title -----
        JLabel titleLabel = new JLabel("💬 Anonymous Chat", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0x00FF99));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ----- Chat Panel -----
        JPanel chatPanel = new JPanel(new BorderLayout(10, 10));
        chatPanel.setBackground(new Color(30, 30, 30));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(40, 40, 40));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x00FF99), 1),
                new EmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);
        chatPanel.add(chatScroll, BorderLayout.CENTER);
        add(chatPanel, BorderLayout.CENTER);

        // ----- Active Users Panel -----
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(35, 35, 35));
        userPanel.setPreferredSize(new Dimension(200, 0));

        JLabel userLabel = new JLabel("Active Users", SwingConstants.CENTER);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(new Color(45, 45, 45));
        userList.setForeground(Color.LIGHT_GRAY);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(userScroll, BorderLayout.CENTER);
        add(userPanel, BorderLayout.EAST);

        // ----- Bottom Panel -----
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(30, 30, 30));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        messageField = new JTextField();
        messageField.setBackground(new Color(50, 50, 50));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x00FF99)),
                new EmptyBorder(8, 8, 8, 8)
        ));
        bottomPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");
        sendButton.setBackground(new Color(0x00FF99));
        sendButton.setForeground(Color.BLACK);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.setEnabled(false);

        exitButton.setBackground(new Color(255, 77, 77));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setFocusPainted(false);

        buttonPanel.add(sendButton);
        buttonPanel.add(exitButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // ----- Name Input -----
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.setBackground(new Color(30, 30, 30));
        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setForeground(Color.WHITE);
        nameField = new JTextField(10);
        nameField.setBackground(new Color(50, 50, 50));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(BorderFactory.createLineBorder(new Color(0x00FF99)));

        joinButton = new JButton("Join");
        joinButton.setBackground(new Color(0x00FF99));
        joinButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        joinButton.setFocusPainted(false);

        namePanel.add(nameLabel);
        namePanel.add(nameField);
        namePanel.add(joinButton);
        chatPanel.add(namePanel, BorderLayout.NORTH);

        // ----- Event Listeners -----
        joinButton.addActionListener(e -> joinChat());
        sendButton.addActionListener(e -> sendMessage());
        exitButton.addActionListener(e -> closeConnection());
        messageField.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });

        setVisible(true);
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8000);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            chatArea.append("🟢 Connected to server.\n");
            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            chatArea.append("🔴 Unable to connect to server.\n");
        }
    }

    private void joinChat() {
        try {
            userName = nameField.getText().trim();
            if (userName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter your name!");
                return;
            }
            if (userName.contains(",")) {
                JOptionPane.showMessageDialog(this, "Name cannot contain ','");
                return;
            }
            output.writeUTF(userName);
            chatArea.append("⏳ Waiting for server approval...\n");
        } catch (IOException e) {
            chatArea.append("Error joining chat.\n");
        }
    }

    private void sendMessage() {
        try {
            if (!joined) {
                JOptionPane.showMessageDialog(this, "Join the chat first!");
                return;
            }
            String msg = messageField.getText().trim();
            if (!msg.isEmpty()) {
                output.writeUTF("💬 " + userName + ": " + msg);
                messageField.setText("");
            }
        } catch (IOException e) {
            chatArea.append("Error sending message.\n");
        }
    }

    private void receiveMessages() {
        try {
            while (true) {
                String msg = input.readUTF();

                if (msg.startsWith("UserList:")) {
                    String listStr = msg.substring("UserList:".length());
                    List<String> list = Arrays.asList(listStr.substring(1, listStr.length() - 1).split(", "));
                    SwingUtilities.invokeLater(() -> {
                        userListModel.clear();
                        for (String name : list) {
                            if (!name.trim().equals(userName))
                                userListModel.addElement("👤 " + name.trim());
                        }
                    });
                } else if (msg.startsWith("Accepted")) {
                    joined = true;
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append("✅ Joined as " + userName + "\n");
                        sendButton.setEnabled(true);
                        joinButton.setEnabled(false);
                        nameField.setEditable(false);
                    });
                } else if (msg.equals("Username already taken")) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "⚠️ Username already exists!"));
                } else {
                    SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n"));
                }
            }
        } catch (IOException e) {
            chatArea.append("⚠️ Connection closed.\n");
        }
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}

