package com.ngarden.hida.externalapi.chatGPT.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngarden.hida.externalapi.chatGPT.config.ChatGPTConfig;
import com.ngarden.hida.externalapi.chatGPT.dto.AIThread;
import com.ngarden.hida.externalapi.chatGPT.dto.Chatmessage;
import com.ngarden.hida.externalapi.chatGPT.dto.request.CreateThreadAndRunRequest;
import com.ngarden.hida.externalapi.chatGPT.dto.response.CreateThreadAndRunResponse;
import com.ngarden.hida.externalapi.chatGPT.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GPTServiceImpl implements GPTService{

    private final ChatGPTConfig chatGPTConfig;

    @Override
    public CreateThreadAndRunResponse createThreadAndRun(CreateThreadAndRunRequest request) {

        CreateThreadAndRunResponse response = null;

        HttpHeaders headers = chatGPTConfig.httpHeaders();
        ResponseEntity<String> APIresponse = chatGPTConfig
                .restTemplate()
                .exchange("https://api.openai.com/v1/threads/runs", HttpMethod.POST, new HttpEntity<>(request, headers), String.class);

        try {
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> data = om.readValue(APIresponse.getBody(), new TypeReference<>() {
            });
            CreateThreadAndRunResponse APIresult = CreateThreadAndRunResponse.builder()
                    .id(data.get("id").toString())
                    .assistantId(data.get("assistant_id").toString())
                    .threadId(data.get("thread_id").toString())
                    .build();
            response = APIresult;
        } catch (JsonMappingException e) {
        log.debug("JsonMappingException :: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.debug("JsonProcessingException :: " + e.getMessage());
        } catch (RuntimeException e) {
            log.debug("RuntimeException :: " + e.getMessage());
        }

        return response;
    }

    @Override
    public List<MessageResponse> getListMessage(String threadId) {

        final String url = "https://api.openai.com/v1/threads/" + threadId + "/messages";
        Map<String, Object> result = new HashMap<>();

        HttpHeaders headers = chatGPTConfig.httpHeaders();
        ResponseEntity<String> APIresponse = chatGPTConfig
                .restTemplate()
                .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        try {
            ObjectMapper om = new ObjectMapper();
            result = om.readValue(APIresponse.getBody(), new TypeReference<>() {
            });
        } catch (JsonMappingException e) {
            log.debug("JsonMappingException :: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.debug("JsonProcessingException :: " + e.getMessage());
        } catch (RuntimeException e) {
            log.debug("RuntimeException :: " + e.getMessage());
        }
        log.debug("result: " + result.toString());

        List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.get("data");
        log.debug("dataList" + dataList.toString());

        List<MessageResponse> messageResponseList = new ArrayList<>();

        for(Map<String, Object> object : dataList){
            if(object.get("role").toString().equals("user")){
                continue;
            }
            MessageResponse messageResponse = MessageResponse.builder()
                    .id(object.get("id").toString())
                    .assistantId(object.get("assistant_id").toString())
                    .threadId(object.get("thread_id").toString())
                    .role(object.get("role").toString())
                    .build();
            List<Map<String, Object>> contentList = (List<Map<String, Object>>) object.get("content");
            if(!contentList.isEmpty()){
                Map<String, Object> content = contentList.get(0);
                Map<String, Object> text = (Map<String, Object>) content.get("text");
                messageResponse.setMessage(text.get("value").toString());
            }
            messageResponseList.add(messageResponse);
        }
        return messageResponseList;
    }

    @Override
    public CreateThreadAndRunRequest generateThreadAndRun(String prompt, String assistantId) {

        List<Chatmessage> chatmessageList = new ArrayList<>();

        Chatmessage chatmessage = Chatmessage.builder()
                .role("user")
                .content(prompt).build();
        chatmessageList.add(chatmessage);

        AIThread aiThread = AIThread.builder()
                .messages(chatmessageList).build();

        return CreateThreadAndRunRequest.builder()
                .assistantId(assistantId)
                .aiThread(aiThread)
                .build();
    }
}