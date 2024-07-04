package com.ngarden.hida.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NoExistException extends RuntimeException{
    public NoExistException(String message){
        super(message);
    }
}
