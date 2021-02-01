package tot.log.logconsole.http;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result<T> {

	private static final String SUCCESS_MSG="操作成功";
	private static final String SUCCESS_CODE="00-0000";
	
	private static final String ERROR_MSG="内部错误";
	private static final String ERROR_RULE="搜索条件不规范";
	private static final String ERROR_CODE="99-9999";
	
	private String msg;
	private String code;
	private Integer total;
	private T data;
	
	public Result(String code,String msg,T t) {
		this.msg = msg;
		this.code = code;
		this.data = t;
	}
	public static Result<String> success() {
		return new Result<String>(SUCCESS_CODE,SUCCESS_MSG,null);
	}
	public static Result<String> error() {
		return new Result<String>(ERROR_CODE,ERROR_MSG,null);
	}
	public static Result<String> error(String msg) {
		return new Result<String>(ERROR_CODE,msg,null);
	}
	public static Result successData(Object t) {
		return new Result(SUCCESS_CODE,SUCCESS_MSG,t);
	}
}
