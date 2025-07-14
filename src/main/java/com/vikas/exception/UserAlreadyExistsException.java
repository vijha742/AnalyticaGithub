package com.vikas.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException() {
        super("The user already exists. Please check other groups..");
    }
}
