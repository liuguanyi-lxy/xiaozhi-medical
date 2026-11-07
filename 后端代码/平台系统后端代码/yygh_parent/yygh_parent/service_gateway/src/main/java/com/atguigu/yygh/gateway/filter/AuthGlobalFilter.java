package com.atguigu.yygh.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关全局鉴权过滤器
 *
 * 在网关层统一校验用户 JWT（前端放在 "token" 请求头中），解决原来任何请求都能
 * 直接穿过网关访问受保护接口的问题。下游服务仍通过 AuthContextHolder 从 token
 * 中解析用户身份，无需改动。
 *
 * 白名单内的路径（登录、验证码、微信回调、医院公开查询、数据字典、管理端等）
 * 不做登录校验，直接放行；管理端 /admin/** 目前沿用课程模板的占位登录，后续
 * 接入真实账号体系后可将该路径移出白名单。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);

    /** 用户端 JWT 的请求头名称，与下游 AuthContextHolder 的读取方式保持一致 */
    private static final String TOKEN_HEADER = "token";

    /** JWT 签名密钥，必须与 common/service_utils 的 JwtHelper 保持一致 */
    private static final SecretKey TOKEN_SIGN_KEY =
            Keys.hmacShaKeyFor("M0PKKI6pYGVWWfDZw90a0lTpGYX1d4AQ".getBytes());

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 鉴权开关，便于本地联调时临时关闭
     */
    private final boolean authEnabled;

    /**
     * 免鉴权白名单（Ant 风格路径），从配置文件读取
     */
    private final List<String> whiteList;

    public AuthGlobalFilter(
            @Value("${gateway.auth.enabled:true}") boolean authEnabled,
            @Value("${gateway.auth.white-list}") List<String> whiteList) {
        this.authEnabled = authEnabled;
        this.whiteList = whiteList;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 鉴权未开启或 CORS 预检请求直接放行
        if (!authEnabled || HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        String path = request.getURI().getPath();
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        // 校验 token：缺失、签名不对或已过期都拦截
        String token = request.getHeaders().getFirst(TOKEN_HEADER);
        Long userId = parseUserId(token);
        if (userId == null) {
            log.warn("网关鉴权拦截请求：path={}，token={}", path, StringUtils.hasText(token) ? "无效" : "缺失");
            return unauthorizedResponse(exchange);
        }

        return chain.filter(exchange);
    }

    /**
     * 解析 token 中的 userId，token 缺失、非法或已过期时返回 null
     */
    private Long parseUserId(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            Claims claims = Jwts.parser().setSigningKey(TOKEN_SIGN_KEY).parseClaimsJws(token).getBody();
            Integer userId = (Integer) claims.get("userId");
            return userId == null ? null : userId.longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isWhiteListed(String path) {
        return whiteList.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 返回统一格式的未登录响应（code=208 对应 ResultCodeEnum.LOGIN_AUTH）
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":208,\"message\":\"未登录或登录已过期\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 优先于路由转发执行
        return -100;
    }
}
