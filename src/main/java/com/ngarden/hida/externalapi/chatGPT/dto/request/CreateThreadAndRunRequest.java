package com.ngarden.hida.externalapi.chatGPT.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngarden.hida.externalapi.chatGPT.dto.AIThread;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateThreadAndRunRequest {
    @JsonProperty("assistant_id")
    private String assistantId;
    @JsonProperty("thread")
    private AIThread aiThread;
}
