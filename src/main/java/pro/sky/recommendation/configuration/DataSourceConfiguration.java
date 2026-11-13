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

/**
 * Настройка двух источников данных.
 *
 * <ul>
 *     <li>{@code defaultDataSource} (H2, только чтение): транзакции клиентов, работа через {@code JdbcTemplate};</li>
 *     <li>{@code rulesDataSource} (PostgreSQL): динамические правила и статистика, работа через JPA,
 *     схема создаётся Liquibase.</li>
 * </ul>
 */
@Configuration
@EnableJpaRepositories(
        basePackageClasses = DynamicRuleRepository.class,
        entityManagerFactoryRef = "rulesEntityManagerFactory",
        transactionManagerRef = "rulesTransactionManager")
public class DataSourceConfiguration {

    /**
     * Основная БД H2 с данными о транзакциях. Помечена {@link Primary}, поэтому
     * автоматически настроенный {@code JdbcTemplate} работает именно с ней.
     *
     * @param properties свойства {@code spring.datasource.*}
     * @return пул соединений к H2
     */
    @Primary
    @Bean(name = "defaultDataSource")
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource defaultDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * Вторая БД PostgreSQL с динамическими правилами.
     * {@link LiquibaseDataSource} указывает Liquibase выполнять миграции здесь, а не в H2.
     *
     * @return пул соединений к PostgreSQL, настроенный свойствами {@code rules.datasource.*}
     */
    @Bean(name = "rulesDataSource")
    @LiquibaseDataSource
    @ConfigurationProperties("rules.datasource")
    public DataSource rulesDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /**
     * Фабрика JPA (Hibernate), работающая только со второй БД.
     *
     * @param builder    построитель фабрики от Spring Boot
     * @param dataSource источник данных PostgreSQL
     * @return фабрика менеджеров сущностей для пакета {@code model}
     */
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

    /**
     * Менеджер транзакций для второй БД. Используется в {@code @Transactional("rulesTransactionManager")}.
     *
     * @param entityManagerFactory фабрика JPA для PostgreSQL
     * @return менеджер транзакций JPA
     */
    @Bean(name = "rulesTransactionManager")
    public PlatformTransactionManager rulesTransactionManager(
            @Qualifier("rulesEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
