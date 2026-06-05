package ai.leadplus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.main.allow-bean-definition-overriding=true",
		"spring.datasource.url=jdbc:h2:mem:leadplus-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driverClassName=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.sql.init.mode=never",
		"spring.liquibase.enabled=false",
		"spring.flyway.enabled=false"
})
@EnabledIfEnvironmentVariable(named = "RUN_CONTEXT_TESTS", matches = "true")
class LeadPlusServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
