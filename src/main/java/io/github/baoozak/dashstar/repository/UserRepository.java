package io.github.baoozak.dashstar.repository;

import io.github.baoozak.dashstar.model.User;
import io.github.baoozak.dashstar.util.HibernateUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;


import java.util.List;

@ApplicationScoped
public class UserRepository {

    public List<User> findAll() throws PersistenceException {
        // 获取用于数据库操作的EntityManager
        EntityManager em = HibernateUtil.getEntityManager();
        List<User> users = null;
        try {
            // 开始事务
            em.getTransaction().begin();
            // 执行查询以获取所有User实体
            users = em
                    .createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
            // 提交事务
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 如果出现异常，回滚事务
            em.getTransaction().rollback();
            // 将异常重新抛出为RuntimeException
            throw new RuntimeException("", e);
        } finally {
            // 关闭EntityManager以释放资源
            em.close();
        }
        // 返回查询结果
        return users;
    }


    public User findByID(Integer id) throws PersistenceException {
        // 获取EntityManager实例，用于与数据库交互
        EntityManager em = HibernateUtil.getEntityManager();
        User user = null;
        try {
            // 开始事务，确保数据库操作的完整性
            em.getTransaction().begin();
            // 创建查询，从User表中根据ID查询用户信息
            user = em
                    .createQuery("SELECT u FROM User u WHERE u.id = :id", User.class)
                    .setParameter("id", id) // 设置查询参数，替换:id为实际的用户ID
                    .getSingleResult(); // 执行查询，并获取单个结果
            // 提交事务，完成数据库操作
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 如果查询过程中发生异常，回滚事务以保持数据一致性
            em.getTransaction().rollback();
            // 抛出运行时异常，包装原始的PersistenceException，提供更详细的错误信息
            throw new RuntimeException("user_not_found", e);
        } finally {
            // 关闭EntityManager，释放资源，避免内存泄漏
            em.close();
        }
        // 返回查询到的用户对象，如果没有找到则返回null
        return user;
    }

    public User findByUsername(String username) throws PersistenceException {
        // 获取EntityManager用于数据库操作
        EntityManager em = HibernateUtil.getEntityManager();
        User user = null;
        try {
            // 开始事务
            em.getTransaction().begin();
            // 创建查询，使用参数化查询防止SQL注入
            user = em
                    .createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            // 提交事务
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 如果发生异常，回滚事务
            em.getTransaction().rollback();
            // 抛出运行时异常，包装原始异常
            throw new RuntimeException("user_not_found", e);
        } finally {
            // 关闭EntityManager释放资源
            em.close();
        }
        // 返回查询到的用户对象
        return user;
    }

    public void create(User user) throws PersistenceException {
        // 获取EntityManager实例，用于管理和执行持久化操作
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            // 开始事务，确保数据的一致性和完整性
            em.getTransaction().begin();
            // 持久化用户实体
            em.persist(user);
            // 提交事务，将更改持久化到数据库
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            // 捕获持久化异常，进行事务回滚以保持数据一致性
            em.getTransaction().rollback();
            // 重新抛出异常，并包装为RuntimeException，便于上层处理
            throw new RuntimeException("user_already_exists", e);
        } finally {
            // 关闭EntityManager，释放资源，避免内存泄漏
            em.close();
        }
    }
}
