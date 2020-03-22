package ru.geekbrains.java2.server.auth;

public interface AuthService {

    String getUsernameByLoginAndPassword(String login, String password);

    void start();
    void stop();

}
