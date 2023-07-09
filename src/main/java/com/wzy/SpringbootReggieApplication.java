package com.wzy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@SpringBootApplication
@EnableCaching
public class SpringbootReggieApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpringbootReggieApplication.class, args);
        log.info("项目启动成功");
    }

}
