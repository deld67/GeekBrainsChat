package ru.geekbrains.java2.server;

import ru.geekbrains.java2.client.Command;
import ru.geekbrains.java2.server.auth.AuthService;
import ru.geekbrains.java2.server.auth.BaseAuthService;
import ru.geekbrains.java2.server.auth.PostgreSQLAuthService;
import ru.geekbrains.java2.server.client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;
import java.util.stream.Collectors;

public class NetworkServer {
    private static final Logger logger = Logger.getLogger( NetworkServer.class.getName() );
    private final int port;
    //    private final List<ClientHandler> clients = new ArrayList<>();
//    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final AuthService authService;

    private static final int CONNECTION_TIMEOUT = 120000;

    public NetworkServer(int port) {
        this.port = port;
        //this.authService = new BaseAuthService();
        this.authService = new PostgreSQLAuthService();
        try {
            Handler h =new FileHandler("NeworkChatServer.log");
            h.setLevel( Level.ALL );
            h.setFormatter(new SimpleFormatter());
            logger.addHandler(h);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер был успешно запущен на порту " + port);
            logger.log(Level.INFO, "Сервер был успешно запущен на порту " + port);

            authService.start();
            while (true) {
                System.out.println("Ожидание клиентского подключения...");
                logger.log(Level.INFO, "Ожидание клиентского подключения...");
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(CONNECTION_TIMEOUT);
                System.out.println("Клиент подлючился");
                logger.log(Level.INFO,"Клиент подлючился");
                createClientHandler(clientSocket);

            }
        }catch (IOException e) {
            System.out.println("Ошибка при работе сервера");
            logger.log(Level.SEVERE,"Ошибка при работе сервера",e);
            e.printStackTrace();
        } finally {
            authService.stop();
            logger.log(Level.INFO,"Сервер был успешно  остановлен");
        }
    }

    private void  createClientHandler(Socket clientSocket) {
        ClientHandler clientHandler = new ClientHandler(this, clientSocket);
        ExecutorService executorService = Executors.newFixedThreadPool( 1 );
        executorService.execute( new Runnable() {
            @Override
            public void run() {
                clientHandler.run();
            }
        } );

    }

    public AuthService getAuthService() {
        return authService;
    }

    public /*synchronized*/ void broadcastMessage(Command message, ClientHandler owner) throws IOException {
        for (ClientHandler client : clients) {
            if (client != owner) {
                client.sendMessage(message);
            }
        }
        if (owner != null)
            logger.log(Level.INFO,"Пользователь "+owner.getNickname()+" отправил сообщние всем пользователям");
    }

    public /*synchronized*/ void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        List<String> users = getAllUsernames();
        broadcastMessage(Command.updateUsersListCommand(users), null);

        logger.log(Level.INFO,"Пользователь "+clientHandler.getNickname()+" вошел в чат");
    }

    public /*synchronized*/ void unsubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
        List<String> users = getAllUsernames();
        broadcastMessage(Command.updateUsersListCommand(users), null);
        logger.log(Level.INFO,"Пользователь "+clientHandler.getNickname()+" вышел из чата");
    }

    private List<String> getAllUsernames() {
//        return clients.stream()
//                .map(client -> client.getNickname())
//                .collect(Collectors.toList());
        List<String> usernames = new LinkedList<>();
        for (ClientHandler clientHandler : clients) {
            usernames.add(clientHandler.getNickname());
        }
        return usernames;
    }

    public /*synchronized*/ void sendMessage(String receiver, Command commandMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(receiver)) {
                client.sendMessage(commandMessage);
                logger.log(Level.INFO,"Пользователю "+client.getNickname()+" направлено персональное сообщение");
                break;
            }
        }

    }

    public boolean isNicknameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(username)) {
                return true;
            }
        }
        return false;
    }
    public void updateUserName(String newUsername, String oldUsername) throws IOException {
        getAuthService().updateUsername(newUsername, oldUsername);
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(oldUsername)) {
                client.setNickname(newUsername);
                break;
            }
        }
        List<String> users = getAllUsernames();
        broadcastMessage(Command.updateUsersListCommand(users), null);

    }
}
