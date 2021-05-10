package tot.log.logconsole.websocket;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import tot.log.logconsole.GlobalTool;
import tot.log.logconsole.SpringTool;
import tot.log.logconsole.entity.MicroserviceConfig;
import tot.log.logconsole.imapper.IMicroserviceConfigMapper;
import tot.log.logconsole.ssh.SshCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.data.domain.Example;

@Slf4j
public class ServerWebChildHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

	ExecutorService executorService = Executors.newFixedThreadPool(10);
	public static Map<String,String> channels = new HashMap();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		log.info("客户端消息:" + msg.text());
		channels.put(ctx.channel().id().asLongText(), "");
		JSONObject reqMsg = JSONObject.parseObject(msg.text());
		String code = reqMsg.getString("code");
		String date = reqMsg.getString("date");
		String env = reqMsg.getString("env");
		String traceId = reqMsg.getString("traceId");
		// 获取到服务地址
		MicroserviceConfig entity = new MicroserviceConfig();
		entity.setMicroserviceCode(code);
		entity.setEnv(env);
		entity.setState(1);		
		Example<MicroserviceConfig> searchEntity = Example.of(entity);
		IMicroserviceConfigMapper mapper = SpringTool.getBean(IMicroserviceConfigMapper.class);
		List<MicroserviceConfig> list = mapper.findAll(searchEntity);
		new SshCommand().readyConnect(list);
		for (MicroserviceConfig mc : list) {
			executorService.execute(new Runnable() {
				public void run() {
					//
						new SshCommand().execCommandTail(mc,ctx.channel(),date,traceId);
					}
				}
			);
		}
	}
	

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		channels.remove(ctx.channel().id().asLongText());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// 移除对应的客户
		channels.remove(ctx.channel().id().asLongText());
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
			}
		}
	}

}
