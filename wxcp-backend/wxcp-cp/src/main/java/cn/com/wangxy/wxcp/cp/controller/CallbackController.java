package cn.com.wangxy.wxcp.cp.controller;

import cn.com.wangxy.wxcp.cp.service.WxCpServiceManager;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.config.WxCpConfigStorage;
import me.chanjar.weixin.cp.message.WxCpMessageRouter;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cp/callback")
public class CallbackController {

    private final WxCpServiceManager wxCpServiceManager;
    private final WxCpMessageRouter wxCpMessageRouter;

    public CallbackController(WxCpServiceManager wxCpServiceManager,
                             WxCpMessageRouter wxCpMessageRouter) {
        this.wxCpServiceManager = wxCpServiceManager;
        this.wxCpMessageRouter = wxCpMessageRouter;
    }

    @GetMapping("/{agentId}")
    public String verify(@PathVariable Integer agentId,
                         @RequestParam("msg_signature") String msgSignature,
                         @RequestParam("timestamp") String timestamp,
                         @RequestParam("nonce") String nonce,
                         @RequestParam("echostr") String echostr) {
        log.info("收到回调验证请求: agentId={}", agentId);
        WxCpService wxCpService = wxCpServiceManager.getService(agentId);
        if (wxCpService == null) {
            log.error("未找到对应的WxCpService: agentId={}", agentId);
            return "error: service not found";
        }
        try {
            WxCpConfigStorage storage = wxCpService.getWxCpConfigStorage();
            me.chanjar.weixin.common.util.crypto.WxCryptUtil cryptUtil =
                    new me.chanjar.weixin.common.util.crypto.WxCryptUtil(
                            storage.getToken(), storage.getAesKey(), storage.getCorpId());
            String decrypted = cryptUtil.decrypt(echostr, msgSignature, timestamp, nonce);
            log.info("回调验证成功: agentId={}", agentId);
            return decrypted;
        } catch (Exception e) {
            log.error("回调验证失败: agentId={}", agentId, e);
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/{agentId}")
    public String callback(@PathVariable Integer agentId,
                           @RequestParam("msg_signature") String msgSignature,
                           @RequestParam("timestamp") String timestamp,
                           @RequestParam("nonce") String nonce,
                           @RequestBody String requestBody) {
        log.info("收到消息回调: agentId={}", agentId);
        WxCpService wxCpService = wxCpServiceManager.getService(agentId);
        if (wxCpService == null) {
            log.error("未找到对应的WxCpService: agentId={}", agentId);
            return "error: service not found";
        }
        try {
            WxCpXmlMessage message = WxCpXmlMessage.fromEncryptedXml(
                    requestBody,
                    wxCpService.getWxCpConfigStorage(),
                    timestamp,
                    nonce,
                    msgSignature
            );
            log.info("解密消息成功: msgType={}, fromUser={}", message.getMsgType(), message.getFromUserName());

            WxCpXmlOutMessage outMessage = wxCpMessageRouter.route(message);
            if (outMessage != null) {
                return "<xml>reply_placeholder</xml>"; // TODO: proper XML serialization
            }
            return "success";
        } catch (Exception e) {
            log.error("处理消息回调失败: agentId={}", agentId, e);
            return "error";
        }
    }
}
