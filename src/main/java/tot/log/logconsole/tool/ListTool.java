package tot.log.logconsole.tool;

import java.util.List;

public class ListTool {

	public static boolean isEmpty(List list) {
		if(list == null || list.size()==0) {
			return true;
		}
		return false;
	}
	
	public static boolean isNotEmpty(List list) {
		return !isEmpty(list);
	}
}
