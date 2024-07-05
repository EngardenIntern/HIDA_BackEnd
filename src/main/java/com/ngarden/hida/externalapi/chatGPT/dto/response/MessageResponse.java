package com.ngarden.hida.externalapi.chatGPT.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String assistantId;
    private String threadId;
    private String role;
    private String message;
}
