package com.samuksa.user.config.jwt;

import com.samuksa.user.dto.user.db.UserJwtToken;
import com.samuksa.user.entity.errorHandler.jwt.CustomJwtException;
import com.samuksa.user.entity.errorHandler.jwt.JwtErrorCode;
import com.samuksa.user.mapper.UserMapper;
import com.samuksa.user.service.user.UserService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider {
    @Value("spring.jwt.secret")
    private  String SECRET_KEY;

    private final long  tokenValidMilisecond = 1000L * 60 * 30;

    private final long  refreshTokenTime = 1000L * 60 * 60 * 24 * 14;

    private final UserService userService;
    private final UserMapper userMapper;
    @PostConstruct
    protected void init(){
        SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes());
    }

    public String createToken(String userPk, Collection<? extends GrantedAuthority> roles){
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidMilisecond))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact()
                ;
    }

    public String createRefreshToken(String userPk, Collection<? extends GrantedAuthority> roles){
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenTime))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact()
                ;
    }
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userService.loadUserByUsername(this.getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest req, String name){
        return req.getHeader(name);
    }

    public boolean validateToken(String jwtToken, HttpServletRequest request) {
        try {
            UserJwtToken userJwtToken = userMapper.getJwtToken(jwtToken);
            if (userJwtToken == null || !userJwtToken.getAccessToken().equals(jwtToken))
                throw new JwtException("");
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        }
        catch(ExpiredJwtException e){
            request.setAttribute("exception", JwtErrorCode.TOKEN_TIME_OUT.getMessage());
        }
        catch (JwtException e){
            request.setAttribute("exception", JwtErrorCode.INVALID_TOKEN.getMessage());
        }
        catch (Exception e) {
            request.setAttribute("exception", JwtErrorCode.UNKNOWN_ERROR.getMessage());
            return false;
        }
        return false;
    }
}
