package ssafy.ddada.api.health;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

@Component
public class EarlyYamlLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();

        String port = env.getProperty("server.port", "8080");
        String dbUrl = env.getProperty("spring.datasource.url", "NOT FOUND");
        String dbUser = env.getProperty("spring.datasource.username", "NOT FOUND");

        // 환경 변수에서도 직접 확인 (null 체크 및 기본값 처리)
        String envDbUrl = System.getenv().getOrDefault("SPRING_DATASOURCE_URL", "NOT SET");
        String envDbUser = System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "NOT SET");
        String envDbPassword = System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "NOT SET");

        System.out.println("🛠️ [YML 및 환경 변수 설정 확인 - DB 연결 전]");
        System.out.println("🟢 서버 포트 (YML): " + port);
        System.out.println("🟢 DB URL (YML): " + dbUrl);
        System.out.println("🟢 DB 사용자명 (YML): " + dbUser);
        System.out.println("🌐 DB URL (ENV): " + envDbUrl);
        System.out.println("🌐 DB 사용자명 (ENV): " + envDbUser);
        System.out.println("🌐 DB 비밀번호 (ENV): " + (envDbPassword.equals("NOT SET") ? "NOT SET" : "********"));
    }
}
