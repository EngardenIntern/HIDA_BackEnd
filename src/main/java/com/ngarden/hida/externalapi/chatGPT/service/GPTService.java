package com.ngarden.hida.externalapi.chatGPT.service;


import com.ngarden.hida.externalapi.chatGPT.dto.AIThread;
import com.ngarden.hida.externalapi.chatGPT.dto.request.CreateThreadAndRunRequest;
import com.ngarden.hida.externalapi.chatGPT.dto.response.CreateThreadAndRunResponse;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface GPTService {
    CreateThreadAndRunResponse createThreadAndRun(CreateThreadAndRunRequest request);
    List<MessageResponse> getListMessage(String threadId);
    CreateThreadAndRunRequest generateThreadAndRun(String assistantId);
}
