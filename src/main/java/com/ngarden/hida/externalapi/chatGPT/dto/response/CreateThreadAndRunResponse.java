package com.ngarden.hida.externalapi.chatGPT.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateThreadAndRunResponse {
    private String id;

    private String assistantId;

    private String threadId;
}
