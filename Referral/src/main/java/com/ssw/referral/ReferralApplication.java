package com.ssw.referral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *     String[] scanBasePackages() default {}; 包扫描方法
 */
@SpringBootApplication(scanBasePackages = {"com.ssw.referral"})
public class ReferralApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReferralApplication.class, args);
    }

}
