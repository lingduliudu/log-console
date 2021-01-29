package tot.log.logconsole.imapper;

import org.springframework.data.jpa.repository.JpaRepository;

import tot.log.logconsole.entity.MicroserviceConfig;

public interface IMicroserviceConfigMapper extends JpaRepository<MicroserviceConfig,String>  {

}
