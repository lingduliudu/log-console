package tot.log.logconsole.tool;

import java.util.ArrayList;
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
	public static List<String> allTail(List<String> list,String tail) {
		List<String> result = new ArrayList<String>();
		if(isNotEmpty(list)) {
			for(String i:list) {
				result.add(i+tail);
			}
		}
		return result;
	}
}
