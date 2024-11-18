package io.github.baoozak.dashstar.handler;

import io.github.baoozak.dashstar.model.User;
import io.github.baoozak.dashstar.model.UserDTO;
import io.github.baoozak.dashstar.model.request.UserLoginRequest;
import io.github.baoozak.dashstar.repository.UserRepository;
import io.github.baoozak.dashstar.security.Secured;
import io.github.baoozak.dashstar.util.BCryptUtil;
import io.github.baoozak.dashstar.util.JwtUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/users")
public class UserHandler {


    @Inject
    private UserRepository userRepository;


    @GET
    @Path("/")
    @Secured({"user", "admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        // 查询数据库中所有用户
        List<User> users = userRepository.findAll();
        // 创建一个映射对象来存储响应内容
        Map<String, Object> res = new HashMap<>();
        // 将HTTP状态码放入响应内容中
        res.put("code", Response.Status.OK);
        // 将查询到的用户列表放入响应内容中
        res.put("data", users);
        // 构建并返回包含响应内容和HTTP状态码的响应对象
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUserById(@PathParam("id") int id) {
        return userRepository.findByID(id);
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserLoginRequest request) {
        // 根据用户名查询用户信息
        User user = userRepository.findByUsername(request.getUsername());
        // 如果用户不存在，返回错误信息
        if (user == null) {
            Map<String, Object> res = new HashMap<>();
            res.put("code", Response.Status.BAD_REQUEST);
            return Response.status(Response.Status.BAD_REQUEST).entity(res).build();
        }
        // 如果密码验证失败，返回错误信息
        if (!BCryptUtil.checkPassword(request.getPassword(), user.getPassword())) {
            Map<String, Object> res = new HashMap<>();
            res.put("code", Response.Status.BAD_REQUEST);
            res.put("msg", "wrong");
            return Response.status(Response.Status.BAD_REQUEST).entity(res).build();
        }
        // 登录成功，生成JWT令牌
        String token = JwtUtil.generateToken(user.getId());
        // 创建UserDTO对象，不包含密码
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setNickname(user.getNickname());
        userDTO.setRole(user.getRole());
        // 构建响应数据，包括状态码、令牌和用户信息
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        res.put("token", token);
        res.put("data", userDTO);
        // 返回成功信息和用户数据
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(User user) {
        // 默认为用户分配"普通用户"角色
        user.setRole("user");
        // 检查数据库中是否已有用户存在
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            // 如果数据库中没有用户，将当前注册的用户设置为管理员
            user.setRole("admin");
        }
        // 对用户密码进行加密处理
        user.setPassword(BCryptUtil.hashPassword(user.getPassword()));
        // 将用户信息保存到数据库中
        userRepository.create(user);
        // 准备响应数据
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.CREATED);
        // 返回表示创建成功的HTTP响应
        return Response.status(Response.Status.CREATED).entity(res).build();
    }
}
