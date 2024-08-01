package com.ngarden.hida.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AlreadyExistException extends RuntimeException{
    public AlreadyExistException(String message){
        super(message);
    }
}
