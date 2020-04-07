package com.wjh;

import com.github.ltsopensource.spring.boot.annotation.EnableJobClient;
import com.github.ltsopensource.spring.boot.annotation.EnableJobTracker;
import com.github.ltsopensource.spring.boot.annotation.EnableMonitor;
import com.github.ltsopensource.spring.boot.annotation.EnableTaskTracker;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ComponentScan({"com.wjh", "com.wjh.ltstasktracker"})
//@ServletComponentScan
@SpringBootApplication(scanBasePackages = "com")
@MapperScan(basePackages = {"com.wjh.mapper"})
@EnableTransactionManagement
@EnableTaskTracker
@EnableMonitor
@EnableJobClient
@EnableJobTracker
public class WjhApplication {
    public static void main(String[] args) {
        SpringApplication.run(WjhApplication.class, args);
    }
}
