package cn.blmdz.proxy.model;

import java.util.List;

import lombok.Data;

/**
 * 
 */
@Data
public class User {

    /**
     * 用户ID
     */
    private String appId;
    /**
     * 用户授权码
     */
    private String authCode;
    /**
     * 用户代理
     */
    private List<UserProxy> proxys;
}
