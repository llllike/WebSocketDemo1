package com.socket.controller;

import com.socket.server.WebSocketServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author yzy
 * @create 2022-12-09-20:58
 */
@RestController
@RequestMapping("/ws")
public class WebSocketController {
    @Resource
    private WebSocketServer webSocketServer;
    /**
     * 私发消息
     */
    @GetMapping("/sendToOne/{userId}/{msg}")
    public void send(@PathVariable("userId") String userId, @PathVariable("msg") String msg){
        webSocketServer.sendMessage(msg,userId);
    }
    /**
     * 群发消息
     */
    @GetMapping("/sendToAll/{msg}")
    public void sendMassMessage(@PathVariable("msg") String msg){
        webSocketServer.sendMassMessage(msg);
    }
}
