package com.kentcarmine.multitopicforum.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@PropertySources({@PropertySource("classpath:application.properties"),@PropertySource("classpath:application-dev.properties"),@PropertySource("classpath:application-test.properties"),@PropertySource("classpath:application-prod.properties"), @PropertySource("classpath:system.properties")})
//@ConfigurationProperties("spring.datasource")
@Profile({"prod"})
public class DBConfig {
//    @Value("${spring.datasource.url}")
//    private String dbUrl;
//
//    @Bean
//    public DataSource dataSource() {
//        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl(dbUrl);
//        return new HikariDataSource(config);
//    }

//    private String driverClassName;
//    private String url;
//    private String username;
//    private String password;
//
//    @Profile("dev")
//    @Bean
//    public String devDatabaseConnection() {
//        System.out.println("DB connection for Dev - H2");
//        System.out.println(driverClassName);
//        System.out.println(url);
//        return "DB Connection for Dev - H2";
//    }
//
//    @Profile("test")
//    @Bean
//    public String testDatabaseConnection() {
//        System.out.println("DB connection for Test - H2");
//        System.out.println(driverClassName);
//        System.out.println(url);
//        return "DB Connection for Test - H2";
//    }
//
//    @Profile("prod")
//    @Bean
//    public String prodDatabaseConnection() {
//        System.out.println("DB connection for Prod - H2");
//        System.out.println(driverClassName);
//        System.out.println(url);
//        return "DB Connection for Prod - H2";
//    }

//    @Value("${spring.datasource.url}")
//    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    @Profile("prod")
    public DataSource dataSource() throws URISyntaxException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

//        BasicData basicDataSource = new BasicDataSource();
//        basicDataSource.setUrl(dbUrl);
//        basicDataSource.setUsername(username);
//        basicDataSource.setPassword(password);
//
//        return basicDataSource;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
        return new HikariDataSource(config);
    }

}
