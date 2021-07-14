package tot.log.logconsole.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tot.log.logconsole.GlobalTool;
import tot.log.logconsole.entity.MicroserviceConfig;
import tot.log.logconsole.http.Result;
import tot.log.logconsole.imapper.IMicroserviceConfigMapper;
import tot.log.logconsole.query.SearchRequest;
import tot.log.logconsole.ssh.SshCommand;
import tot.log.logconsole.tool.HelpTool;
import tot.log.logconsole.tool.ZipTool;

@RestController
public class DownloadController {
	@Autowired
	private IMicroserviceConfigMapper mapper;
	@Autowired
	private HttpServletResponse response;
	
	@GetMapping("downloadlog")
	public void download(SearchRequest search){
		HelpTool.initDate(search);
		MicroserviceConfig entity = new MicroserviceConfig();
		entity.setMicroserviceCode(search.getMicroserviceCode());
		entity.setEnv(search.getEnv());
		entity.setState(1);
		Example<MicroserviceConfig> searchEntity = Example.of(entity);
		List<MicroserviceConfig> list = mapper.findAll(searchEntity);
		// 下载所有的临时文件
		List<String> fileList = new ArrayList<>();
		for(MicroserviceConfig microserviceConfig:list) {
			String targetFilePath = GlobalTool.FILE_PATH+microserviceConfig.getSshIp()+ search.getMicroserviceCode()+"-"+search.getDate()+".log";
			File file = new File(targetFilePath);
			if(file.exists()) {
				file.delete();
			}
			fileList.add(targetFilePath);
			new SshCommand().downloadFile(microserviceConfig, HelpTool.getFullFilePath(microserviceConfig,search), targetFilePath);
		}
		 List<File> files = new ArrayList<File>();
		 for(String filePath:fileList) {
			 File file = new File(filePath);
			 files.add(file);
		 }
        //打包凭证.zip与EXCEL一起打包
        try {
            String zipFilenameA =  GlobalTool.FILE_PATH + "/log.zip" ;
            File fileA = new File(zipFilenameA);
            if (!fileA.exists()){   
                fileA.createNewFile();   
            }
            //创建文件输出流
            FileOutputStream fousa = new FileOutputStream(fileA);   
            ZipOutputStream zipOutA = new ZipOutputStream(fousa);
            ZipTool.zipFile(files, zipOutA);
            zipOutA.close();
            fousa.close();
            ZipTool.downloadZip(fileA,response);
        }catch (Exception e) {
            e.printStackTrace();
        }
		
	}
}
