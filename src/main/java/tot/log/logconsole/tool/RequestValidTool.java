package tot.log.logconsole.tool;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import tot.log.logconsole.query.SearchRequest;

public class RequestValidTool {

	public static boolean stop(SearchRequest s) {
		// 是否有微服务 和 环境
		if(StringUtils.isEmpty(s.getMicroserviceCode()) || StringUtils.isEmpty(s.getEnv())) {
			return true;
		}
		if(StringUtils.isEmpty(s.getGrepContent()) &&  StringUtils.isEmpty(s.getBeginTime())&& StringUtils.isEmpty(s.getEndTime())) {
			return true;
		}
		if(!StringUtils.isEmpty(s.getBeginTime()) && StringUtils.isEmpty(s.getEndTime())) {
			return true;
		}
		if(!StringUtils.isEmpty(s.getEndTime()) && StringUtils.isEmpty(s.getBeginTime())) {
			return true;
		}
		if(!StringUtils.isEmpty(s.getEndTime()) && StringUtils.isEmpty(s.getBeginTime())) {
			return true;
		}
		if(!StringUtils.isEmpty(s.getDate()) && !matchDate(s.getDate())) {
			return true;
		}
		if(!StringUtils.isEmpty(s.getBeginTime()) && !matchTime(s.getBeginTime())) {
			return true;
		}
		if(!StringUtils.isEmpty(s.getEndTime()) && !matchTime(s.getEndTime())) {
			return true;
		}
		if(!StringUtils.isEmpty(s.getGrepContent()) && matchIllegalGrep(s.getGrepContent()) ) {
			return true;
		}
		return false;
	}
	
	public static boolean matchTime(String content) {
		boolean isMatch = Pattern.matches("^[0-9]{2}:[0-9]{2}:[0-9]{2}$", content);
		return isMatch;
	}
	public static boolean matchDate(String content) {
		boolean isMatch = Pattern.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}$", content);
		return isMatch;
	}
	// 检测grep的合法性
	public static boolean matchIllegalGrep(String content) {
		// 匹配一下字符皆违法
		String[] illegals = {"|","rm "," rm","rm -","/","\\","'","\""};
		for(String illegal:illegals) {
			if(content.contains(illegal)) {
				return true;
			}
		}
		return false;
		
	}
}
