package _2.ArtFusion.config;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.ZoneId;

@Configuration
@EnableR2dbcRepositories(basePackages = "_2.ArtFusion.repository.r2dbc")
public class R2dbcConfig {
    @Value("${spring.r2dbc.url}")
    private String url;

    @Value("${spring.r2dbc.name}")
    private String name;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Bean
    public ConnectionFactory myConnectionFactory() {
        MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
                .host("localhost")
                .port(3306)
                .user(username)
                .password(password)
                .database(name)
                .serverZoneId(ZoneId.of("Asia/Seoul"))
                .build();

        return MySqlConnectionFactory.from(configuration);
    }

    @Bean(name = "r2dbcTransactionManager")
    public ReactiveTransactionManager r2dbcTransactionManager(ConnectionFactory myConnectionFactory) {
        return new R2dbcTransactionManager(myConnectionFactory);
    }

    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(ConnectionFactory myConnectionFactory) {
        return new R2dbcEntityTemplate(myConnectionFactory);
    }

    @Bean
    public DatabaseClient databaseClient(ConnectionFactory myConnectionFactory) {
        return DatabaseClient.builder()
                .connectionFactory(myConnectionFactory)
                .namedParameters(true)
                .build();
    }
}