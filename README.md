# encjdbc-spring-boot-starter
集成阿里云数据库代理加密 本项目依赖的encjdbc版本为 1.0.9-3
```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaobinger</groupId>
        <artifactId>encjdbc-spring-boot-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```
配置文件
```yaml
spring:
  datasource:
    #数据库加密配置
    encdb:
    #是否启用加密代理,测试环境非阿里云数据库，不启用，生产环境需要启用的话，显示设置成true
    enabled: false
    #是否重置配置,如果后期有变更MEK需要设置此值为true
    reset: false
    #加密秘钥,可自定义32位密钥,不设置则自动生成，项目启动后可自行查看配置文件
    mek:
    #加密算法，默认 SM4_128_CBC
    encAlgo: SM4_128_CBC
    #加密配置文件路径 默认路径 /etc/encdb/config/encjdbc.conf
    configPath:
```
