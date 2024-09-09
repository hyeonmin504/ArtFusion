package _2.ArtFusion.config;

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Duration;
import java.time.ZoneId;

import static io.r2dbc.pool.PoolingConnectionFactoryProvider.*;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
@EnableR2dbcRepositories(basePackages = "_2.ArtFusion.repository.r2dbc")
public class R2dbcConfig  {
    @Value("${spring.r2dbc.url}")
    private String url;

    @Value("${spring.r2dbc.host}")
    private String rdbhost;

    @Value("${spring.r2dbc.name}")
    private String name;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Bean
    public ConnectionFactory myConnectionFactory() {
        MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
                .host(rdbhost)
                .port(3306)
                .user(username)
                .password(password)
                .database(name)
                .serverZoneId(ZoneId.of("Asia/Seoul"))
                .build();

        return MySqlConnectionFactory.from(configuration);
    }

//    @Bean
//    public ConnectionFactory myConnectionFactory() {
//        return ConnectionFactories.get(builder()
//                .option(DRIVER, "pool")
//                .option(PROTOCOL, "mysql")
//                .option(HOST, "localhost") // EC2 환경에서는 해당 서버의 IP나 도메인을 사용
//                .option(USER, username)
//                .option(PORT, 3306)
//                .option(PASSWORD, password)
//                .option(DATABASE, name)
//                .option(MAX_SIZE, 20)
//                .option(INITIAL_SIZE, 10)
//                .option(MAX_IDLE_TIME, Duration.ofSeconds(60))
//                .option(MAX_LIFE_TIME, Duration.ofMinutes(30))
//                .build());
//    }

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