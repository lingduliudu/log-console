package tot.log.logconsole.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import tot.log.logconsole.entity.MicroserviceConfig;
import tot.log.logconsole.query.SearchRequest;
import tot.log.logconsole.ssh.SshCommand;

public class HelpTool {

	public static String getFullFilePath(MicroserviceConfig config,SearchRequest s) {
		if(s.getDate() == null || "".equals(s.getDate())) {
			s.setDate(DateUtil.today());
		}
		return config.getLogPath()+"/"+config.getLogFilePattern().replaceAll(Pattern.quote("${yyyy-mm-dd}"), s.getDate());
	}
	public static void initDate(SearchRequest s) {
		if(s.getDate() == null || "".equals(s.getDate())) {
			s.setDate(DateUtil.today());
		}
		if(s.getBeginTime() == null || "".equals(s.getBeginTime())) {
			s.setBeginTime("00:00:00");
		}
		if(s.getEndTime() == null || "".equals(s.getEndTime())) {
			s.setEndTime("23:59:59");
		}
	}

	public static List<String> sortAscList(List<String> allResult) {
		// 重新排序
		List<String> reult = new ArrayList<String>();
		if(allResult.isEmpty()) {
			return reult;
		}
		SortedMap<String,String> sortedMap = new TreeMap<String,String>();
		// 在上一个时间的基础上加一好秒
		String preTime = "";
		for(String str:allResult) {
			if(str!=null&& str.startsWith("2")) {
				preTime =  str.substring(0,23);
				String key = str.substring(0,23);
				sortedMap.put(key, str);
			}else {
				String key = HelpTool.add1Ms(preTime);
				sortedMap.put(key, str);
			}
		}
		for(Entry<String, String> temp :sortedMap.entrySet()){
			reult.add(temp.getValue());  
		}
		return reult;
	}
	public static List<String> sortAscList(List<String> allResult,String grep) {
		// 重新排序
		List<String> reult = new ArrayList<String>();
		if(allResult.isEmpty()) {
			return reult;
		}
		SortedMap<String,String> sortedMap = new TreeMap<String,String>();
		String preTime = "";
		for(String str:allResult) {
			if(str!=null&& str.startsWith("2")) {
				preTime =  str.substring(0,23);
				String key = str.substring(0,23);
				sortedMap.put(key, str);
			}else {
				String key = HelpTool.add1Ms(preTime);
				sortedMap.put(key, str);
			}
		}
		for(Entry<String, String> temp :sortedMap.entrySet()){
			if(temp.getValue().contains(grep)) {
				reult.add(temp.getValue());  
			}
		}
		return reult;
	}
	
