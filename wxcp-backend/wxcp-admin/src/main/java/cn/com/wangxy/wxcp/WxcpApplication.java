package cn.com.wangxy.wxcp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("cn.com.wangxy.wxcp.**.mapper")
@EnableScheduling
public class WxcpApplication {
    public static void main(String[] args) {
        SpringApplication.run(WxcpApplication.class, args);
    }
}
