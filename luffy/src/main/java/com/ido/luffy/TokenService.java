package com.ido.luffy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Carl
 * Date 2019/1/3
 **/
@Service
public class TokenService {

    @Value("${lufft.jwt.expire-time:15}")
    private int TOKEN_EXPIRE_TIME;
    @Value("${lufft.jwt.secret:luffy-secret}")
    private String secret;
    @Value("${lufft.jwt.key:luffy-key}")
    private String key;

    /**
     * 生成jwt token
     *
     * @param user
     * @return
     */
    public String createToken(Authentication user) {
        return createToken(user, secret, key);
    }

    /**
     * 生成jwt token
     *
     * @param user
     * @param secret jwt 秘钥
     * @param key    jwt issuer
     * @return
     */
    public String createToken(Authentication user, String secret, String key) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(Base64.getEncoder().encode(secret.getBytes()));
            String token = JWT.create()
                    .withIssuer(key)
                    .withExpiresAt(addMinutes(new Date(), TOKEN_EXPIRE_TIME))
                    .withClaim("userId", user.getUserId().toString())
                    .withClaim("role", user.getRole())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException(exception);
            //log Token Signing Failed
        }
    }

    private static Date addMinutes(Date date, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MINUTE, amount);
        return c.getTime();
    }


    public Authentication doVerify(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(Base64.getEncoder().encode(secret.getBytes()));
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            return new Authentication() {
                @Override
                public String getUserId() {
                    return jwt.getClaim("userId").asString();
                }

                @Override
                public String getRole() {
                    return jwt.getClaim("role").asString();
                }

                @Override
                public String getUserName() {
                    return null;
                }

                @Override
                public Object getPayload() {
                    return null;
                }
            };
        } catch (JWTVerificationException exception) {
            //log WRONG Encoding message
            return null;
        }
    }

    /**
     * 查看token是否有效
     *
     * @param token
     * @return
     */
    public boolean isTokenValid(String token) {
        return this.doVerify(token) != null;
    }

}
