package com.wei.ojbackendjudgeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 主类（项目启动入口）
 *
 * @author wei
 * todo 如需开启 Redis，须移除 exclude 中的内容@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
 * <p>
 * 必须加@ComponentScan("com.wei")，否则无法扫描到其他模块的包，其他模块的java文件也是在com.wei包路径下
 */
@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.wei")
@EnableDiscoveryClient
//接口的位置
@EnableFeignClients(basePackages = {"com.wei.ojbackendserviceclient.service"})
public class OjBackendJudgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjBackendJudgeServiceApplication.class, args);
    }

}
