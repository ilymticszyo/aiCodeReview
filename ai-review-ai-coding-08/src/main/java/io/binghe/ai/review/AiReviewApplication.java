package io.binghe.ai.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author binghe(微信 : hacker_binghe)
 * @version 1.0.0
 * @description 项目启动类
 * @github https://github.com/binghe001
 * @copyright 公众号: 冰河技术
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class AiReviewApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiReviewApplication.class, args);
    }
}
