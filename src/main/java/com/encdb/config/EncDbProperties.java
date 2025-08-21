package com.encdb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xiongbing
 * @date 2025/8/18 17:30
 * @description 加密配置类
 * @version 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource.encdb")
@Data
public class EncDbProperties {

    /**
     * 是否启用数据库加密
     */
    private boolean enabled = false;
    /**
     * 是否重置配置
     */
    private boolean reset = false;

    /**
     * 主加密密钥
     */
    private String mek;

    /**
     * 加密算法
     */
    private String encAlgo = "SM4_128_CBC";

    /**
     * 加密驱动类名
     */
    private String driverClassName = "com.aliyun.encdb.mysql.jdbc.EncDriver";

    /**
     * 加密配置文件路径
     */
    private String configPath = "/etc/encdb/config/encjdbc.conf";
}
