package cn.com.wangxy.wxcp.cp.listener;

import cn.com.wangxy.wxcp.cp.service.WxCpServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 企业微信启动监听器
 * 应用启动时初始化企业微信服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WxCpStartupListener implements CommandLineRunner {

    private final WxCpServiceManager wxCpServiceManager;

    @Override
    public void run(String... args) {
        log.info("应用启动，开始初始化企业微信服务...");
        wxCpServiceManager.init();
    }
}
