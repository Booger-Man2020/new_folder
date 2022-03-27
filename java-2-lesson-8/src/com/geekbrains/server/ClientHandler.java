package com.geekbrains.server;

import com.geekbrains.server.authorization.JdbcConnector;

import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final BufferedWriter fileWriter;

    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.fileWriter = new BufferedWriter(new FileWriter("C:/Users/Aleh/Desktop/new_folder/java-2-lesson-8/src/com/geekbrains/client/chat_history.txt", true));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        authentication();
                        readMessages();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException exception) {
            throw new RuntimeException("Проблемы при создании обработчика");
        }
    }

    public void authentication() throws Exception {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(ServerCommandConstants.AUTHENTICATION)) {
                String[] authInfo = message.split(" ");
                String nickName = server.getAuthService().getNickNameByLoginAndPassword(authInfo[1], authInfo[2]);
                if (nickName != null) {
                    if (!server.isNickNameBusy(nickName)) {
                        sendAuthenticationMessage(true);
                        this.nickName = nickName;
                        server.broadcastMessage(nickName + " зашел в чат");
                        sendMessage(server.getClients());
                        server.addConnectedUser(this);
                        return;
                    } else {
                        sendAuthenticationMessage(false);
                    }
                } else {
                    sendAuthenticationMessage(false);
                }
            }
        }
    }

    private void sendAuthenticationMessage(boolean authenticated) throws IOException {
        outputStream.writeBoolean(authenticated);
    }

    private void readMessages() throws IOException {
        while (true) {
            String messageInChat = inputStream.readUTF();
            System.out.println("от " + nickName + ": " + messageInChat);
            if (messageInChat.equals(ServerCommandConstants.EXIT)) {
                closeConnection();
                return;
            }
            server.broadcastMessage(nickName + ": " + messageInChat);
                fileWriter.write(nickName + ": " + messageInChat);
                fileWriter.append("\n");

        }
    }



    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void closeConnection() {
        server.disconnectUser(this);
        server.broadcastMessage(ServerCommandConstants.EXIT + " " + nickName);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
            JdbcConnector.disconnect();
            fileWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
