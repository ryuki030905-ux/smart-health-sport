package com.healthsport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// HealthSportApplication：这个类先负责对应模块逻辑
public class HealthSportApplication {

    // 项目启动入口，跑这个 main 就会把 SpringBoot 全部拉起来
    public static void main(String[] args) {
        SpringApplication.run(HealthSportApplication.class, args);
    }
}

