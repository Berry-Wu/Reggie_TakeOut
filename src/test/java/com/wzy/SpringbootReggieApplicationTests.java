package com.wzy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class SpringbootReggieApplicationTests {

    @Test
    void contextLoads() {
        String time = String.valueOf(LocalDateTime.now());
        System.out.println("当前时间:"+time);
    }

}
