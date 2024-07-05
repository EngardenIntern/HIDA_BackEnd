package com.ngarden.hida.externalapi.chatGPT.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIThread {
    private List<Chatmessage> messages;
}
