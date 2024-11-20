package io.github.baoozak.dashstar.repository;

import io.github.baoozak.dashstar.model.Comment;
import io.github.baoozak.dashstar.util.HibernateUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.List;

@ApplicationScoped
public class CommentRepository {

    public void delete(Integer id) throws PersistenceException {
        // 获取EntityManager实例
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            // 开始事务
            em.getTransaction().begin();
            // 根据ID查找评论对象
            Comment comment = em.find(Comment.class, id);
            if (comment != null) {
                // 如果找到评论对象，则从数据库中删除
                em.remove(comment);
            }
            // 提交事务
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 如果遇到PersistenceException，回滚事务并抛出RuntimeException
            em.getTransaction().rollback();
            throw new RuntimeException("", e);
        } finally {
            // 关闭EntityManager
            em.close();
        }
    }


    public List<Comment> findByArticleId(Integer id) throws PersistenceException {
        // 获取EntityManager实例，用于与数据库交互
        EntityManager em = HibernateUtil.getEntityManager();
        List<Comment> comments = null;
        try {
            // 开始数据库事务
            em.getTransaction().begin();
            // 执行查询，获取与指定文章ID相关的评论列表
            comments = em
                    .createQuery("SELECT c FROM Comment c WHERE c.articleId = :articleId", Comment.class)
                    .setParameter("articleId", id)
                    .getResultList();
            // 提交事务，确保查询结果的持久化
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 如果发生异常，回滚事务以保持数据一致性
            em.getTransaction().rollback();
            // 抛出运行时异常，包装原始的PersistenceException，使其更易于处理
            throw new RuntimeException("", e);
        } finally {
            // 关闭EntityManager，释放资源，避免内存泄漏
            em.close();
        }
        // 返回查询到的评论列表，如果没有找到相关评论，则返回null
        return comments;
    }

    public void create(Comment comment) throws PersistenceException {
        // 获取EntityManager实例，用于管理持久化对象
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            // 开始事务，确保数据的一致性和完整性
            em.getTransaction().begin();
            // 将评论对象持久化到数据库中
            em.persist(comment);
            // 提交事务，将更改永久保存
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 如果持久化过程中发生错误，回滚事务以撤销更改
            em.getTransaction().rollback();
            // 将原始异常包装在RuntimeException中，便于异常处理
            throw new RuntimeException("", e);
        } finally {
            // 关闭EntityManager，释放资源
            em.close();
        }
    }
}
