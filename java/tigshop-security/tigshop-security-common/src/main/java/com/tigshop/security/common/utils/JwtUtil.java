// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.common.utils;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tigshop.common.exception.GlobalException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;

/**
 * jwt工具类
 * @author Tigshop团队
 * @create 2024年11月21日 14:30
 */
@Component
@Slf4j
public class JwtUtil {



    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expire:43200}")  // 默认30天(分钟): 60 * 24 * 30 = 43200
    private long expire;

    public static final String HEADER = "Authorization";

    /**
     * 初始化时校验JWT配置
     * 如果jwt.secret未配置或配置为空，将抛出异常阻止应用启动
     */
    @PostConstruct
    public void validateConfig() {
        if (StrUtil.isBlank(secret)) {
            throw new IllegalStateException(
                    "JWT配置错误：jwt.secret未配置或为空！请在application.yaml中配置jwt.secret，" +
                            "或通过环境变量JWT_SECRET设置。示例：jwt.secret: ${JWT_SECRET:your-secure-secret-key}"
            );
        }

        log.info("JWT配置校验通过，token过期时间：{}分钟", expire);
    }

    /**
     * 生成jwt token
     * @param userId 用户id，可能是前台也有可能是后台
     * @param isAdmin true是后台，false是前台
     * @param username 用户名
     * @return String
     */
    @SneakyThrows
    public String generateToken(Integer userId, boolean isAdmin, String username) {
        // 使用 SHA-512 生成哈希值
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] keyBytes = digest.digest(secret.getBytes());
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);
        //过期时间
        LocalDateTime tokenExpirationTime = LocalDateTime.now().plusMinutes(expire);
        return Jwts.builder()
                .signWith(signingKey, Jwts.SIG.HS512)
                .header().add("typ", "JWT").and()
                .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .issuer("tigshop")
                .subject(username)
                .expiration(Timestamp.valueOf(tokenExpirationTime))
                .claims(
                        Map.of(
                                "username", username,
                                "userId", userId,
                                "isAdmin", isAdmin
                        )
                )
                .compact();
    }

    public Claims getClaimsByToken(String token) {
        try {
            // 使用 SHA-512 生成哈希值
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] keyBytes = digest.digest(secret.getBytes());
            SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e){
            throw new GlobalException(PARAM_ERROR);
        }

    }

    /**
     * 检查token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * 获得token中的自定义信息,一般是获取token的username，无需secret解密也能获得
     * @param token jwt token
     * @param filed 字段
     * @return String
     */
    public String getClaimFiled(String token, String filed){
        try{
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(filed).asString();
        } catch (JWTDecodeException e){
            log.error("JwtUtil getClaimFiled error: ", e);
            return null;
        }
    }

    /**
     * 获取 token 中的 isAdmin 标识
     * true 表示后台 token，false 表示前台 token；
     * 字段缺失或类型异常返回 null（由调用方按上下文拒绝）
     * @param claims 已解析的 Claims
     * @return Boolean
     */
    public Boolean getIsAdmin(Claims claims) {
        try {
            return claims.get("isAdmin", Boolean.class);
        } catch (Exception e) {
            return null;
        }
    }
}