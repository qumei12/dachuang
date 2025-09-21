package filehelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import dbhelper.DBHelper;

/**
 * 数据导入工具类
 * 用于将病种耗材相关的CSV数据导入到数据库中
 */
public class DataImportUtil {
    
    /**
     * 导入病例表数据
     * @param filePath CSV文件路径
     */
    public static void importCaseData(String filePath) {
        System.out.println("开始导入病例数据: " + filePath);
        
        // 检查文件是否存在
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("错误: 找不到病例数据文件 " + filePath);
            System.err.println("当前工作目录: " + System.getProperty("user.dir"));
            return;
        }
        
        Connection connection = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            connection = DBHelper.getConnection();
            if (connection == null) {
                System.err.println("错误: 无法获取数据库连接");
                return;
            }
            
            // 清空表数据
            stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM tb_case");
            System.out.println("已清空病例表数据");
            stmt.close();
            
            String sql = "INSERT INTO tb_case (N_ID, N_MASHUP_ID, C_CASE_ID) VALUES (?, ?, ?)";
            pstmt = connection.prepareStatement(sql);
            
            String line;
            int count = 0;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 3) {
                    try {
                        int id = Integer.parseInt(values[0].trim());
                        int mashupId = Integer.parseInt(values[1].trim());
                        String caseId = values[2].trim();
                        
                        pstmt.setInt(1, id);
                        pstmt.setInt(2, mashupId);
                        pstmt.setString(3, caseId);
                        
                        pstmt.addBatch();
                        count++;
                        
                        // 每1000条记录执行一次批处理
                        if (count % 1000 == 0) {
                            pstmt.executeBatch();
                            System.out.println("已导入病例数据: " + count + " 条");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("数据格式错误，跳过该行: " + line);
                    } catch (SQLException e) {
                        System.err.println("数据库插入错误，跳过该行: " + line);
                        e.printStackTrace();
                    }
                }
            }
            
            // 执行剩余的批处理
            pstmt.executeBatch();
            System.out.println("病例数据导入完成，总共: " + count + " 条");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (pstmt != null) {
                    pstmt.close();
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
     * 导入病种表数据
     * @param filePath CSV文件路径
     */
    public static void importDiseaseData(String filePath) {
        System.out.println("开始导入病种数据: " + filePath);
        
        // 检查文件是否存在
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("错误: 找不到病种数据文件 " + filePath);
            System.err.println("当前工作目录: " + System.getProperty("user.dir"));
            return;
        }
        
        Connection connection = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            connection = DBHelper.getConnection();
            if (connection == null) {
                System.err.println("错误: 无法获取数据库连接");
                return;
            }
            
            // 清空表数据
            stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM tb_disease");
            System.out.println("已清空病种表数据");
            stmt.close();
            
            String sql = "INSERT INTO tb_disease (N_ID, C_NAME, C_DESCRIPTION) VALUES (?, ?, ?)";
            pstmt = connection.prepareStatement(sql);
            
            String line;
            int count = 0;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 3) {
                    try {
                        int id = Integer.parseInt(values[0].trim());
                        String name = values[1].trim();
                        String description = values[2].trim();
                        
                        pstmt.setInt(1, id);
                        pstmt.setString(2, name);
                        pstmt.setString(3, description);
                        
                        pstmt.addBatch();
                        count++;
                        
                        // 每1000条记录执行一次批处理
                        if (count % 1000 == 0) {
                            pstmt.executeBatch();
                            System.out.println("已导入病种数据: " + count + " 条");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("数据格式错误，跳过该行: " + line);
                    } catch (SQLException e) {
                        System.err.println("数据库插入错误，跳过该行: " + line);
                        e.printStackTrace();
                    }
                }
            }
            
            // 执行剩余的批处理
            pstmt.executeBatch();
            System.out.println("病种数据导入完成，总共: " + count + " 条");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (pstmt != null) {
                    pstmt.close();
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
     * 导入耗材表数据
     * @param filePath CSV文件路径
     */
    public static void importSupplyData(String filePath) {
        System.out.println("开始导入耗材数据: " + filePath);
        
        // 检查文件是否存在
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("错误: 找不到耗材数据文件 " + filePath);
            System.err.println("当前工作目录: " + System.getProperty("user.dir"));
            return;
        }
        
        Connection connection = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            connection = DBHelper.getConnection();
            if (connection == null) {
                System.err.println("错误: 无法获取数据库连接");
                return;
            }
            
            // 清空表数据
            stmt = connection.createStatement();
            stmt.executeUpdate("DELETE FROM tb_supply");
            System.out.println("已清空耗材表数据");
            stmt.close();
            
            String sql = "INSERT INTO tb_supply (N_ID, N_CASE_ID, C_NAME, C_PRODUCT_NAME, C_SPECIFICATION, C_PRICE, C_QUANTITY) VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = connection.prepareStatement(sql);
            
            String line;
            int count = 0;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 使用更复杂的CSV解析逻辑来处理带引号的字段
                String[] values = parseCsvLine(line);
                if (values.length >= 7) {
                    try {
                        int id = Integer.parseInt(values[0].trim());
                        int caseId = Integer.parseInt(values[1].trim());
                        String name = values[2].trim();
                        String productName = values[3].trim();
                        String specification = values[4].trim();
                        double price = parsePrice(values[5].trim());
                        int quantity = Integer.parseInt(values[6].trim());
                        
                        pstmt.setInt(1, id);
                        pstmt.setInt(2, caseId);
                        pstmt.setString(3, name);
                        pstmt.setString(4, productName);
                        pstmt.setString(5, specification);
                        pstmt.setDouble(6, price);
                        pstmt.setInt(7, quantity);
                        
                        pstmt.addBatch();
                        count++;
                        
                        // 每1000条记录执行一次批处理
                        if (count % 1000 == 0) {
                            pstmt.executeBatch();
                            System.out.println("已导入耗材数据: " + count + " 条");
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("数据格式错误，跳过该行: " + line);
                        System.err.println("具体错误: " + e.getMessage());
                    } catch (SQLException e) {
                        System.err.println("数据库插入错误，跳过该行: " + line);
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.err.println("处理数据时发生错误，跳过该行: " + line);
                        System.err.println("具体错误: " + e.getMessage());
                    }
                }
            }
            
            // 执行剩余的批处理
            pstmt.executeBatch();
            System.out.println("耗材数据导入完成，总共: " + count + " 条");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (pstmt != null) {
                    pstmt.close();
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
     * 解析价格字段，支持多种数字格式
     * @param priceStr 价格字符串
     * @return 解析后的double值
     * @throws NumberFormatException 如果无法解析为有效数字
     */
    private static double parsePrice(String priceStr) throws NumberFormatException {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new NumberFormatException("价格字段为空");
        }
        
        // 去除可能的空格
        String cleanPrice = priceStr.trim();
        
        // 替换可能的逗号小数点为标准小数点
        if (cleanPrice.contains(",")) {
            // 检查是否是千位分隔符还是小数点
            int lastCommaIndex = cleanPrice.lastIndexOf(",");
            int lastDotIndex = cleanPrice.lastIndexOf(".");
            
            // 如果逗号在点之后，或者没有点，那么逗号可能是小数点
            if (lastCommaIndex > lastDotIndex) {
                // 替换最后一个逗号为点，其他逗号移除（千位分隔符）
                StringBuilder sb = new StringBuilder(cleanPrice);
                // 移除所有逗号
                for (int i = sb.length() - 1; i >= 0; i--) {
                    if (sb.charAt(i) == ',') {
                        sb.deleteCharAt(i);
                    }
                }
                // 在原来最后一个逗号位置插入点
                if (lastCommaIndex < cleanPrice.length() - 1) {
                    sb.insert(lastCommaIndex, '.');
                }
                cleanPrice = sb.toString();
            } else {
                // 逗号是千位分隔符，直接移除
                cleanPrice = cleanPrice.replace(",", "");
            }
        }
        
        try {
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("无法解析价格: " + priceStr + " (清理后: " + cleanPrice + ")");
        }
    }
    
    /**
     * 解析CSV行，支持带引号的字段
     * @param line CSV行内容
     * @return 字段数组
     */
    private static String[] parseCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return new String[0];
        }
        
        ArrayList<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // 检查是否是转义引号
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // 双引号表示一个引号字符
                    current.append('"');
                    i++; // 跳过下一个引号
                } else {
                    // 切换引号状态
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        // 添加最后一个字段
        values.add(current.toString());
        
        // 清理每个字段值，去除首尾空格但保留引号内的空格
        String[] result = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).trim();
        }
        
        return result;
    }
    
    /**
     * 一次性导入所有数据
     * @param caseFilePath 病例表文件路径
     * @param diseaseFilePath 病种表文件路径
     * @param supplyFilePath 耗材表文件路径
     */
    public static void importAllData(String caseFilePath, String diseaseFilePath, String supplyFilePath) {
        System.out.println("开始导入所有数据...");
        long startTime = System.currentTimeMillis();
        
        importCaseData(caseFilePath);
        importDiseaseData(diseaseFilePath);
        importSupplyData(supplyFilePath);
        
        long endTime = System.currentTimeMillis();
        System.out.println("所有数据导入完成，总耗时: " + (endTime - startTime) + "ms");
    }
}