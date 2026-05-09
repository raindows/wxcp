package cn.com.wangxy.wxcp.cp.handler;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.message.WxCpMessageHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class LogHandler implements WxCpMessageHandler {

    @Override
    public WxCpXmlOutMessage handle(WxCpXmlMessage message,
                                    Map<String, Object> context,
                                    WxCpService wxCpService,
                                    WxSessionManager sessionManager) throws WxErrorException {
        log.info("收到企微消息: msgType={}, fromUser={}, content={}",
                message.getMsgType(),
                message.getFromUserName(),
                message.getContent());
        return null;
    }
}
