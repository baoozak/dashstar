package io.github.baoozak.dashstar.listener;

import io.github.baoozak.dashstar.util.HibernateUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.persistence.PersistenceException;

@WebListener
public class HibernateListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // 初始化Hibernate工具类，包括数据库连接等操作
            HibernateUtil.init();
        } catch (PersistenceException ex) {
            // 数据库连接失败时的异常处理
            System.err.println("Database connection failed. Server will shut down.");
            // 关闭服务器
            shutdownServer();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 检查EntityManagerFactory是否已初始化且当前是打开状态
        if (HibernateUtil.getEntityManagerFactory() != null && HibernateUtil.getEntityManagerFactory().isOpen()) {
            // 如果是，关闭EntityManagerFactory以释放资源
            HibernateUtil.getEntityManagerFactory().close();
        }
    }

    private void shutdownServer() {
        System.exit(1);
    }
}
