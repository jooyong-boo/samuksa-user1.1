package com.samuksa.user.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan(value = "com.samuksa.user.config.security.mapper", sqlSessionTemplateRef = "UserSessionTemplate")
public class UserMybatisConfig {

    @Value("${spring.samuksa-user-db-datasource.usermapperlocations}")
    String mPath;

    @Bean(name = "UserSqlSessionFactory")
    public SqlSessionFactory SqlSessionFactory(@Qualifier("SamuksaUserDbDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResource(mPath));
        sqlSessionFactoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("classpath:config/mybatis-config.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "UserSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("UserSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
