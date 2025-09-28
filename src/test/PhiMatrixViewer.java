package test;

import model.LDAModel;
import model.ModelTrainer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Phi矩阵查看器
 * 用于检查和分析LDA模型中的Phi矩阵（主题-耗材分布矩阵）
 */
public class PhiMatrixViewer {
    
    public static void main(String[] args) {
        try {
            // 加载预训练模型
            LDAModel ldaModel = ModelTrainer.loadPretrainedModel();
            
            if (ldaModel == null) {
                System.out.println("未找到预训练模型，开始训练新模型...");
                // 如果没有预训练模型，则进行实时训练
                ldaModel = new LDAModel();
                ldaModel.initializeLDAModel();
                ldaModel.inferenceModel();
                System.out.println("模型训练完成");
            }
            
            System.out.println("模型加载成功");
            System.out.println("主题数量: " + ldaModel.getTopicAmount());
            System.out.println("耗材数量: " + ldaModel.getSupplyAmount());
            
            // 显示Phi矩阵信息
            displayPhiMatrixInfo(ldaModel);
            
            // 保存Phi矩阵到CSV文件
            savePhiMatrixToCSV(ldaModel, "phi_matrix_output.csv");
            
            System.out.println("Phi矩阵信息已保存到 phi_matrix_output.csv");
            
        } catch (Exception e) {
            System.err.println("查看Phi矩阵时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 显示Phi矩阵的基本信息
     * @param ldaModel LDA模型实例
     */
    private static void displayPhiMatrixInfo(LDAModel ldaModel) {
        System.out.println("\n=== Phi矩阵信息 ===");
        double[][] phi = ldaModel.getPhi();
        
        // 检查矩阵维度
        System.out.println("Phi矩阵维度: " + phi.length + " x " + (phi.length > 0 ? phi[0].length : 0));
        
        // 显示每个主题中概率最高的几个耗材
        int topicCount = Math.min(5, ldaModel.getTopicAmount()); // 最多显示5个主题
        int topSupplies = 10; // 每个主题显示10个最相关的耗材
        
        System.out.println("\n前 " + topicCount + " 个主题中最相关的耗材:");
        for (int topic = 0; topic < topicCount; topic++) {
            System.out.println("\n主题 " + topic + ":");
            
            // 创建索引数组并按概率排序
            Double[] probabilities = new Double[ldaModel.getSupplyAmount()];
            Integer[] supplyIndices = new Integer[ldaModel.getSupplyAmount()];
            
            for (int supply = 0; supply < ldaModel.getSupplyAmount(); supply++) {
                probabilities[supply] = phi[topic][supply];
                supplyIndices[supply] = supply;
            }
            
            // 按概率降序排序
            for (int i = 0; i < probabilities.length - 1; i++) {
                for (int j = 0; j < probabilities.length - 1 - i; j++) {
                    if (probabilities[j] < probabilities[j + 1]) {
                        // 交换概率
                        double tempProb = probabilities[j];
                        probabilities[j] = probabilities[j + 1];
                        probabilities[j + 1] = tempProb;
                        
                        // 交换索引
                        int tempIndex = supplyIndices[j];
                        supplyIndices[j] = supplyIndices[j + 1];
                        supplyIndices[j + 1] = tempIndex;
                    }
                }
            }
            
            // 显示前topSupplies个最相关的耗材
            for (int i = 0; i < Math.min(topSupplies, probabilities.length); i++) {
                if (probabilities[i] > 0.001) { // 只显示概率大于0.1%的耗材
                    System.out.println("  耗材索引 " + supplyIndices[i] + ": " + 
                                     String.format("%.4f", probabilities[i]) + 
                                     " (" + String.format("%.2f", probabilities[i] * 100) + "%)");
                }
            }
        }
    }
    
    /**
     * 将Phi矩阵保存到CSV文件
     * @param ldaModel LDA模型实例
     * @param filename 输出文件名
     * @throws IOException 文件写入异常
     */
    private static void savePhiMatrixToCSV(LDAModel ldaModel, String filename) throws IOException {
        double[][] phi = ldaModel.getPhi();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // 写入CSV头部
            writer.print("主题\\耗材");
            for (int supply = 0; supply < ldaModel.getSupplyAmount(); supply++) {
                writer.print(",耗材_" + supply);
            }
            writer.println();
            
            // 写入每行数据
            for (int topic = 0; topic < ldaModel.getTopicAmount(); topic++) {
                writer.print("主题_" + topic);
                for (int supply = 0; supply < ldaModel.getSupplyAmount(); supply++) {
                    writer.print("," + phi[topic][supply]);
                }
                writer.println();
            }
        }
        
        System.out.println("\nPhi矩阵已保存到 " + filename);
        System.out.println("文件格式: 每行代表一个主题，每列代表一个耗材，单元格值为概率");
    }
}