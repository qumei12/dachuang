package test;

import model.LDAModel;
import model.ModelTrainer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 详细的Phi矩阵分析器
 * 提供更深入的Phi矩阵分析功能
 */
public class DetailedPhiMatrixAnalyzer {
    
    public static void main(String[] args) {
        try {
            // 加载预训练模型
            LDAModel ldaModel = ModelTrainer.loadPretrainedModel();
            
            if (ldaModel == null) {
                System.out.println("未找到预训练模型");
                return;
            }
            
            System.out.println("模型加载成功");
            System.out.println("主题数量: " + ldaModel.getTopicAmount());
            System.out.println("耗材数量: " + ldaModel.getSupplyAmount());
            
            // 执行详细分析
            performDetailedAnalysis(ldaModel);
            
        } catch (Exception e) {
            System.err.println("分析Phi矩阵时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 执行Phi矩阵的详细分析
     * @param ldaModel LDA模型实例
     */
    private static void performDetailedAnalysis(LDAModel ldaModel) {
        System.out.println("\n=== 执行Phi矩阵详细分析 ===");
        
        double[][] phi = ldaModel.getPhi();
        
        // 1. 检查矩阵的基本统计信息
        analyzeMatrixStatistics(phi);
        
        // 2. 查找稀疏性问题
        checkSparsity(phi);
        
        // 3. 分析主题分布
        analyzeTopicDistributions(phi);
        
        // 4. 查找异常值
        findOutliers(phi);
        
        // 5. 生成报告
        generateAnalysisReport(ldaModel, phi);
    }
    
    /**
     * 分析矩阵的基本统计信息
     * @param phi Phi矩阵
     */
    private static void analyzeMatrixStatistics(double[][] phi) {
        System.out.println("\n--- 矩阵统计信息 ---");
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0.0;
        int count = 0;
        int zeroCount = 0;
        
        for (int i = 0; i < phi.length; i++) {
            for (int j = 0; j < phi[i].length; j++) {
                double value = phi[i][j];
                if (value < min) min = value;
                if (value > max) max = value;
                sum += value;
                count++;
                if (value == 0.0) zeroCount++;
            }
        }
        
        double average = sum / count;
        double sparsity = (double) zeroCount / count * 100;
        
        System.out.println("最小值: " + String.format("%.8f", min));
        System.out.println("最大值: " + String.format("%.6f", max));
        System.out.println("平均值: " + String.format("%.8f", average));
        System.out.println("零值数量: " + zeroCount + "/" + count + " (" + String.format("%.2f", sparsity) + "%)");
    }
    
    /**
     * 检查矩阵稀疏性
     * @param phi Phi矩阵
     */
    private static void checkSparsity(double[][] phi) {
        System.out.println("\n--- 稀疏性检查 ---");
        
        DecimalFormat df = new DecimalFormat("#.####%");
        
        for (int topic = 0; topic < Math.min(5, phi.length); topic++) {
            int nonZeroCount = 0;
            for (int supply = 0; supply < phi[topic].length; supply++) {
                if (phi[topic][supply] > 0.0) {
                    nonZeroCount++;
                }
            }
            
            double sparsity = (double) nonZeroCount / phi[topic].length;
            System.out.println("主题 " + topic + ": " + nonZeroCount + "/" + phi[topic].length + 
                             " 非零值 (" + df.format(sparsity) + ")");
        }
    }
    
    /**
     * 分析主题分布
     * @param phi Phi矩阵
     */
    private static void analyzeTopicDistributions(double[][] phi) {
        System.out.println("\n--- 主题分布分析 ---");
        
        for (int topic = 0; topic < Math.min(5, phi.length); topic++) {
            // 计算主题分布的熵
            double entropy = 0.0;
            for (int supply = 0; supply < phi[topic].length; supply++) {
                double p = phi[topic][supply];
                if (p > 0) {
                    entropy -= p * Math.log(p);
                }
            }
            
            // 找到最大概率值
            double maxProb = 0.0;
            int maxSupply = -1;
            for (int supply = 0; supply < phi[topic].length; supply++) {
                if (phi[topic][supply] > maxProb) {
                    maxProb = phi[topic][supply];
                    maxSupply = supply;
                }
            }
            
            System.out.println("主题 " + topic + ":");
            System.out.println("  最大概率耗材: 耗材 " + maxSupply + " (概率: " + String.format("%.6f", maxProb) + ")");
            System.out.println("  分布熵: " + String.format("%.6f", entropy) + " (熵越高表示分布越均匀)");
        }
    }
    
    /**
     * 查找异常值
     * @param phi Phi矩阵
     */
    private static void findOutliers(double[][] phi) {
        System.out.println("\n--- 异常值检查 ---");
        
        double threshold = 0.5; // 概率阈值
        int outlierCount = 0;
        
        for (int topic = 0; topic < phi.length; topic++) {
            for (int supply = 0; supply < phi[topic].length; supply++) {
                if (phi[topic][supply] > threshold) {
                    if (outlierCount < 10) { // 只显示前10个异常值
                        System.out.println("异常值: 主题 " + topic + ", 耗材 " + supply + 
                                         ", 概率 " + String.format("%.6f", phi[topic][supply]));
                    }
                    outlierCount++;
                }
            }
        }
        
        System.out.println("总共发现 " + outlierCount + " 个异常值 (概率 > " + threshold + ")");
    }
    
    /**
     * 生成分析报告
     * @param ldaModel LDA模型
     * @param phi Phi矩阵
     */
    private static void generateAnalysisReport(LDAModel ldaModel, double[][] phi) {
        System.out.println("\n--- 分析报告 ---");
        
        // 检查Phi矩阵是否正确归一化
        boolean isNormalized = true;
        DecimalFormat df = new DecimalFormat("#.#####");
        
        System.out.println("检查每行（主题）概率和是否为1:");
        for (int topic = 0; topic < Math.min(10, phi.length); topic++) {
            double rowSum = 0.0;
            for (int supply = 0; supply < phi[topic].length; supply++) {
                rowSum += phi[topic][supply];
            }
            
            System.out.println("主题 " + topic + " 概率和: " + df.format(rowSum));
            if (Math.abs(rowSum - 1.0) > 1e-6) {
                isNormalized = false;
                System.out.println("  WARNING: 主题 " + topic + " 概率和不为1!");
            }
        }
        
        if (isNormalized) {
            System.out.println("✓ Phi矩阵已正确归一化");
        } else {
            System.out.println("✗ Phi矩阵未正确归一化，请检查updateEstimatedParameters方法");
        }
        
        // 检查是否有全零行
        int zeroRows = 0;
        for (int topic = 0; topic < phi.length; topic++) {
            boolean isZeroRow = true;
            for (int supply = 0; supply < phi[topic].length; supply++) {
                if (phi[topic][supply] != 0.0) {
                    isZeroRow = false;
                    break;
                }
            }
            if (isZeroRow) {
                zeroRows++;
                if (zeroRows <= 5) { // 只显示前5个全零行
                    System.out.println("WARNING: 主题 " + topic + " 是全零行");
                }
            }
        }
        
        if (zeroRows > 0) {
            System.out.println("发现 " + zeroRows + " 个全零行，请检查模型训练过程");
        }
    }
}