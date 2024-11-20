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
        // 获取EntityManager实例
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            // 开始事务
            em.getTransaction().begin();
            // 持久化文章对象
            em.persist(article);
            // 提交事务
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 回滚事务以确保数据一致性
            em.getTransaction().rollback();
            // 将捕获的PersistenceException包装为RuntimeException并重新抛出
            throw new RuntimeException("", e);
        } finally {
            // 关闭EntityManager释放资源
            em.close();
        }
    }

    public void update(Article article) throws PersistenceException {
        // 获取EntityManager实例，用于管理和执行数据库操作
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            // 开始事务，确保数据库操作的完整性
            em.getTransaction().begin();
            // 根据传入文章的ID从数据库中查找对应的文章实体
            Article existingArticle = em.find(Article.class, article.getId());
            try {
                // 将传入文章对象的非空属性复制到查找到的文章实体对象中，以实现部分更新
                HibernateUtil.copyNonNullProperties(article, existingArticle);
            } catch (IllegalAccessException e) {
                // 如果在复制属性过程中发生访问异常，将其包装为运行时异常并抛出
                throw new RuntimeException(e);
            }
            // 将更新后的文章实体合并到当前的持久化上下文中，以便在数据库中更新
            em.merge(existingArticle);
            // 提交事务，将更改持久化到数据库
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 如果在执行数据库操作过程中发生异常，回滚事务以保持数据一致性
            em.getTransaction().rollback();
            // 将捕获的持久化异常包装为运行时异常，并抛出，以便调用者可以处理
            throw new RuntimeException("", e);
        } finally {
            // 关闭EntityManager，释放资源
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