	public static long getBeginLine(MicroserviceConfig mc,SearchRequest s) {
		initDate(s);
		String preBeginTime = s.getDate()+" "+s.getBeginTime();
		// 先判断是否可以直接定位
		List<String> result = SshCommand.execCommand(mc, "grep -n "+"'"+preBeginTime+"'"+" "+getFullFilePath(mc,s) +" |head -n 1 ");
		// 找到了开始的行
		if(ListTool.isNotEmpty(result)) {
			return Long.parseLong(result.get(0).split(Pattern.quote(":"))[0]);
		}
		// 没找到
		Map<String,List<String>> checkPointer = checkPointer(s.getBeginTime());
		List<String> hours =checkPointer.get("prehours");
		List<String> minutes = checkPointer.get("preminutes");
		List<String> seconds = checkPointer.get("preseconds");
		String[] times = s.getBeginTime().split(Pattern.quote(":"));
		// 三层循环取
		for(String hour:hours) {
			String findTime = s.getDate()+" "+hour+":";
			List<String> hourResult = SshCommand.execCommand(mc, "grep -n "+"'"+findTime+"'"+" "+getFullFilePath(mc,s) +" |head -n 1 ");
			// 找到了开始的行
			if(ListTool.isNotEmpty(hourResult)) {
				// 开始判断分
				for(String minute:minutes) {
					// 开始判断
					if(hour.equals(times[0])) {
						if(minute.compareTo(times[1])<0) {
							continue;
						}
					}
					findTime = s.getDate()+" "+hour+":"+minute+":";
					List<String> minuteResult = SshCommand.execCommand(mc, "grep -n "+"'"+findTime+"'"+" "+getFullFilePath(mc,s) +" |head -n 1 ");
					if(ListTool.isNotEmpty(minuteResult)) {
						for(String second:seconds) {
							if(hour.equals(times[0]) && minute.equals(times[1])) {
								if(second.compareTo(times[2])<0) {
									continue;
								}
							}
							findTime = s.getDate()+" "+hour+":"+minute+":"+second;
							List<String> secondResult = SshCommand.execCommand(mc, "grep -n "+"'"+findTime+"'"+" "+getFullFilePath(mc,s) +" |head -n 1 ");
							if(ListTool.isNotEmpty(secondResult)) {
								// 找到了
								return Long.parseLong(secondResult.get(0).split(Pattern.quote(":"))[0]);
							}
						}
					}
				}
			}
			
		}
		return -1;
		
	}
	public static Map<String,List<String>> checkPointer(String time){
		String[] times = time.split(Pattern.quote(":"));
		List<String> hours = new ArrayList<>();
		List<String> minutes = new ArrayList<>();
		List<String> seconds = new ArrayList<>();
		
		List<String> prehours = new ArrayList<>();
		List<String> preminutes = new ArrayList<>();
		List<String> preseconds = new ArrayList<>();
		int maxHour = 23;
		int maxMinute = 59;
		int maxSecond = 59;
		// 开始判断  前置
		for(int i = Integer.parseInt(times[0].equals("00")?"0":times[0]);i<=maxHour;i++) {
			if(i<10) {
				prehours.add("0"+i);
				continue;
			}
			prehours.add(""+i);
		}
		for(int i = 0;i<=maxMinute;i++) {
			if(i<10) {
				preminutes.add("0"+i);
				continue;
			}
			preminutes.add(""+i);
		}
		// 开始判断
		for(int i = 0;i<=maxSecond;i++) {
			if(i<10) {
				preseconds.add("0"+i);
				continue;
			}
			preseconds.add(""+i);
		}
		// 开始判断
		
		for(int i = Integer.parseInt(times[0].equals("00")?"0":times[0]);i>=0;i--) {
			if(i<10) {
				hours.add("0"+i);
				continue;
			}
			hours.add(""+i);
		}

		for(int i = maxMinute;i>=0;i--) {
			if(i<10) {
				minutes.add("0"+i);
				continue;
			}
			minutes.add(""+i);
		}
		// 开始判断
		for(int i = maxSecond;i>=0;i--) {
			if(i<10) {
				seconds.add("0"+i);
				continue;
			}
			seconds.add(""+i);
		}
		Map<String,List<String>> result = new HashMap();
		result.put("hours", hours);
		result.put("minutes", minutes);
		result.put("seconds", seconds);
		result.put("prehours", prehours);
		result.put("preminutes", preminutes);
		result.put("preseconds", preseconds);
		return result;
	}
	public static long getEndLine(MicroserviceConfig mc,SearchRequest s) {
		initDate(s);
		String preEndTime = s.getDate()+" "+s.getEndTime();
		// 先判断是否可以直接定位
		List<String> result = SshCommand.execCommand(mc, "grep -n "+"'"+preEndTime+"'"+" "+getFullFilePath(mc,s) +" |tail -n 1 ");
		// 找到了开始的行
		if(ListTool.isNotEmpty(result)) {
			return Long.parseLong(result.get(0).split(Pattern.quote(":"))[0]);
		}
		// 没找到
		Map<String,List<String>> checkPointer = checkPointer(s.getBeginTime());
		List<String> hours =checkPointer.get("hours");
		List<String> minutes = checkPointer.get("minutes");
		List<String> seconds = checkPointer.get("seconds");
		String[] times = s.getBeginTime().split(Pattern.quote(":"));
		// 三层循环取
		for(String hour:hours) {
			String findTime = s.getDate()+" "+hour+":";
			List<String> hourResult = SshCommand.execCommand(mc, "grep -n "+"'"+findTime+"'"+" "+getFullFilePath(mc,s) +" |tail -n 1 ");
			// 找到了开始的行
			if(ListTool.isNotEmpty(hourResult)) {
				// 开始判断分
				for(String minute:minutes) {
					if(hour.equals(times[0])) {
						if(minute.compareTo(times[1])>0) {
							continue;
						}
					}
					findTime = s.getDate()+" "+hour+":"+minute+":";
					List<String> minuteResult = SshCommand.execCommand(mc, "grep -n "+"'"+findTime+"'"+" "+getFullFilePath(mc,s) +" |tail -n 1 ");
					if(ListTool.isNotEmpty(minuteResult)) {
						for(String second:seconds) {
							if(hour.equals(times[0]) && minute.equals(times[1])) {
								if(second.compareTo(times[2])>0) {
									continue;
								}
							}
							findTime = s.getDate()+" "+hour+":"+minute+":"+second;
							List<String> secondResult = SshCommand.execCommand(mc, "grep -n "+"'"+findTime+"'"+" "+getFullFilePath(mc,s) +" |tail -n 1 ");
							if(ListTool.isNotEmpty(secondResult)) {
								// 找到了
								return Long.parseLong(secondResult.get(0).split(Pattern.quote(":"))[0]);
							}
						}
					}
				}
			}
			
		}
		return -1;
	}
	
	public static String add1Ms(String time) {
		DateTime newTime =DateUtil.parse(time, "yyyy-MM-dd HH:mm:ss,SSS");
		return DateUtil.format(DateUtil.offsetMillisecond(newTime, 1),"yyyy-MM-dd HH:mm:ss,SSS");
	}
	public static void main(String[] args) {
		System.out.println(add1Ms("2021-02-01 09:59:18,565"));
	}
}
