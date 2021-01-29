package tot.log.logconsole.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
@Component
@Slf4j
public class WebSocketServer {

        @Value("${server.websocket.port}")
        private String webSocketPort;

        private EventLoopGroup mainWeb = new NioEventLoopGroup(); // 通过nio方式来接收连接和处理连接 websocket
        private  EventLoopGroup workWeb = new NioEventLoopGroup(); // 通过nio方式来接收连接和处理连接 websocket
        private ServerBootstrap bootStrapWeb = new ServerBootstrap();



        /**
         * @Title:  web 启动
         * @Package type_name
         * @author: yuanhao
         * @date:   2020年1月16日 下午3:56:16
         * @version lastest
         */
        public  void startWeb() throws Exception {
            // 创建连接
            bootStrapWeb.group(mainWeb, workWeb);
            bootStrapWeb.channel(NioServerSocketChannel.class);
            bootStrapWeb.childHandler(new ServerWebChildInitializer());
            if(StringUtils.isEmpty(webSocketPort)){
                throw new RuntimeException("启动 server web 失败,绑定端口号不能为空!");
            }
            bootStrapWeb.bind(Integer.parseInt(webSocketPort)).sync();
            log.info("WS 端口"+webSocketPort+"监听成功.......");

        }

        public void destory() {
            // 关闭  web
            mainWeb.shutdownGracefully();
            workWeb.shutdownGracefully();
        }

    }