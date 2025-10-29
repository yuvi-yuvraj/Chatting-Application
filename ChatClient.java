package neww;
import javax.swing.*;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.UIManager;
import java.awt.Font;

public class ChatClient extends JFrame {

    private JTextField messageField;
    private JTextField nameField;
    private JButton joinButton, sendButton, exitButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JLabel statusLabel;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private Map<String, Color> userColors = new HashMap<>();
    private String userName;
    private boolean joined = false;

    
    private JPanel chatContainer;

    public ChatClient() {

        setTitle("💬 Modern Chat App");
        setSize(750, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(25, 25, 25));

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setBorder(new EmptyBorder(6, 0, 6, 0));
        statusLabel.setText("");
        add(statusLabel, BorderLayout.BEFORE_FIRST_LINE);

        JPanel chatPanel = new JPanel(new BorderLayout(10, 10));
        chatPanel.setBackground(new Color(30, 30, 30));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(new Color(30, 30, 30));

        JScrollPane chatScroll = new JScrollPane(chatContainer);
        chatScroll.setBorder(null);
        chatScroll.getViewport().setOpaque(false);

        chatPanel.add(chatScroll, BorderLayout.CENTER);

        add(chatPanel, BorderLayout.CENTER);

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(35, 35, 35));
        userPanel.setPreferredSize(new Dimension(200, 0));

        JLabel userLabel = new JLabel("Active Users", SwingConstants.CENTER);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);

        userList.setForeground(Color.LIGHT_GRAY);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        userPanel.add(userLabel, BorderLayout.NORTH);
        userPanel.add(userScroll, BorderLayout.CENTER);
        add(userPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(30, 30, 30));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        messageField = new JTextField();
        messageField.putClientProperty("JTextField.placeholderText", "Type message...");
        messageField.setBackground(new Color(50, 50, 50));
        messageField.setForeground(Color.WHITE);
        messageField.setCaretColor(Color.WHITE);
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.setEnabled(false);

        bottomPanel.add(messageField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(30, 30, 30));

        sendButton = new JButton("Send");
        sendButton.setForeground(Color.BLACK);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setEnabled(false);

        Color enabledColor = new Color(0x00FF99);
        Color disabledColor = new Color(80, 80, 80);
        sendButton.setBackground(disabledColor);

        sendButton.addPropertyChangeListener("enabled", evt -> {
            boolean en = (boolean) evt.getNewValue();
            sendButton.setBackground(en ? enabledColor : disabledColor);
        });

        exitButton = new JButton("Exit");
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        buttonPanel.add(sendButton);
        buttonPanel.add(exitButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.setBackground(new Color(30, 30, 30));
        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setForeground(Color.WHITE);

        nameField = new JTextField(10);
        nameField.putClientProperty("JTextField.placeholderText", "Enter name...");
        nameField.setBackground(new Color(50, 50, 50));
        nameField.setForeground(Color.WHITE);

        joinButton = new JButton("Join");
        joinButton.setBackground(new Color(0x00FF99));
        joinButton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        namePanel.add(nameLabel);
        namePanel.add(nameField);
        namePanel.add(joinButton);
        chatPanel.add(namePanel, BorderLayout.NORTH);

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

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("🟢 Connected to server.");
                statusLabel.setForeground(new Color(0x00CC00));
            });

            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("⏳ Waiting for server approval...");
                statusLabel.setForeground(Color.RED);
            });

            appendMessage("🔴 Unable to connect to server.", Color.red);
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

        } catch (IOException e) {
            appendMessage("Error joining chat.", Color.RED);
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
                output.writeUTF(": " + msg);
                messageField.setText("");
            }
        } catch (IOException e) {
            appendMessage("Error sending message.", Color.RED);
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
                    messageField.setEnabled(true);
                    joined = true;
                    SwingUtilities.invokeLater(() -> {

                        appendMessage("✅ Joined as " + userName, Color.CYAN);
                        sendButton.setEnabled(true);
                        joinButton.setEnabled(false);
                        nameField.setEditable(false);
                    });

                } else if (msg.equals("Username already taken")) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "⚠️ Username already exists!"));
                } else {

                    SwingUtilities.invokeLater(() -> {
                        boolean mine = userName != null && extractName(msg).equals(userName);
                        Color c = mine ? new Color(0x00FF99) : getUserColor(extractName(msg));
                        appendMessage(msg, c);
                    });
                }
            }
        } catch (IOException e) {
            appendMessage("⚠️ Connection closed.", Color.WHITE);
        }
    }

    private void appendMessage(String text, Color textColor) {

        boolean mine = userName != null && text.startsWith(userName + ":");
        boolean system = !text.contains(":");

        // ✅ System message centered
        if (system) {

            JPanel wrapper = new JPanel();
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
            wrapper.setOpaque(false);

            JLabel lbl = new JLabel(text);
            lbl.setForeground(textColor);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

            wrapper.add(Box.createHorizontalGlue());
            wrapper.add(lbl);
            wrapper.add(Box.createHorizontalGlue());

            chatContainer.add(wrapper);
            scrollDown();
            return;
        }

       
        JPanel wrapper = new JPanel(new FlowLayout(mine ? FlowLayout.LEFT : FlowLayout.RIGHT));
        wrapper.setOpaque(false);

        String time = new SimpleDateFormat("HH:mm").format(new Date());
        ChatBubble bubble = new ChatBubble(text, time, mine, textColor, false);

        wrapper.add(bubble);
        chatContainer.add(wrapper);

        scrollDown();
    }


    private void scrollDown() {
        SwingUtilities.invokeLater(() -> {
            chatContainer.revalidate();
            chatContainer.repaint();
            JScrollBar sb = ((JScrollPane) chatContainer.getParent().getParent()).getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
    }


    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
        dispose();
        System.exit(0);
    }

    private Color getUserColor(String name) {
        return userColors.computeIfAbsent(name, n -> {
            Random r = new Random(n.hashCode());
            return new Color(100 + r.nextInt(155), 100 + r.nextInt(155), 100 + r.nextInt(155));
        });
    }

    private String extractName(String msg) {
        int sp = msg.indexOf(":");
        if (sp != -1)
            return msg.substring(0, sp).trim();
        return "unknown";
    }

    public static void main(String[] args) {
        try {
            FlatAnimatedLafChange.showSnapshot();
            FlatDarkLaf.setup();
            UIManager.put("Button.arc", 20);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
            FlatAnimatedLafChange.hideSnapshotWithAnimation();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(ChatClient::new);
    }
}


class ChatBubble extends JPanel {

    private final String text;
    private final String time;
    private final boolean mine;
    private final boolean sys;
    private final Color textColor;

    ChatBubble(String text, String time, boolean mine, Color textColor, boolean sys) {
        this.text = text;
        this.time = time;
        this.mine = mine;
        this.textColor = textColor;
        this.sys = sys;

        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("<html>" + text + "</html>");
        label.setForeground(textColor);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        add(label, BorderLayout.CENTER);

        if (!sys) {
            JLabel t = new JLabel(time);
            t.setForeground(Color.LIGHT_GRAY);
            t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            add(t, BorderLayout.SOUTH);
        }

        setBorder(new EmptyBorder(8, 12, 8, 12));
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bubble = sys
                ? new Color(90, 90, 90, 140)
                : (mine ? new Color(0, 150, 90, 180) : new Color(70, 70, 70, 180));

        g2.setColor(bubble);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        super.paintComponent(g);
    }
}
