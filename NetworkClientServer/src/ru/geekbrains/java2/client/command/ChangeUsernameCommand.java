package ru.geekbrains.java2.client.command;

import java.io.Serializable;

public class ChangeUsernameCommand implements Serializable {

    private final String oldUsername;
    private final String newUsername;

    public ChangeUsernameCommand(String oldUsername, String newUsername) {
        this.oldUsername = oldUsername;
        this.newUsername = newUsername;
    }

    public String getOldUsername() {
        return oldUsername;
    }

    public String getNewUsername() {
        return newUsername;
    }
}
