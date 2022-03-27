package com.geekbrains.client;

import com.geekbrains.CommonConstants;
import com.geekbrains.server.ServerCommandConstants;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

public class Network {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private static BufferedReader fileReader;
    private static ArrayList<String> memory = new ArrayList<>();

    private final ChatController controller;

    public Network(ChatController chatController) {
        this.controller = chatController;
    }

    private void startReadServerMessages() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String messageFromServer = inputStream.readUTF();
                        System.out.println(messageFromServer);
                        if (messageFromServer.contains("зашел в чат")) {
                            String[] client = messageFromServer.split(" ");
                            controller.displayClient(client[0]);
                        } else if (messageFromServer.startsWith(ServerCommandConstants.EXIT)) {
                            String[] client = messageFromServer.split(" ");
                            controller.removeClient(client[1]);
                            controller.displayMessage(client[1] + " покинул чат");
                        } else if (messageFromServer.startsWith(ServerCommandConstants.CLIENTS)) {
                            String[] client = messageFromServer.split(" ");
                            for (int i = 1; i < client.length; i++) {
                                controller.displayClient(client[i]);
                            }
                        } else {
                            controller.displayMessage(messageFromServer);

                        }
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    private void initializeNetwork() throws IOException {
        socket = new Socket(CommonConstants.SERVER_ADDRESS, CommonConstants.SERVER_PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }


    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean sendAuth(String login, String password)  {
        try {
            if (socket == null || socket.isClosed()) {
                initializeNetwork();
            }
            outputStream.writeUTF(ServerCommandConstants.AUTHENTICATION + " " + login + " " + password);

            boolean authenticated = inputStream.readBoolean();
            if (authenticated) {
                readChatHistory();
                startReadServerMessages();
            }
            return authenticated;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void closeConnection() {
        try {
            outputStream.writeUTF(ServerCommandConstants.EXIT);
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        System.exit(1);
    }
    public void readChatHistory() throws IOException {
        fileReader = new BufferedReader(new FileReader("C:/Users/Aleh/Desktop/new_folder/java-2-lesson-8/src/com/geekbrains/client/chat_history.txt"));
        String line;
        int current = 0;
        while ((line = fileReader.readLine()) != null) {
            memory.add(line);
            current++;
        }
        if (current<=100){
            for (int i = current; i > 0; i--){
                controller.displayMessage(memory.get(i));
            }
        }else{
            for (int i = current; i >= current - 100; i--) {
                controller.displayMessage(memory.get(i));
            }
        }
    }

}
