package com.vikas.exception;

public class UserDoesNotExistException extends RuntimeException {

    public UserDoesNotExistException(String username) {
        super("The user doesn't exist..make sure there isn't a typo and the " + username + " exists");
    }
}
