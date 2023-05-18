package io.github.kamarias.utils.encrypt;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加解密工具
 *
 * @author wangyuxing@gogpay.cn
 * @date 2023/5/5 14:35
 */
public class AesUtils {

    private AesUtils() {
        //Do nothing
    }

    /**
     * 将数据对称加密后用Base64编码
     *
     * @param content 内容
     * @param key     密钥 （需要满足16 字节，或 23 字节，或 32 字节）
     * @return 加密后的Base64内容
     * @throws Exception 加密异常
     */
    public static String encrypt(String content, String key) throws Exception {
        SecretKeySpec secretKeySpec = generateKey(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding/");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = cipher.doFinal(byteContent);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 将Base64编码的加密数据转码后的解密
     *
     * @param content 内容
     * @param key     密钥 （需要满足16 字节，或 23 字节，或 32 字节）
     * @return 加密后的内容
     * @throws Exception 解密异常
     */
    public static String decrypt(String content, String key) throws Exception {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        SecretKeySpec secretKey = generateKey(key);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding/");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(content);
        byte[] result = cipher.doFinal(encryptedBytes);
        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * 生成密钥规范
     *
     * @param key 加密Key
     * @return 规范的加密key
     */
    public static SecretKeySpec generateKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }

}
