package ru.geekbrains.java2.client.command;

import java.io.Serializable;

public class ChangeUsernameCommand implements Serializable {

    private final String receiver;
    private final String message;

    public ChangeUsernameCommand(String receiver, String message) {
        this.receiver = receiver;
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }
}
