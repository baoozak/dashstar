package io.github.baoozak.dashstar.handler;

import io.github.baoozak.dashstar.model.Article;
import io.github.baoozak.dashstar.model.Comment;
import io.github.baoozak.dashstar.model.User;
import io.github.baoozak.dashstar.repository.ArticleRepository;
import io.github.baoozak.dashstar.repository.CommentRepository;
import io.github.baoozak.dashstar.repository.UserRepository;
import io.github.baoozak.dashstar.security.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.HashMap;
import java.util.Map;

@Path("/comments")
public class CommentHandler {

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ArticleRepository articleRepository;

    @POST
    @Path("/")
    @Secured({"user", "admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComment(Comment comment, @Context SecurityContext securityContext) {
        // 根据当前安全上下文中用户主体的名称查找用户
        User user = userRepository.findByID(Integer.valueOf(securityContext.getUserPrincipal().getName()));
        comment.setUser(user);
        // 根据评论中指定的文章ID查找文章
        Article article = articleRepository.findByID(comment.getArticleId());
        comment.setArticle(article);
        // 设置评论的创建时间戳
        comment.setCreatedAt(System.currentTimeMillis() / 1000);
        // 在数据库中创建评论
        commentRepository.create(comment);
        // 准备响应数据
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        // 返回表示操作成功的响应
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @DELETE
    @Path("/{id}")
    @Secured({"admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComment(@PathParam("id") Integer id) {
        try {
            // 调用评论仓库的删除方法来删除指定ID的评论
            commentRepository.delete(id);
            // 创建一个映射来存储响应数据
            Map<String, Object> res = new HashMap<>();
            // 放入成功状态码
            res.put("code", Response.Status.OK);
            // 返回成功响应
            return Response.status(Response.Status.OK).entity(res).build();
        } catch (Exception e) {
            // 创建一个映射来存储响应数据
            Map<String, Object> res = new HashMap<>();
            // 放入失败状态码
            res.put("code", Response.Status.INTERNAL_SERVER_ERROR);
            // 放入错误信息
            res.put("message", "Failed to delete comment");
            // 返回失败响应
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(res).build();
        }
    }


}
