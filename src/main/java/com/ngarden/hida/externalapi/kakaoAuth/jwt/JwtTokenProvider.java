package com.ngarden.hida.externalapi.kakaoAuth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final Long accessTokenValidTime = Duration.ofSeconds(30).toMillis();
    private static final Long refreshTokenValidTime = Duration.ofDays(7).toMillis();

    @Value("${JWT.SECRET-KEY}")
    private String secretKey;


    public Long getUserId(String token){
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    public boolean isAccessToken(String token) throws MalformedJwtException {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getHeader().get("type").toString().equals("access");
    }


    public String createAccessToken(Long outhId){
        return createJwtToken(outhId, secretKey, "access", accessTokenValidTime);
    }

    public String createRefreshToken(Long outhId){
        return createJwtToken(outhId, secretKey, "refresh", refreshTokenValidTime);
    }

    private String createJwtToken(Long outhId, String secretKey, String type, Long tokenValidTime){
        Claims claims = Jwts.claims();
        claims.put("userId", outhId);

        return Jwts.builder()
                .setHeaderParam("type", type)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
