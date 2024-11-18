package io.github.baoozak.dashstar.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.lang.reflect.Field;

public class HibernateUtil {
    private static EntityManagerFactory emf;


    public static void init() {
        emf = Persistence.createEntityManagerFactory("default");
    }


    public static void copyNonNullProperties(Object source, Object target) throws IllegalAccessException {
        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(source);
            if (value != null) {
                field.set(target, value);
            }
        }
    }

    /**
     * 获取EntityManagerFactory实例
     * EntityManagerFactory是用于创建EntityManager的工厂
     * 在应用程序中，通常只需要一个EntityManagerFactory实例，因为它可以重复使用
     * 此方法提供了一种访问该工厂的途径，确保在整个应用程序中可以一致地访问和使用它
     *
     * @return EntityManagerFactory实例，用于创建EntityManager
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }


    /**
     * 获取EntityManager实例方法
     * EntityManager是Java Persistence API(JPA)中的一个接口，用于管理实体的生命周期
     * 它提供了一种与实体对象交互的方式，支持增删查改操作
     * @return EntityManager 返回一个EntityManager实例，用于执行实体对象的持久化操作
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
