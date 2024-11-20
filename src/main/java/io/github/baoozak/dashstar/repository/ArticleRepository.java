package io.github.baoozak.dashstar.repository;

import io.github.baoozak.dashstar.model.Article;
import io.github.baoozak.dashstar.util.HibernateUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.List;

@ApplicationScoped
public class ArticleRepository {

    public List<Article> findAll(int page, int size) throws PersistenceException {
        // 获取EntityManager实例，用于数据库操作
        EntityManager em = HibernateUtil.getEntityManager();
        List<Article> articles = null;
        try {
            // 开始数据库事务
            em.getTransaction().begin();
            // 计算查询的起始位置
            int start = (page - 1) * size;
            // 创建并执行查询，获取指定页码和大小的文章列表
            articles = em
                    .createQuery("SELECT a FROM Article a", Article.class)
                    .setFirstResult(start)
                    .setMaxResults(size)
                    .getResultList();
            // 提交事务
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 如果出现异常，回滚事务
            em.getTransaction().rollback();
            // 抛出运行时异常，包装原始的PersistenceException
            throw new RuntimeException("articles_not_found", e);
        } finally {
            // 关闭EntityManager，释放资源
            em.close();
        }
        // 返回查询到的文章列表，如果没有找到文章，则返回null
        return articles;
    }

    public Article findByID(Integer id) throws PersistenceException {
        // 获取EntityManager实例，用于管理和执行JPA操作
        EntityManager em = HibernateUtil.getEntityManager();
        Article article = null;
        try {
            // 开始事务，确保数据库操作的完整性
            em.getTransaction().begin();
            // 创建并执行查询，使用命名参数避免SQL注入
            article = em
                    .createQuery("SELECT a FROM Article a WHERE a.id = :id", Article.class)
                    .setParameter("id", id)
                    .getSingleResult();
            // 提交事务，确保查询结果的持久化
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 回滚事务，处理查询过程中出现的异常
            em.getTransaction().rollback();
            // 抛出运行时异常，包装原始的持久化异常，提供更具体的错误信息
            throw new RuntimeException("article_not_found", e);
        } finally {
            // 关闭EntityManager，释放资源，避免内存泄漏
            em.close();
        }
        // 返回查询到的文章对象
        return article;
    }

    public void create(Article article) throws PersistenceException {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(article);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            throw new RuntimeException("", e);
        } finally {
            em.close();
        }
    }

    public void update(Article article) throws PersistenceException {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Article existingArticle = em.find(Article.class, article.getId());
            try {
                HibernateUtil.copyNonNullProperties(article, existingArticle);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            em.merge(existingArticle);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            em.getTransaction().rollback();
            throw new RuntimeException("", e);
        } finally {
            em.close();
        }
    }

    public long countAll() {
        // 获取EntityManager实例，用于执行数据库操作
        EntityManager em = HibernateUtil.getEntityManager();
        // 创建并执行一条JPQL查询语句，计算Article实体的总数
        // 使用getSingleResult()方法获取查询结果，由于是计算总数，所以结果是单个Long值
        return em.createQuery("SELECT COUNT(a) FROM Article a", Long.class).getSingleResult();
    }
}
