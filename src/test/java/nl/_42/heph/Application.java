/*
 * (C) 2014 42 bv (www.42.nl). All rights reserved.
 */
package nl._42.heph;

import javax.sql.DataSource;

import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;
import nl._42.database.truncator.DatabaseTruncator;
import nl._42.database.truncator.Platform;
import nl._42.database.truncator.config.DatabaseTruncatorProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public DatabaseTruncator databaseTruncator(DataSource dataSource) {
        return Platform.H2.createTruncator(dataSource, new DatabaseTruncatorProperties());
    }

    @Bean
    public BeanMapper beanMapper() {
        BeanMapperBuilder builder = new BeanMapperBuilder()
                .addPackagePrefix(Application.class.getPackage().getName());
        return builder.build();
    }

}
