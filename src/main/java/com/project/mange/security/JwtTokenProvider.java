package com.project.mange.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // Tao Key chuan cho HMAC tu chuoi bi mat
    private Key getSignInKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // 1. Tao token tu thong tin User
    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userDetails.getUser().getId()))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignInKey())
                .compact();
    }

    // 2. Lay User ID tu Token (Giai ma)
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    // Xac thuc token
    public boolean validateToken(String token) {
        try{
            Jwts.parser().setSigningKey(getSignInKey()).build().parseClaimsJws(token);
            return true;
        }catch(MalformedJwtException ex){
            System.err.println("Invalid JWT token");
        }catch (ExpiredJwtException ex){
            System.err.println("Expired JWT token");
        }catch (UnsupportedJwtException ex){
            System.err.println("Unsupported JWT token");
        }catch (IllegalArgumentException ex){
            System.err.println("JWT claims string is empty");
        }catch (SignatureException ex){
            System.err.println("Invalid JWT signature");
        }
        return false;
    }
}
