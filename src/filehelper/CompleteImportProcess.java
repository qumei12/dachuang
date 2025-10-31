package filehelper;

import dbhelper.DatabaseInitializer;

/**
 * 完整的数据导入流程示例
 * 包括数据库初始化和数据导入
 */
public class CompleteImportProcess {
    
    public static void main(String[] args) {
        System.out.println("开始完整的数据导入流程...");
        
        try {
            // 第一步：初始化数据库
            System.out.println("第一步：初始化数据库");
            DatabaseInitializer.createDatabase();
            
            // 第二步：导入数据
            System.out.println("\n第二步：导入数据");
            // 修改数据文件路径到D盘dachuang目录下的数据文件夹
            String basePath = "D:" + java.io.File.separator + "dachuang" + java.io.File.separator + "数据" + java.io.File.separator;
            
            String caseFilePath = basePath + "病例表.csv";
            String diseaseFilePath = basePath + "病种表.csv";
            String supplyFilePath = basePath + "耗材表.csv";
            
            System.out.println("病例文件路径: " + caseFilePath);
            System.out.println("病种文件路径: " + diseaseFilePath);
            System.out.println("耗材文件路径: " + supplyFilePath);
            
            // 检查文件是否存在
            java.io.File caseFile = new java.io.File(caseFilePath);
            java.io.File diseaseFile = new java.io.File(diseaseFilePath);
            java.io.File supplyFile = new java.io.File(supplyFilePath);
            
            if (!caseFile.exists()) {
                System.err.println("错误: 病例文件不存在: " + caseFilePath);
                return;
            }
            
            if (!diseaseFile.exists()) {
                System.err.println("错误: 病种文件不存在: " + diseaseFilePath);
                return;
            }
            
            if (!supplyFile.exists()) {
                System.err.println("错误: 耗材文件不存在: " + supplyFilePath);
                return;
            }
            
            System.out.println("所有文件都存在，开始导入数据...");
            
            // 执行数据导入
            DataImportUtil.importAllData(caseFilePath, diseaseFilePath, supplyFilePath);
            System.out.println("\n数据导入流程完成！");
            
        } catch (Exception e) {
            System.err.println("数据导入流程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}