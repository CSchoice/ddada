package ssafy.ddada.api.health;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EarlyYamlLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(EarlyYamlLogger.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();

        String port = env.getProperty("server.port", "8080");
        String dbUrl = env.getProperty("spring.datasource.url", "NOT FOUND");
        String dbUser = env.getProperty("spring.datasource.username", "NOT FOUND");

        String envDbUrl = System.getenv().getOrDefault("SPRING_DATASOURCE_URL", "NOT SET");
        String envDbUser = System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "NOT SET");
        String envDbPassword = System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "NOT SET");

        logger.info("🛠️ [YML 및 ENV 설정 확인 - DB 연결 전]");
        logger.info("🟢 서버 포트 (YML): {}", port);
        logger.info("🟢 DB URL (YML): {}", dbUrl);
        logger.info("🟢 DB 사용자명 (YML): {}", dbUser);
        logger.info("🌐 DB URL (ENV): {}", envDbUrl);
        logger.info("🌐 DB 사용자명 (ENV): {}", envDbUser);
        logger.info("🌐 DB 비밀번호 (ENV): {}", envDbPassword.equals("NOT SET") ? "NOT SET" : "********");
    }
}
