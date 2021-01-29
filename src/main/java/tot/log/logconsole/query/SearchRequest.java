package tot.log.logconsole.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {

	private String microserviceCode;
	private String date;
	private String env;
	private String beginTime;
	private String endTime;
	private Integer isTail;
	private String fullFilePath;
	
}
