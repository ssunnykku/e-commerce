package kr.hhplus.be.server;

import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.TestInstance;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestcontainersConfiguration {

	public static final MySQLContainer<?> MYSQL_CONTAINER;
	public static final GenericContainer<?> REDIS_CONTAINER;

	static {
		// MySQL 컨테이너 설정
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
				.withDatabaseName("hhplus")
				.withUsername("test")
				.withPassword("test");
		MYSQL_CONTAINER.start();

		// Redis 컨테이너 설정 (기본 포트 6379)
		REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
				.withExposedPorts(6379);
		REDIS_CONTAINER.start();

		// MySQL Spring 설정
		System.setProperty("spring.datasource.url",
				MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());
		System.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");

		// Redis Spring 설정
		System.setProperty("spring.redis.host", REDIS_CONTAINER.getHost());
		System.setProperty("spring.redis.port", REDIS_CONTAINER.getMappedPort(6379).toString());
	}

	@PreDestroy
	public void preDestroy() {
		if (MYSQL_CONTAINER.isRunning()) {
			MYSQL_CONTAINER.stop();
		}
		if (REDIS_CONTAINER.isRunning()) {
			REDIS_CONTAINER.stop();
		}
	}
}