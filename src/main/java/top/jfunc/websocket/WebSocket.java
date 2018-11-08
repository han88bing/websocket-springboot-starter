package top.jfunc.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.jfunc.websocket.utils.BasicThreadFactory;
import top.jfunc.websocket.utils.SpringContextHolder;

import javax.websocket.Session;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiongshiyan at 2018/10/10 , contact me with email yanshixiong@126.com or phone 15208384257
 */
public class WebSocket {
    private static final Logger logger = LoggerFactory.getLogger(WebSocket.class);

    private String identifier;
    private Session session;

    /**
     * 管理"我"的WebSocketManager
     */
    private WebSocketManager webSocketManager;

    /**
     * 是否是活的
     */
    private boolean alive = true;

    /**
     * 错误次数
     */
    private int errorCount = 0;
    /**
     * 最大错误次数
     */
    private static final int MAX_ERROR_COUNT = 3;
    /**
     * 初始监测延时
     */
    private static final int INIT_DELAY = 1;
    /**
     * 心跳时间间隔
     */
    private static final int BEAT_HEART_INTERVAL = 10;

    private ScheduledExecutorService scheduledExecutorService = null;


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    public void setWebSocketManager(WebSocketManager webSocketManager) {
        this.webSocketManager = webSocketManager;
    }

    /**
     * 开始检测客户端心跳,延时一秒之后检测,每十秒检测一次,如果超过三次都失败那么认为此WebSocket已经断开连接了
     */
    public void beginCheck(){
        if(null == scheduledExecutorService){
            scheduledExecutorService = new ScheduledThreadPoolExecutor(1 ,
                    new BasicThreadFactory.Builder().namingPattern("check websocket beatheart thread %d").daemon(true).build());
        }
        scheduledExecutorService.scheduleWithFixedDelay(this::doCheck ,
                INIT_DELAY , BEAT_HEART_INTERVAL , TimeUnit.SECONDS);
    }
    private void doCheck(){
        logger.info("alive = " + alive + " , errorCount = " + errorCount);
        if(this.errorCount > MAX_ERROR_COUNT){
            //移除
            webSocketManager.remove(this.getIdentifier());
            //出错次数达到上限,那么发送错误的事件消息
            SpringContextHolder.getApplicationContext().publishEvent(
                    new WebSocketCloseEvent(this.getIdentifier()));
            return;
        }
        //如果没有心跳将此置为true
        if(!this.alive){
            this.errorCount ++ ;
        }
        //本身check将之置为false
        this.alive = false;
    }

    /**心跳包将之置为true*/
    public void keepAlive(){
        this.alive = true;
        this.errorCount = 0;
    }
}
