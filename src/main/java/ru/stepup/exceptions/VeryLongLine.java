package ru.stepup.exceptions;

public class VeryLongLine extends RuntimeException {

    public VeryLongLine(String message) {
        super(message);
    }
}