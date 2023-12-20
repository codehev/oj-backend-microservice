package com.wei.ojbackendgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author wei
 * 父工程引入了mybatisplus会启动数据库，这里排除数据库
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//也需要在nacos上注册
@EnableDiscoveryClient
public class OjBackendGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjBackendGatewayApplication.class, args);
    }

}
