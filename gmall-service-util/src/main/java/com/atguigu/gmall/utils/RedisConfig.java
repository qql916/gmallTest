package com.atguigu.gmall.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedisConfig负责在spring容器启动时自动注入
 */
@Configuration
public class RedisConfig {
    //读取配置文件中的redis的ip地址
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Bean
    public RedisUtil getRedisUtil(){
        if (host.equals("diabled")){
            return null;
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initPool(host,port,database);
        return redisUtil;
    }
}
