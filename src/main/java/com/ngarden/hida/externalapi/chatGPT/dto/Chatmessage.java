package com.ngarden.hida.externalapi.chatGPT.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Chatmessage {
    private String role;
    private String content;
}
