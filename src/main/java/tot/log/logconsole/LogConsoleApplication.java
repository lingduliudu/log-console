package tot.log.logconsole;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import tot.log.logconsole.websocket.WebSocketServer;


@SpringBootApplication
@EnableScheduling
public class LogConsoleApplication implements CommandLineRunner {
	@Autowired
	SpringTool springTool;
	@Autowired
	private WebSocketServer webSocketServer;

	public static void main(String[] args) {
		SpringApplication.run(LogConsoleApplication.class, args);
	}


    @Override
    public void run(String... args) throws Exception {
    	// 创建下载日志临时目录
    	File tempFile = new File(GlobalTool.FILE_PATH);
    	tempFile.mkdirs();
        webSocketServer.startWeb();
    }
}
