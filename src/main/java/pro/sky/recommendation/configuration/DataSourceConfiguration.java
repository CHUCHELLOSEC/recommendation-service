package pro.sky.recommendation.configuration;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import pro.sky.recommendation.model.DynamicRule;
import pro.sky.recommendation.repository.DynamicRuleRepository;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = DynamicRuleRepository.class,
        entityManagerFactoryRef = "rulesEntityManagerFactory",
        transactionManagerRef = "rulesTransactionManager")
public class DataSourceConfiguration {

    // Основная БД: H2 с данными о транзакциях (только чтение)
    @Primary
    @Bean(name = "defaultDataSource")
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource defaultDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    // Вторая БД: PostgreSQL с динамическими правилами.
    // @LiquibaseDataSource говорит Liquibase выполнять миграции именно здесь, а не в H2.
    @Bean(name = "rulesDataSource")
    @LiquibaseDataSource
    @ConfigurationProperties("rules.datasource")
    public DataSource rulesDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    // JPA (Hibernate) работает только со второй БД
    @Bean(name = "rulesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean rulesEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("rulesDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(DynamicRule.class)
                .persistenceUnit("rules")
                .build();
    }

    @Bean(name = "rulesTransactionManager")
    public PlatformTransactionManager rulesTransactionManager(
            @Qualifier("rulesEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}