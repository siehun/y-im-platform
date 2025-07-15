package io.yue.im.platform.common.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @description JWT属性
 */
@Component
public class JwtProperties {

    @Value("${jwt.accessToken.expireIn}")
    private Integer accessTokenExpireIn;

    @Value("${jwt.accessToken.secret}")
    private String accessTokenSecret;

    @Value("${jwt.refreshToken.expireIn}")
    private Integer refreshTokenExpireIn ;

    @Value("${jwt.refreshToken.secret}")
    private String refreshTokenSecret;

    public Integer getAccessTokenExpireIn() {
        return accessTokenExpireIn;
    }

    public void setAccessTokenExpireIn(Integer accessTokenExpireIn) {
        this.accessTokenExpireIn = accessTokenExpireIn;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public Integer getRefreshTokenExpireIn() {
        return refreshTokenExpireIn;
    }

    public void setRefreshTokenExpireIn(Integer refreshTokenExpireIn) {
        this.refreshTokenExpireIn = refreshTokenExpireIn;
    }

    public String getRefreshTokenSecret() {
        return refreshTokenSecret;
    }

    public void setRefreshTokenSecret(String refreshTokenSecret) {
        this.refreshTokenSecret = refreshTokenSecret;
    }
}
