package com.example.applicationflow.exception;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class InvalidStatusException extends Throwable {

    public InvalidStatusException(@NotBlank @NotNull String message) {
        super(message);
    }
}
