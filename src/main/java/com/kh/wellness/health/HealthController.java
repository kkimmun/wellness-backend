package com.kh.wellness.health;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/health")
public class HealthController {
	
	@Autowired
	private DataSource dataSource;

	@Value("${spring.application.name:wellness}")
    private String appName;
	
	@GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        String dbStatus = "DOWN";

        try (Connection conn = dataSource.getConnection()) {
            dbStatus = "UP";
        } catch (Exception e) {
            dbStatus = "DOWN";
        }

        String overallStatus = "UP".equals(dbStatus) ? "UP" : "DOWN";

        result.put("status", overallStatus);
        result.put("database", dbStatus);
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        result.put("app", appName);

        return result;
    }
}
