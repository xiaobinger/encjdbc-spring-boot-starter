package com.encdb.util;

import com.encdb.config.EncDbProperties;
import org.springframework.util.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author xiongbing
 * @date 2025/8/18 17:40
 * @description 数据库列加密工具类
 */
public class EncDbUtil {
    public static final String DEFAULT_CONFIG_PATH = "/etc/encdb/config/encjdbc.conf";
    public static final String DEFAULT_ALGO = "SM4_128_CBC";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
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


    /**
     * 使用字节方式生成更安全的16进制密钥（推荐）
     * @return 32位16进制字符串
     */
    private static String generateRandomMek() {
        // 16字节 = 128位
        byte[] keyBytes = new byte[16];
        SECURE_RANDOM.nextBytes(keyBytes);
        return bytesToHex(keyBytes);
    }
    /**
     * 将字节数组转换为16进制字符串
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
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
