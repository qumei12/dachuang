package com.wangyan.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

import model.LDAModel;
import model.ModelTrainer;

/**
 * 应用上下文监听器，用于在Web应用启动时预加载LDA模型
 * 增强版包含详细调试信息，便于问题排查和性能监控
 */
public class ModelLoaderListener implements ServletContextListener {
    
    // 静态模型实例，供整个应用使用
    private static LDAModel ldaModel = null;
    
    /**
     * 获取预加载的LDA模型实例
     * @return LDAModel实例
     */
    public static LDAModel getLdaModel() {
        return ldaModel;
    }
    
    /**
     * 应用上下文初始化时调用
     * @param event ServletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("开始初始化LDA模型...");
        System.out.println("Servlet上下文路径: " + event.getServletContext().getRealPath("/"));
        long startTime = System.currentTimeMillis();
        
        try {
            // 尝试加载预训练模型
            System.out.println("尝试加载预训练模型...");
            ldaModel = ModelTrainer.loadPretrainedModel();
            
            if (ldaModel == null) {
                // 如果没有预训练模型，则记录警告信息
                System.out.println("警告: 未找到预训练模型，请运行预训练脚本或首次请求时将进行实时训练");
            } else {
                long endTime = System.currentTimeMillis();
                System.out.println("LDA模型加载成功，耗时: " + (endTime - startTime) + "ms");
                System.out.println("模型信息 - 主题数: " + ldaModel.getTopicAmount() + 
                                 ", 病种数: " + ldaModel.getCaseAmount() + 
                                 ", 耗材数: " + ldaModel.getSupplyAmount());
            }
        } catch (Exception e) {
            System.err.println("初始化LDA模型时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 应用上下文销毁时调用
     * @param event ServletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("应用上下文销毁，清理资源...");
        // 清理资源（如果需要）
        ldaModel = null;
    }
}