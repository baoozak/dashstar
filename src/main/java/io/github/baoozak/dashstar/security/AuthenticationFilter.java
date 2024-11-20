package io.github.baoozak.dashstar.security;

import io.github.baoozak.dashstar.model.User;
import io.github.baoozak.dashstar.repository.UserRepository;
import io.github.baoozak.dashstar.util.JwtUtil;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private UserRepository userRepository;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        // 获取资源方法，以便后续提取该方法所需的角色权限
        Method resourceMethod = resourceInfo.getResourceMethod();
        // 提取资源方法上注解指定的角色权限列表
        List<String> methodRoles = extractRoles(resourceMethod);
        // 获取请求中的授权头信息
        String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        // 检查授权头信息是否存在且以"Bearer "开头
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // 如果授权头信息不符合要求，抛出未授权异常
            throw new NotAuthorizedException("invalid_authorization_header");
        }
        // 从授权头信息中提取令牌部分
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        Integer id = null;
        // 尝试验证令牌并获取用户ID
        try {
            id = Integer.valueOf(JwtUtil.validateToken(token));
        } catch (Exception e) {
            // 如果令牌验证失败，中断请求并返回未授权状态
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }
        // 将获取到的用户ID用于后续的安全上下文中
        final String finalID = id.toString();
        // 设置自定义的安全上下文，包含用户信息和权限检查方法
        containerRequestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> finalID;
            }
            @Override
            public boolean isUserInRole(String s) {
                return false;
            }
            @Override
            public boolean isSecure() {
                return false;
            }
            @Override
            public String getAuthenticationScheme() {
                return "";
            }
        });
        // 根据用户ID从用户库中查找用户信息
        User user = userRepository.findByID(id);
        // 遍历方法所需的角色权限列表，检查用户是否具有其中之一
        for (String role : methodRoles) {
            if (user.getRole().equals(role)) {
                // 如果用户具有所需权限，继续执行请求
                return;
            }
        }
        // 如果用户没有所需权限，中断请求并返回禁止访问状态
        containerRequestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
    }

    private List<String> extractRoles(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<>();
            } else {
                String[] allowedRoles = secured.value();
                return Arrays.asList(allowedRoles);
            }
        }
    }
}
