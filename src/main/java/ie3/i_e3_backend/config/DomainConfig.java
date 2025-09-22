package ie3.i_e3_backend.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("ie3.i_e3_backend.domain")
@EnableJpaRepositories("ie3.i_e3_backend.repos")
@EnableTransactionManagement
public class DomainConfig {
}
