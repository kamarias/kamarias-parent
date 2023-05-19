package io.github.kamarias.uuid;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/2/23 9:53
 */
public abstract class UuidObject implements Serializable {

    /**
     * 用户缓存token key
     */
    private String uuid = UUID.randomUUID().toString();


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
