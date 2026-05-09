package cn.com.wangxy.wxcp.common.util;

import cn.hutool.crypto.symmetric.AES;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AesUtil {

    @Value("${wxcp.aes.key}")
    private String aesKey;

    private AES aes;

    @PostConstruct
    public void init() {
        this.aes = new AES(aesKey.getBytes());
    }

    public String encrypt(String plainText) {
        return aes.encryptBase64(plainText);
    }

    public String decrypt(String cipherText) {
        return aes.decryptStr(cipherText);
    }
}
