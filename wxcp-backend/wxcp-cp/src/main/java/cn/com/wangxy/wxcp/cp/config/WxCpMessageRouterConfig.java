package cn.com.wangxy.wxcp.cp.config;

import cn.com.wangxy.wxcp.cp.handler.LogHandler;
import me.chanjar.weixin.cp.message.WxCpMessageRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxCpMessageRouterConfig {

    @Bean
    public WxCpMessageRouter wxCpMessageRouter(LogHandler logHandler) {
        WxCpMessageRouter router = new WxCpMessageRouter(null);

        // Handle all event messages
        router.rule()
                .async(false)
                .msgType("event")
                .handler(logHandler)
                .end();

        // Handle all other messages (text, image, etc.)
        router.rule()
                .async(false)
                .handler(logHandler)
                .next();

        return router;
    }
}
