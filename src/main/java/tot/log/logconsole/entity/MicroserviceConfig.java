package tot.log.logconsole.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="microservice_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroserviceConfig {

	@Id	//主键id
	@GeneratedValue(strategy=GenerationType.IDENTITY)//主键生成策略
	private String id;
	
	@Column(name="microservice_code")
	private String microserviceCode;
	
	@Column(name="log_path")
	private String logPath;
	
	@Column(name="log_file_pattern")
	private String logFilePattern;
	
	@Column(name="ssh_username")
	private String sshUsername;
	
	@Column(name="ssh_password")
	private String sshPassword;
	
	
	@Column(name="ssh_port")
	private Integer sshPort;
	
	@Column(name="ssh_ip")
	private String sshIp;
	
	@Column(name="state")
	private Integer state;
	
	@Column(name="env")
	private String env;
	
	@Transient
	private String fileName;
	
	
}
