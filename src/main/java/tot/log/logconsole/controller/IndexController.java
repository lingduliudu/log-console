package tot.log.logconsole.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import tot.log.logconsole.entity.MicroserviceConfig;
import tot.log.logconsole.http.Result;
import tot.log.logconsole.imapper.IMicroserviceConfigMapper;
import tot.log.logconsole.query.SearchRequest;
import tot.log.logconsole.ssh.SshCommand;
import tot.log.logconsole.tool.DeduplicationTool;

@RestController
public class IndexController {

	@Autowired
	private IMicroserviceConfigMapper mapper;
	@PostMapping("getdata")
	public Result getData(SearchRequest search){
		// 检测查询是否合法
		if(search.getMicroserviceCode()==null || "".equals(search.getMicroserviceCode())) {
			return Result.error();
		}
		if(search.getIsTail()==1) {
			// 特殊处理
			// 处理
			return Result.success();
		}
		MicroserviceConfig entity = new MicroserviceConfig();
		entity.setMicroserviceCode(search.getMicroserviceCode());
		entity.setEnv(search.getEnv());
		entity.setState(1);
		Example<MicroserviceConfig> searchEntity = Example.of(entity);
		List<MicroserviceConfig> list = mapper.findAll(searchEntity);
		List<String> allResult = new ArrayList<String>();
		// 准备连接中
		new SshCommand().readyConnect(list);
		for(MicroserviceConfig mc:list) {
			// 查找当天的
			mc.setFileName(mc.getLogFilePattern().replaceAll(Pattern.quote("${yyyy-mm-dd}"), search.getDate()));
			String beginTime = search.getBeginTime();
			String endTime = search.getEndTime();
			//	
			Date date = DateUtil.parse(endTime);
			DateTime newEndTime = DateUtil.offsetMinute(date, 1);
			endTime =DateUtil.format(newEndTime, "HH:mm:ss");
			// 获取第一行的数据
			List<String> beginResult = SshCommand.execCommand(mc, "grep -n "+"'"+beginTime+"'"+" "+mc.getLogPath()+"/"+mc.getFileName() +" |head -n 500 ");
			List<String> endResult = SshCommand.execCommand(mc, "grep -n "+"'"+endTime+"'"+" "+mc.getLogPath()+"/"+mc.getFileName() +" |head -n 1 ");
			List<String> totalLine = SshCommand.execCommand(mc, "cat "+mc.getLogPath()+"/"+mc.getFileName() +" |wc -l");
			long total = Long.parseLong(totalLine.get(0).split(Pattern.quote(":"))[0]);
			if(beginResult.isEmpty()) {
				// 未找到数据
				continue;
			}
			// 最后都没有则全部都是范围内
			if(!endResult.isEmpty()) {
				total = Long.parseLong(totalLine.get(0).split(Pattern.quote(":"))[0]); 
			}
			// 
			Long beginLine = Long.parseLong(beginResult.get(0).split(Pattern.quote(":"))[0]);
			List<String> result = new ArrayList<String>();
			
			allResult.addAll(result);
		}
		Result r = Result.success();
		r.setTotal(allResult.size());
		r.setData(allResult);
		return r;
	}
	
	
	
	@PostMapping("getmicroservicecode")
	public Result getMicroservicecode(){
		// 检测查询是否合法
		MicroserviceConfig entity = new MicroserviceConfig();
		entity.setState(1);
		Example<MicroserviceConfig> searchEntity = Example.of(entity);
		List<MicroserviceConfig> list = mapper.findAll(searchEntity);
		return Result.successData(list.stream().filter(DeduplicationTool.distinctByKey(t->t.getMicroserviceCode())).collect(Collectors.toList()));
	}
	@PostMapping("getenv")
	public Result getenv(){
		// 检测查询是否合法
		MicroserviceConfig entity = new MicroserviceConfig();
		entity.setState(1);
		Example<MicroserviceConfig> searchEntity = Example.of(entity);
		List<MicroserviceConfig> list = mapper.findAll(searchEntity);
		return Result.successData(list.stream().filter(DeduplicationTool.distinctByKey(t->t.getEnv())).collect(Collectors.toList()));
	}
}
