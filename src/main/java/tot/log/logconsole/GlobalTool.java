package tot.log.logconsole;

import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.Session;

public class GlobalTool {

	public static final String FILE_PATH="/home/log-console/file/";
	
	public static Map<String,Session> sshs = new HashMap<String,Session>();
}
 