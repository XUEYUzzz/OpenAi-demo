package com.example.demo.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.example.demo.po.JsonData;

@RestController
public class OpenAiController {
    @Resource
    private OpenAiChatModel chatModel;

    
    @RequestMapping("chat1")
    public JsonData chat1(String key){
        JsonData data=new JsonData();
        try {
            String echo= chatModel.call(key);
            data.setData(echo);
        }
        catch (Exception ex){
             data.setError(300);
             data.setMessage(ex.getMessage());
        }
        return data;
    }

    @RequestMapping("chat2")
    public JsonData chat2(String key,HttpSession session,double temp){
        JsonData data=new JsonData();
        List<Message>messages=(List<Message>)session.getAttribute("messages");
        if(messages==null){
            messages=new ArrayList<>();
            session.setAttribute("messages",messages);
            messages.add(new SystemMessage("你是一个聊天助理"));
        }
        if(messages.size()>21){
            messages.remove(1);
            messages.remove(1);
        }

        try{
            messages.add(new UserMessage(key));
            ChatResponse response=chatModel.call(new Prompt(messages, ChatOptionsBuilder.builder().withTemperature(temp).build()));
            data.setData(response);
            messages.add(new AssistantMessage(response.getResult().getOutput().getContent()));
        }
        catch (Exception ex){
            data.setError(300);
            data.setMessage(ex.getMessage());
        }
        return data;
    }
    @RequestMapping("test1")
    public  Map<String,String> test1(){
        Map<String, String> data=new HashMap<>();
        data.put("name","杨智");
        data.put("age","18");
        data.put("gender","男");
        data.put("identity","超高校级的程序员");
        return data;
    }
    @RequestMapping("test2")
    public JsonData test2(){
        JsonData data=new JsonData();
        data.setError(100);
        data.setMessage("事实上");
        return data;
    }
    @RequestMapping(path="flux",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(String key){
        Flux<String> flux = chatModel.stream(key);
        return flux.delayElements(Duration.ofMillis(200));
    }
    @RequestMapping(path="flux2",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public  Flux<JsonData> stream2(String key){
        Flux<String> flux = chatModel.stream(key);
        Flux<JsonData> ret=flux.map((s)->JsonData.ok(s));
        return ret.delayElements(Duration.ofMillis(100));
    }
}
