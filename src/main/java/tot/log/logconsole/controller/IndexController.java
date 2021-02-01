package tot.log.logconsole.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.util.StringUtils;
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
import tot.log.logconsole.tool.HelpTool;
import tot.log.logconsole.tool.RequestValidTool;

@RestController
public class IndexController {

	@Autowired
	private IMicroserviceConfigMapper mapper;
	@PostMapping("getdata")
	public Result getData(SearchRequest search){
		// 检测查询是否合法
		if(RequestValidTool.stop(search)) {
			return Result.error("搜索条件不规范");
		}
		if(isOnlyGrep(search)) {
			return getOnlyGrepData(search); 
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

			long beginLine = HelpTool.getBeginLine(mc, search);
			long endLine = HelpTool.getEndLine(mc, search);
			// 
			// 文件的总行数
			
			List<String> totalLines = SshCommand.execCommand(mc, "cat "+HelpTool.getFullFilePath(mc, search)+" |wc -l ");
			long totalLine = Long.parseLong(totalLines.get(0));
			if(beginLine>0) {
				if(endLine>0) {
					List<String> result = SshCommand.execCommand(mc, "sed -n '"+beginLine+","+endLine+"p' "+HelpTool.getFullFilePath(mc, search));
					allResult.addAll(result);
				}
				if(endLine<0) {
					List<String> result = SshCommand.execCommand(mc,"sed -n '"+beginLine+","+totalLine+"p' "+HelpTool.getFullFilePath(mc, search));
					allResult.addAll(result);
				}
			}
		}
		Result r = Result.success();
		if(StringUtils.isEmpty(search.getGrepContent())) {
			allResult = HelpTool.sortAscList(allResult);
		}else {
			allResult = HelpTool.sortAscList(allResult,search.getGrepContent());
		}
		r.setTotal(allResult.size());
		r.setData(HelpTool.sortAscList(allResult));
		return r;
	}
	
	public boolean isOnlyGrep(SearchRequest search) {
		if(!StringUtils.isEmpty(search.getGrepContent()) ) {
			if(StringUtils.isEmpty(search.getBeginTime()) || StringUtils.isEmpty(search.getEndTime())) {
				return true;
			}
		}
		return false;
		
	}
	public Result getOnlyGrepData(SearchRequest search) {
		MicroserviceConfig entity = new MicroserviceConfig();
		entity.setMicroserviceCode(search.getMicroserviceCode());
		entity.setEnv(search.getEnv());
		entity.setState(1);
		Example<MicroserviceConfig> searchEntity = Example.of(entity);
		List<MicroserviceConfig> list = mapper.findAll(searchEntity);
		// 开始查找各个文件的符合要求的数据
		// 准备连接中
		new SshCommand().readyConnect(list);
		List<String> allResult = new ArrayList<String>();
		// 仅仅是grep的过滤查询进行数据
		for(MicroserviceConfig mc:list) {
			List<String> result = SshCommand.execCommand(mc, "grep  "+"'"+search.getGrepContent()+"' "+HelpTool.getFullFilePath(mc, search));
			allResult.addAll(result);
		}
		return Result.successData(HelpTool.sortAscList(allResult));
		
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
