package tot.log.logconsole.ssh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cn.hutool.core.date.DateUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import tot.log.logconsole.GlobalTool;
import tot.log.logconsole.entity.MicroserviceConfig;
import tot.log.logconsole.websocket.ServerWebChildHandler;

@Slf4j
public class SshCommand {

	public static List<String> execCommand(MicroserviceConfig config,String cmd) {
		List<String> result = new ArrayList<String>();
		try {
			Session session = new SshCommand().getSession(config);
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(cmd);         //添加传入进来的shell命令
			channelExec.setInputStream(null);
			channelExec.setErrStream(System.err);//通道连接错误信息提示
			channelExec.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
			String msg;
			while ((msg = in.readLine()) != null) {
				result.add(msg);
			}
			in.close();
			channelExec.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public void execCommandTail(MicroserviceConfig config,Channel channel,String date) {
		try {
			if(date==null || "".equals(date)) {
				date  = DateUtil.today();
			}
			// 获取今天的
			String fullFilePath = config.getLogPath()+"/"+config.getLogFilePattern().replaceAll(Pattern.quote("${yyyy-mm-dd}"), date);
			String cmd = "tail -f "+fullFilePath;
			//log.info("tail命令:"+cmd);
			Session session = new SshCommand().getSession(config);
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(cmd);         //添加传入进来的shell命令
			channelExec.setInputStream(null);
			channelExec.setErrStream(System.err);//通道连接错误信息提示
			channelExec.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));
			String msg;
			while ((msg = in.readLine()) != null) {
				 if(channel == null) {
					 break;
				 }
				 if(!ServerWebChildHandler.channels.containsKey(channel.id().asLongText())) {
					 break;
				 }
				// log.info(msg);
				 TextWebSocketFrame twsf = new TextWebSocketFrame(msg);
				 channel.writeAndFlush(twsf);
			}
			in.close();
			channelExec.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public  void connect(MicroserviceConfig config) throws Exception {
		JSch jsch = new JSch();
		Session session = jsch.getSession(config.getSshUsername(), config.getSshIp(), config.getSshPort());
		session.setPassword(config.getSshPassword()); // 设置密码
		session.setUserInfo(new SshUserInfo());
		Properties prop = new Properties();
		prop.put("StrictHostKeyChecking", "no");//在代码里需要跳过检测。否则会报错找不到主机
		session.setConfig(prop); // 为Session对象设置properties
		int timeout = 300000;
		session.setTimeout(timeout); // 设置timeout时间
		session.connect(); // 通过Session建立与远程服务器的连接回话
		GlobalTool.sshs.put(config.getSshIp()+config.getMicroserviceCode(), session);
	}
	public  Session getSession(MicroserviceConfig config) throws Exception {
		if(GlobalTool.sshs.get(config.getSshIp()+config.getMicroserviceCode()) !=null &&  GlobalTool.sshs.get(config.getSshIp()+config.getMicroserviceCode()).isConnected()) {
			return GlobalTool.sshs.get(config.getSshIp()+config.getMicroserviceCode());
		}
		connect(config);
		return getSession(config);
	}
	
	public  void readyConnect(List<MicroserviceConfig> list) {
		if(list !=null && list.size()>0) {
			for(MicroserviceConfig config:list){
				if(GlobalTool.sshs.get(config.getSshIp()+config.getMicroserviceCode()) ==null ||  !GlobalTool.sshs.get(config.getSshIp()+config.getMicroserviceCode()).isConnected()) {
					try {
						connect(config);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
}
