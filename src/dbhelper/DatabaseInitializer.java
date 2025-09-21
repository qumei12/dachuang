package dbhelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库初始化工具类
 * 用于创建数据库和表结构
 */
public class DatabaseInitializer {
    
    // 数据库连接信息（不指定具体数据库）
    private static String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static String dbUrl = "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static String dbUser = "root";
    private static String dbPassword = "root";
    
    /**
     * 创建数据库
     */
    public static void createDatabase() {
        System.out.println("开始创建数据库...");
        Connection connection = null;
        Statement statement = null;
        
        try {
            // 加载数据库驱动
            Class.forName(dbClassName);
            
            // 建立连接（不指定具体数据库）
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            statement = connection.createStatement();
            
            // 创建数据库
            String createDbSql = "CREATE DATABASE IF NOT EXISTS db_mashup " +
                                "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            statement.executeUpdate(createDbSql);
            System.out.println("数据库 db_mashup 创建成功");
            
            // 使用数据库
            statement.executeUpdate("USE db_mashup");
            
            // 创建病种表
            String createDiseaseTableSql = "CREATE TABLE IF NOT EXISTS tb_disease (" +
                                          "N_ID INT PRIMARY KEY COMMENT '病种ID'," +
                                          "C_NAME VARCHAR(255) NOT NULL COMMENT '病种名称'," +
                                          "C_DESCRIPTION TEXT COMMENT '病种描述'" +
                                          ") COMMENT='病种表'";
            statement.executeUpdate(createDiseaseTableSql);
            System.out.println("病种表 tb_disease 创建成功");
            
            // 创建病例表
            String createCaseTableSql = "CREATE TABLE IF NOT EXISTS tb_case (" +
                                       "N_ID INT PRIMARY KEY COMMENT '病例ID'," +
                                       "N_MASHUP_ID INT NOT NULL COMMENT '关联的病种ID'," +
                                       "C_CASE_ID VARCHAR(50) NOT NULL COMMENT '病例编号'" +
                                       ") COMMENT='病例表'";
            statement.executeUpdate(createCaseTableSql);
            System.out.println("病例表 tb_case 创建成功");
            
            // 创建耗材表
            String createSupplyTableSql = "CREATE TABLE IF NOT EXISTS tb_supply (" +
                                         "N_ID INT PRIMARY KEY COMMENT '耗材ID'," +
                                         "N_CASE_ID INT NOT NULL COMMENT '关联的病例ID'," +
                                         "C_NAME VARCHAR(255) NOT NULL COMMENT '耗材名称'," +
                                         "C_PRODUCT_NAME VARCHAR(255) COMMENT '产品名称'," +
                                         "C_SPECIFICATION VARCHAR(100) COMMENT '规格'," +
                                         "C_PRICE DECIMAL(10,2) COMMENT '单价'," +
                                         "C_QUANTITY INT COMMENT '数量'" +
                                         ") COMMENT='耗材表'";
            statement.executeUpdate(createSupplyTableSql);
            System.out.println("耗材表 tb_supply 创建成功");
            
            // 创建索引
            try {
                statement.executeUpdate("CREATE INDEX idx_disease_name ON tb_disease(C_NAME)");
                System.out.println("病种名称索引创建成功");
            } catch (SQLException e) {
                System.out.println("病种名称索引已存在");
            }
            
            try {
                statement.executeUpdate("CREATE INDEX idx_case_mashup ON tb_case(N_MASHUP_ID)");
                System.out.println("病例关联病种索引创建成功");
            } catch (SQLException e) {
                System.out.println("病例关联病种索引已存在");
            }
            
            try {
                statement.executeUpdate("CREATE INDEX idx_supply_case ON tb_supply(N_CASE_ID)");
                System.out.println("耗材关联病例索引创建成功");
            } catch (SQLException e) {
                System.out.println("耗材关联病例索引已存在");
            }
            
        } catch (Exception e) {
            System.err.println("创建数据库或表时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 清空所有表数据
     */
    public static void clearAllData() {
        System.out.println("开始清空所有表数据...");
        Connection connection = null;
        Statement statement = null;
        
        try {
            // 加载数据库驱动
            Class.forName(dbClassName);
            
            // 建立连接
            String url = "jdbc:mysql://localhost:3306/db_mashup?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            connection = DriverManager.getConnection(url, dbUser, dbPassword);
            statement = connection.createStatement();
            
            // 清空表数据
            statement.executeUpdate("DELETE FROM tb_supply");
            System.out.println("耗材表数据已清空");
            
            statement.executeUpdate("DELETE FROM tb_case");
            System.out.println("病例表数据已清空");
            
            statement.executeUpdate("DELETE FROM tb_disease");
            System.out.println("病种表数据已清空");
            
        } catch (Exception e) {
            System.err.println("清空表数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 主方法，用于测试数据库初始化
     */
    public static void main(String[] args) {
        createDatabase();
        System.out.println("数据库初始化完成");
    }
}