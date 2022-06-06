package com.ssw.referral;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *     String[] scanBasePackages() default {}; 包扫描方法
 */
@SpringBootApplication(scanBasePackages = {"com.ssw.referral"})
@MapperScan("com.ssw.referral.dao")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public class ReferralApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReferralApplication.class, args);
    }

}
