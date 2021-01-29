package tot.log.logconsole.ssh;

import java.util.List;

import tot.log.logconsole.entity.MicroserviceConfig;
import tot.log.logconsole.query.SearchRequest;

public class FilePoint {
	
	// 定位锚点
	public long anchor(MicroserviceConfig mc,SearchRequest search) {
		
		// 重置开始时间
		// 重置结束时间
		return 0;
	}
	
	public String resetBeginTime(MicroserviceConfig mc,SearchRequest search) {
		
		// 先通过本时间进行一次试探操作
		String beginTime = search.getBeginTime();
		String date_time = search.getDate()+" "+beginTime;
		String init_hour = "00";
		//while(true) {
		//	List<String> begin = SshCommand.execCommand(mc, "grep -n "+"'"+beginTime+"'"+" "+search.getFullFilePath() +" |head -n  ");
		//}
		return "";
	}
	

}
