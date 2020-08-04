package highest.flow.taobaolive.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MinaService {

    private static Logger logger = LoggerFactory.getLogger(MinaService.class);

    private static List<IoSession> sessiones = new CopyOnWriteArrayList<>();

    private static Random random = new Random();

    public static String sendMessage(String message){
        Map<String,Object> result =  new HashMap<String,Object>();
        result.put("ret",100);

        IoSession session = getSessions();
        if(session == null){
            result.put("ret",500);
            result.put("msg","没有可用的设备");
        }else{
            session.getConfig().setUseReadOperation(true);
            WriteFuture future = session.write(message); // 发送数据
            future.awaitUninterruptibly(); // 等待发送数据操作完成
            if (future.getException() != null) {
                throw new RuntimeException(future.getException().getMessage());
            }
            if (future.isWritten()) {
                // 数据已经被成功发送
                ReadFuture readFuture = session.read();
                readFuture.awaitUninterruptibly();
                if (readFuture.getException() != null) {
                    throw new RuntimeException(readFuture.getException().getMessage());
                }
                String resultMsg = readFuture.getMessage().toString();
                result.put("msg","签名成功");
                result.put("data", JsonParserFactory.getJsonParser().parseMap(resultMsg));
            } else {
                result.put("msg","签名数据发送失败");
                result.put("data",null);
            }
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(result);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private synchronized  static IoSession getSessions() {
        if(sessiones == null  || sessiones.size() == 0){
            return null;
        }
        return sessiones.get(random.nextInt(sessiones.size()));
    }


    public static void start(){
        logger.info("准备启动");
        IoAcceptor acceptor = new NioSocketAcceptor();
        //添加日志过滤器
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        acceptor.setHandler(new DemoServerHandler());
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        try {
            acceptor.bind(new InetSocketAddress(9228));
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("启动服务");
    }

    /**
     * @ClassName: DemoServerHandler
     * @Description: 负责session对象的创建和监听以及消息的创建和接收监听
     * @author chenzheng
     * @date 2016-12-9 下午3:57:11
     */
    private static class DemoServerHandler extends IoHandlerAdapter {

        //服务器与客户端创建连接
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            logger.info("服务器与客户端创建连接...");
            super.sessionCreated(session);
            sessiones.add(session);
        }


        @Override
        public void sessionOpened(IoSession session) throws Exception {
            logger.info("服务器与客户端连接打开...");
            super.sessionOpened(session);
        }

        //消息的接收处理
        @Override
        public void messageReceived(IoSession session, Object message)
                throws Exception {
            super.messageReceived(session, message);
            String str = message.toString();
            Date date = new Date();
            session.write(date.toString());
            logger.info("接收到的数据："+str);

        }

        @Override
        public void messageSent(IoSession session, Object message)
                throws Exception {
            super.messageSent(session, message);
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
            sessiones.remove(session);
        }
    }
}
