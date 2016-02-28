package spring;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author K
 */
@Configuration
@PropertySource("classpath:application.properties")
@EnableJpaRepositories(basePackages = "entities")
@EnableTransactionManagement
public class JPAConfig {

    @Autowired
    private Environment enviroment;

    @Bean
    public DataSource dataSource() throws SQLException {
        String driverClassName = enviroment.getProperty("jdbc.driver.className");
        String jdbcUrl = enviroment.getProperty("spring.datasource.url");
        String username = enviroment.getProperty("spring.datasource.username");
        String password = enviroment.getProperty("spring.datasource.password");

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() throws SQLException {

        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setGenerateDdl(true);

        HibernateJpaDialect hibernateJpaDialect = new HibernateJpaDialect();

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaDialect(hibernateJpaDialect);
        factory.setDataSource(dataSource());
        factory.setPackagesToScan("entities");
        factory.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        factory.setJpaProperties(jpaProperties());
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    private Properties jpaProperties() {
        String hibenrateDialect = enviroment.getProperty("spring.jpa.properties.hibernate.dialect");
        String hibenrateHbm2ddlAuto = enviroment.getProperty("spring.jpa.hibernate.auto");
        Boolean hibernateShowSql = true;
//        String hibenrateHbm2ddlImportFiles = enviroment.getProperty("hibernate.hbm2ddl.import_files");

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", hibenrateDialect);
        jpaProperties.put("hibernate.hbm2ddl.auto", hibenrateHbm2ddlAuto);
        jpaProperties.put("hibernate.show.sql", hibernateShowSql);
//        jpaProperties.put("hibernate.hbm2ddl.import_files", hibenrateHbm2ddlImportFiles);
        jpaProperties.put("hibernate.cache.use_second_level_cache", true);
        jpaProperties.put("hibernate.cache.use_query_cache", true);
        jpaProperties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
        return jpaProperties;
    }

    @Bean
    public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws SQLException {

        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory());
        return txManager;
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }
}
