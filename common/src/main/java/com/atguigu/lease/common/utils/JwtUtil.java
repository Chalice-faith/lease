package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static long tokenExpiration = 60 * 60 * 1000L;
    private static SecretKey tokenSignKey = Keys.hmacShaKeyFor("M0PKKI6pYGVWWfDZw90a0lTpGYX1d4AQ".getBytes());

    public static String createToken(Long userId, String username) {
        String jwt = Jwts.builder().
                setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)).
                setSubject("LOGIN_USER").
                claim("userId", userId).
                claim("username", username).
                signWith(tokenSignKey,SignatureAlgorithm.HS256).
                compact();
        return jwt;
    }
    public static Claims parseToken(String token){

        if (token==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }

        try{
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(tokenSignKey).build();
            return jwtParser.parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }
//    public static void main(String[] args){
//        System.out.println(createToken(1L,"zhangsan"));
//    }
}
