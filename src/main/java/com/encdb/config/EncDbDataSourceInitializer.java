package com.encdb.config;

import com.encdb.util.EncDbUtil;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author xiongbing
 * @date 2025/8/18 17:42
 * @description 描述：加密数据库数据源初始化器
 * @version 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.encdb", name = "enabled", havingValue = "true")
@AllArgsConstructor
@ComponentScan(basePackages = "com.encdb")
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class EncDbDataSourceInitializer implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EncDbDataSourceInitializer.class);
    private final EncDbProperties encDbProperties;
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!encDbProperties.isEnabled()) {
            return;
        }
        try {
            // 生成配置文件
            EncDbUtil.generateEncDbConfig(encDbProperties);
            if (encDbProperties.isShowLog()) {
                logger.info("Generated encdb config file at: {}", encDbProperties.getConfigPath());
            }
            // 修改现有数据源配置
            modifyDataSources();

        } catch (IOException e) {
            logger.error("Failed to generate encdb config file", e);
        } catch (Exception e) {
            logger.error("Failed to initialize encdb", e);
        }
    }


    /**
     * 修改所有DataSource Bean的配置
     */
    private void modifyDataSources() {
        String[] dataSourceNames = applicationContext.getBeanNamesForType(DataSource.class);

        for (String dataSourceName : dataSourceNames) {
            try {
                DataSource dataSource = applicationContext.getBean(dataSourceName, DataSource.class);
                modifyDataSource(dataSource);
                logger.info("Modified DataSource bean: {}", dataSourceName);
            } catch (Exception e) {
                logger.warn("Failed to modify DataSource bean: {}", dataSourceName, e);
            }
        }
    }

    /**
     * 修改单个DataSource配置
     * @param dataSource DataSource实例
     */
    private void modifyDataSource(DataSource dataSource) {
        try {
            // 使用反射修改DataSource的配置
            Class<?> clazz = dataSource.getClass();

            // 修改driverClassName（如果存在）
            modifyField(dataSource, clazz, encDbProperties.getDriverClassName());

            // 修改url（如果存在）
            modifyUrlField(dataSource, clazz);

            // 修改dataSourceProperties（如果存在）
            modifyDataSourceProperties(dataSource,clazz);

        } catch (Exception e) {
            logger.warn("Failed to modify DataSource configuration", e);
        }
    }

    /**
     * 修改字段值
     */
    private void modifyField(Object obj, Class<?> clazz, String newValue) {
        try {
            Field field = getField(clazz, "driverClassName");
            if (field != null) {
                field.setAccessible(true);
                Object oldValue = field.get(obj);
                if (encDbProperties.isShowLog()) {
                    logger.info("Changing {} from {} to {}", "driverClassName", oldValue, newValue);
                }
                field.set(obj, newValue);
            }
        } catch (Exception e) {
            logger.warn("Failed to modify field: {}", "driverClassName", e);
        }
    }


    private void modifyDataSourceProperties(Object obj, Class<?> clazz) {
        try {
            Field dataSourcePropertiesField = getField(clazz, "dataSourceProperties");
            if (dataSourcePropertiesField != null) {
                dataSourcePropertiesField.setAccessible(true);
                Properties dataSourceProperties = (Properties) dataSourcePropertiesField.get(obj);
                String configPath = encDbProperties.getConfigPath();
                if (configPath == null || configPath.isEmpty()) {
                    configPath = EncDbUtil.DEFAULT_CONFIG_PATH;
                }
                dataSourceProperties.setProperty("encJdbcConfigFile", configPath);
            }
        }catch (Exception e) {
            logger.warn("Failed to modify field: {}", "dataSourceProperties", e);
        }
    }

    /**
     * 特殊处理URL字段
     */
    private void modifyUrlField(Object obj, Class<?> clazz) {
        try {
            Field field = getField(clazz, "jdbcUrl");
            if (field != null) {
                field.setAccessible(true);
                String oldUrl = (String) field.get(obj);
                if (oldUrl != null) {
                    String newUrl = EncDbUtil.convertToEncDbUrl(oldUrl);
                    if (encDbProperties.isShowLog()) {
                        logger.info("Changing URL from {} to {}", oldUrl, newUrl);
                    }
                    field.set(obj, newUrl);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to modify URL field", e);
        }
    }

    /**
     * 递归获取字段（包括父类）
     */
    private Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
