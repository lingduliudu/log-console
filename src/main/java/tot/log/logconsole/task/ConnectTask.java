package tot.log.logconsole.task;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tot.log.logconsole.entity.MicroserviceConfig;
import tot.log.logconsole.imapper.IMicroserviceConfigMapper;
import tot.log.logconsole.ssh.SshCommand;

@Component
public class ConnectTask {

	@Autowired
	private IMicroserviceConfigMapper mapper;
	
	
	@Scheduled(cron = "0 0/10 * * * ?")
	public void autoConnect() {
		MicroserviceConfig entity = new MicroserviceConfig();
		entity.setState(1);
		Example<MicroserviceConfig> searchEntity = Example.of(entity);
		List<MicroserviceConfig> list = mapper.findAll(searchEntity);
		// 准备连接中
		new SshCommand().readyConnect(list);
	}
}
