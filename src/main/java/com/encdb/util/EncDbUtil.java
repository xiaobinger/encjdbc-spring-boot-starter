package com.encdb.util;

import com.encdb.config.EncDbProperties;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

/**
 * @author xiongbing
 * @date 2025/8/18 17:40
 * @description 数据库列加密工具类
 */
public class EncDbUtil {
    public static final int RANDOM_MEK_LENGTH = 32;
    public static final String RANDOM_MEK_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String DEFAULT_CONFIG_PATH = "/etc/encdb/config/encjdbc.conf";
    public static final String DEFAULT_ALGO = "SM4_128_CBC";
    /**
     * 生成encdb.conf配置文件
     * @param properties 配置属性
     */
    public static void generateEncDbConfig(EncDbProperties properties) throws IOException {
        if (!properties.isEnabled()) {
            return;
        }
        // 确保目录存在
        String configPath = properties.getConfigPath();
        if (configPath == null || configPath.isEmpty()) {
            configPath = DEFAULT_CONFIG_PATH;
        }
        File configFile = new File(configPath);
        File parentDir = configFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (configFile.exists() && !properties.isReset()) {
            return;
        }
        // 生成配置内容
        StringBuilder configContent = new StringBuilder();
        String mek = properties.getMek();
        if (mek == null || mek.isEmpty()) {
            mek = generateRandomMek();
        }
        configContent.append("MEK=").append(mek).append("\n");
        String encAlgo = properties.getEncAlgo();
        if (encAlgo == null || encAlgo.isEmpty()) {
            encAlgo = DEFAULT_ALGO;
        }
        configContent.append("ENC_ALGO=").append(encAlgo).append("\n");

        // 写入配置文件
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(properties.getConfigPath()))) {
            writer.write(configContent.toString());
        }
    }

    private static String generateRandomMek() {
        Random random = new Random();
        StringBuilder mekBuilder = new StringBuilder();
        for (int i = 0; i < RANDOM_MEK_LENGTH; i++) {
            int randomIndex = random.nextInt(RANDOM_MEK_LENGTH);
            mekBuilder.append(RANDOM_MEK_CHARACTERS.charAt(randomIndex));
        }
        return mekBuilder.toString();
    }
    /**
     * 转换MySQL JDBC URL为加密URL
     * @param originalUrl 原始JDBC URL
     * @return 加密JDBC URL
     */
    public static String convertToEncDbUrl(String originalUrl) {
        if (!StringUtils.hasText(originalUrl)) {
            return originalUrl;
        }

        // 如果已经是加密URL，则直接返回
        if (originalUrl.startsWith("jdbc:mysql:encdb://")) {
            return originalUrl;
        }

        // 转换MySQL URL为加密URL
        if (originalUrl.startsWith("jdbc:mysql://")) {
            return "jdbc:mysql:encdb://" + originalUrl.substring(13);
        }

        return originalUrl;
    }
}
