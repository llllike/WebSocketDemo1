package com.socket.server;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author yzy
 * @create 2022-12-09-20:35
 */
@Data
@Slf4j
@Component
@ServerEndpoint("/hhu/{userId}")
public class WebSocketServer {
    /**
     * 会话
     */
    private Session session;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 存储会话的set集合
     */
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 存储会话的map集合  用户id作为key ，value作为session
     */
    private static ConcurrentHashMap<String,WebSocketServer> webSocketMap = new ConcurrentHashMap();
    /**
     * 建立连接
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        webSocketSet.add(this);
        webSocketMap.put(userId,this);
        log.info("[ID:{}]建立连接 \n 当前连接数:{}", this.userId, webSocketMap.size());
    }

    /**
     * 断开连接
     **/
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        webSocketMap.remove(userId);
        log.info("[ID:{}] 断开连接 \n 当前连接数:{}", userId, webSocketMap.size());
    }

    /**
     * 发送错误
     **/
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("[ID:{}] 错误原因:{}", this.userId, error.getMessage());
        error.printStackTrace();
    }

    /**
     * 收到消息
     **/
    @OnMessage
    public void onMessage(String message) {
        log.info("收到[ID:{}] 发送的消息:{}", this.userId, message);
    }

    /**
     * 私发消息
     */
    public void sendMessage(String message,String userId) {
        WebSocketServer webSocketServer = webSocketMap.get(userId);
        if (webSocketServer!=null){
            try {
                webSocketServer.session.getBasicRemote().sendText(message);
                log.info("【私发消息成功】,to={},message={}", userId,message);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("【私发消息失败】,to={},message={},error={}",userId, message,e.getMessage());
            }
        }else {
            log.warn("【私发消息失败】,to={},message={},error={}", userId,message,"用户不在线");
        }
    }
    /**
     * 群发消息
     */
    public void sendMassMessage(String message) {
        webSocketSet.forEach(o-> {
            try {
                o.session.getBasicRemote().sendText(message);
                log.info("【群发消息成功】,message={}",message);
            } catch (IOException e) {
                log.info("【群发消息异常】,message={},error={}",message,e.getMessage());
                e.printStackTrace();
            }
        });
    }
}