package io.yue.im.platform.message;
import io.yue.im.platform.common.threadpool.GroupMessageThreadPoolUtils;
import io.yue.im.platform.common.threadpool.PrivateMessageThreadPoolUtils;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDubbo
@EnableDiscoveryClient
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = {"io.yue.im.platform.message.domain.repository"})
@ComponentScan(basePackages = {"io.yue.im"})
@SpringBootApplication(exclude= {SecurityAutoConfiguration.class })// 禁用secrity
public class IMPlatformMessageStarter {
    public static void main(String[] args) {
        // 添加一个JVM关闭时的钩子，在应用关闭时执行。这里用于关闭两个自定义的线程池
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PrivateMessageThreadPoolUtils.shutdown();
            GroupMessageThreadPoolUtils.shutdown();
        }));
        System.setProperty("user.home", "D:/y-im/y-im-platform-message");
        SpringApplication.run(IMPlatformMessageStarter.class, args);
    }

}
