## 如果觉得内容对你有帮助的话，点一下免费的star吧

## 1、引入依赖

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
```

继承的父类

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.11</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
```

## 2、配置文件

只需要配置项目的运行端口进行

```yaml
server:
  port: 8090
```

## 3、配置类

配置WebSocket运行节点，让Spring容器拿到运行节点

```java
@Configuration
public class WebSocketConfig {
    /**
     * 创建一个服务节点
     * @return ServerEndpointExporter
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

## 4、服务类

建立socket连接的路径是/hhu/{userId}

userId 用于识别连接，作为连接的唯一属性

```java
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
```

## 5、Controller类

写了两个get方法，分别是私发和群发

```java
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
```

## 6、开始测试

> [WebSocket测试网站： http://wstool.jackxiang.com/](http://wstool.jackxiang.com/)

上面的网站用于连接SocketServer

下面我们建立两个socket连接，userId分别是1 和 2

地址是：ws://ipv4:端口+server类上面的路径

![Snipaste_2022-12-09_21-17-22.png](https://hhuhahaha.oss-cn-hangzhou.aliyuncs.com/img/Snipaste_2022-12-09_21-17-22.png)![Snipaste_2022-12-09_21-18-04.png](https://hhuhahaha.oss-cn-hangzhou.aliyuncs.com/img/Snipaste_2022-12-09_21-18-04.png)发送get请求测试连接

下图是用IDEA中的插件RestServices，发送的请求

## ![Snipaste_2022-12-09_21-20-14.png](https://hhuhahaha.oss-cn-hangzhou.aliyuncs.com/img/Snipaste_2022-12-09_21-20-14.png)![Snipaste_2022-12-09_21-21-22.png](https://hhuhahaha.oss-cn-hangzhou.aliyuncs.com/img/Snipaste_2022-12-09_21-21-22.png)![Snipaste_2022-12-09_21-24-53.png](https://hhuhahaha.oss-cn-hangzhou.aliyuncs.com/img/Snipaste_2022-12-09_21-24-53.png)7、代码下载连接

阿里云OSS：https://hhuhahaha.oss-cn-hangzhou.aliyuncs.com/code/WebSocketDemo1.zip

GitHub: https://github.com/llllike/WebSocketDemo1

Gitee: https://gitee.com/jk_2_yu/WebSocketDemo1
