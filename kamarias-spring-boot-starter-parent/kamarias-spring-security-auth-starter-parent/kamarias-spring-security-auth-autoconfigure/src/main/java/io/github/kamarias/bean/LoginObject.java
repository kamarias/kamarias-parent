package io.github.kamarias.bean;

import org.springframework.util.Assert;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author wangyuxing@gogpay.cn
 * @date 2023/2/23 9:53
 */
public class LoginObject implements Serializable {

    /**
     * 用户唯一Id（须保证每个系统用户唯一）
     */
    private String id;

    /**
     * 用户缓存token key
     */
    private String uuid = UUID.randomUUID().toString();

    /**
     * 过期时间 （jwt只支持时间戳）
     */
    private Timestamp expireTime;

    /**
     * 刷新token过期时间（jwt只支持时间戳）
     */
    private Timestamp refreshExpireTime;

    /**
     * 权限列表
     */
    private Set<String> permissions = new HashSet<>();

    /**
     * 角色列表
     */
    private Set<String> roles = new HashSet<>();

    public LoginObject() {
    }

    public final String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final Set<String> getPermissions() {
        return permissions;
    }

    public final void setPermissions(Set<String> permissions) {
        Assert.notNull(permissions, "permissions is not allow null");
        this.permissions = permissions;
    }

    public final Set<String> getRoles() {
        return roles;
    }

    public final void setRoles(Set<String> roles) {
        Assert.notNull(permissions, "roles is not allow null");
        this.roles = roles;
    }

    public final String getUuid() {
        return uuid;
    }

    public final void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Timestamp getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Timestamp expireTime) {
        this.expireTime = expireTime;
    }

    public Timestamp getRefreshExpireTime() {
        return refreshExpireTime;
    }

    public void setRefreshExpireTime(Timestamp refreshExpireTime) {
        this.refreshExpireTime = refreshExpireTime;
    }
}
