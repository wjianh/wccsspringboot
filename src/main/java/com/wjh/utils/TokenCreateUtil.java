package com.wjh.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class TokenCreateUtil {
    public String getToken(String username,String password) {
        Date start = new Date();
        long currentTime = System.currentTimeMillis() + 30* 60 * 1000;//一小时有效时间
        Date end = new Date(currentTime);
        String token = "";

        token = JWT.create().withAudience(username).withIssuedAt(start).withExpiresAt(end)
                .sign(Algorithm.HMAC256(password));
        return token;
    }
}
