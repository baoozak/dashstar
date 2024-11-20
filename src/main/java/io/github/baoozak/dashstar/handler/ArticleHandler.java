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
import java.util.List;
import java.util.Map;

@Path("/articles")
public class ArticleHandler {

    @Inject
    private ArticleRepository articleRepository;

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private UserRepository userRepository;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllArticles(@QueryParam("page") @DefaultValue("1") int page,
                                   @QueryParam("size") @DefaultValue("5") int size) {
        List<Article> articles = articleRepository.findAll(page, size);
        long totalArticles = articleRepository.countAll();  // 获取总文章数
        Map<String, Object> res = new HashMap<>();
        res.put("totalArticles", totalArticles);  // 将总文章数添加到响应数据中
        res.put("code", Response.Status.OK);
        res.put("data", articles);
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArticleById(@PathParam("id") Integer id) {
        // 从文章仓库中按ID查找文章
        Article article = articleRepository.findByID(id);
        // 创建一个Map来存储响应结果
        Map<String, Object> res = new HashMap<>();
        // 将HTTP状态码和找到的文章放入响应结果中
        res.put("code", Response.Status.OK);
        res.put("data", article);
        // 构建并返回包含文章信息和HTTP状态的Response对象
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @GET
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComments(@PathParam("id") Integer id) {
        // 根据文章ID从评论仓库中获取评论列表
        List<Comment> comments = commentRepository.findByArticleId(id);
        // 创建一个映射对象来存储响应数据
        Map<String, Object> res = new HashMap<>();
        // 将HTTP状态码和评论数据添加到响应映射中
        res.put("code", Response.Status.OK);
        res.put("data", comments);
        // 构建并返回包含响应数据的HTTP响应对象
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @POST
    @Path("/")
    @Secured({"admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createArticle(Article article, @Context SecurityContext securityContext) {
        // 根据当前用户ID获取用户信息，用于设置文章作者
        User author = userRepository.findByID(Integer.valueOf(securityContext.getUserPrincipal().getName()));
        article.setAuthor(author);
        // 设置文章创建时间为当前时间戳
        article.setCreatedAt(System.currentTimeMillis() / 1000);
        // 在数据库中创建新文章
        articleRepository.create(article);
        // 准备响应数据，表示文章创建成功
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        // 返回表示成功的HTTP响应状态码和响应体
        return Response.status(Response.Status.OK).entity(res).build();
    }

    @PUT
    @Path("/")
    @Secured({"admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateArticle(Article article , @Context SecurityContext securityContext ) {
        // 设置文章作者ID为当前用户ID，确保文章与用户关联
        article.setAuthorId(Integer.valueOf(securityContext.getUserPrincipal().getName()));
        // 执行文章信息更新操作
        articleRepository.update(article);
        // 创建响应对象，用于返回更新状态
        Map<String, Object> res = new HashMap<>();
        res.put("code", Response.Status.OK);
        // 返回成功更新的响应
        return Response.status(Response.Status.OK).entity(res).build();
    }
}
