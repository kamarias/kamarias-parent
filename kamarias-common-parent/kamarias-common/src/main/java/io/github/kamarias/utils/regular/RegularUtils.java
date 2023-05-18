package io.github.kamarias.utils.regular;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 * @author wangyuxing@gogpay.cn
 * @date 2023/1/30 11:47
 */
public class RegularUtils {


    /**
     * 验证身份证号
     * @param idCard 输入身份证号
     */
    public static boolean verifyIdCard(String idCard) {
        String regEx = "^(\\d{6})(\\d{4})(\\d{2})(\\d{2})(\\d{3})([0-9]|X)$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(idCard);
        return matcher.matches();
    }

    /**
     * 验证手机号
     * @param phoneNumber 手机号
     */
    public static boolean verifyPhoneNumber(String phoneNumber) {
        String regEx = "^1[3456789]\\d{9}$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }

    /**
     * 验证邮箱
     * @param mail 邮箱
     */
    public static boolean verifyMail(String mail) {
        String regEx = "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(mail);
        return matcher.matches();
    }

    /**
     * 验证Ivp4
     * @param ipv4 ip地址
     */
    public static boolean verifyIpv4(String ipv4) {
        String regEx = "\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(ipv4);
        return matcher.matches();
    }

}
