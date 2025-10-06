package model;

import java.io.*;
import java.util.*;

import filehelper.CaseSupplyMatrixService;
import dbhelper.DBSearch;
import javabean.Disease;
import com.wangyan.index.DiseaseMap;
import com.wangyan.index.SupplyMap;

public class ModelTrainer {
    private static String MODEL_DIR = "models";
    private static String PHI_FILE = MODEL_DIR + "/phi.dat";
    private static String THETA_FILE = MODEL_DIR + "/theta.dat";
    private static String CONFIG_FILE = MODEL_DIR + "/config.dat";
    private static String PRECOMPUTED_DIR = MODEL_DIR + "/precomputed";
    
    /**
     * 设置模型目录，使用相对于项目根目录的路径
     * @param contextPath Web应用程序上下文路径
     */
    public static void setContextPath(String contextPath) {
        // 获取项目根目录路径
        File contextDir = new File(contextPath);

        File projectRootDir = contextDir.getParentFile().getParentFile().getParentFile();
        
        // 确保路径分隔符统一并添加末尾斜杠
        String projectRootPath = projectRootDir.getAbsolutePath().replace("\\", "/");
        if (!projectRootPath.endsWith("/")) {
            projectRootPath += "/";
        }
        
        // 设置模型目录为项目根目录下的models目录
        MODEL_DIR = projectRootPath + "models";
        PHI_FILE = MODEL_DIR + "/phi.dat";
        THETA_FILE = MODEL_DIR + "/theta.dat";
        CONFIG_FILE = MODEL_DIR + "/config.dat";
        PRECOMPUTED_DIR = MODEL_DIR + "/precomputed";
        
        System.out.println("模型目录已设置为: " + MODEL_DIR);
    }
    
    /**
     * 预训练LDA模型并保存到文件，同时预计算所有病种的推荐结果
     */
    public static void trainAndSaveModel() {
        System.out.println("开始预训练LDA模型...");
        
        // 创建模型目录
        File modelDir = new File(MODEL_DIR);
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
        
        // 初始化LDA模型
        LDAModel ldaModel = new LDAModel();
        ldaModel.initializeLDAModel();
        
        System.out.println("模型初始化完成，开始迭代训练...");
        
        // 进行模型训练
        long startTime = System.currentTimeMillis();
        trainModelWithConvergenceCheck(ldaModel);
        long endTime = System.currentTimeMillis();
        
        System.out.println("模型训练完成，耗时: " + (endTime - startTime) + "ms");
        
        // 保存模型参数
        saveModel(ldaModel);
        
        System.out.println("模型已保存到 " + MODEL_DIR + " 目录");
    }
    
    /**
     * 带收敛检测的模型训练
     * @param model LDA模型
     */
    private static void trainModelWithConvergenceCheck(LDAModel model) {
        int totalIterations = model.iterations;
        int progressInterval = Math.max(1, totalIterations / 100); // 至少每1%显示一次进度
        
        // 收敛检测参数
        double phiConvergenceThreshold = 1e-4;   // Phi矩阵收敛阈值，更严格的收敛条件
        double thetaConvergenceThreshold = 1e-3; // Theta矩阵收敛阈值
        int maxIterationsWithoutImprovement = 10; // 最大无改善迭代次数
        int iterationsWithoutImprovement = 0;
        
        System.out.println("开始训练，总共 " + totalIterations + " 次迭代");
        System.out.println("收敛阈值: Phi=" + phiConvergenceThreshold + ", Theta=" + thetaConvergenceThreshold);
        printProgressBar(0, totalIterations);
        
        // 保存前一次的矩阵用于计算变化量
        double[][] prevPhi = cloneMatrix(model.phi);
        double[][] prevTheta = cloneMatrix(model.theta);
        
        // 记录变化量历史用于分析收敛趋势
        List<Double> phiChangeHistory = new ArrayList<>();
        List<Double> thetaChangeHistory = new ArrayList<>();
        
        // 调整收敛检测频率，确保有足够的检测点
        int convergenceCheckInterval = Math.max(1, model.saveStep / 2);
        
        // 记录最小变化量用于判断是否陷入局部最优
        double minPhiChange = Double.MAX_VALUE;
        double minThetaChange = Double.MAX_VALUE;
        int minChangeNotImprovedCount = 0;
        int maxMinChangeNotImprovedCount = 5; // 最小变化量连续无改善次数阈值
        
        for (int i = 0; i < totalIterations; i++) {
            // 使用Gibbs采样更新主题分配
            for (int m = 0; m < model.caseAmount; m++) {
                // 正确遍历z矩阵，而不是CasesSupplies矩阵
                for (int n = 0; n < model.z[m].length; n++) {
                    int newTopic = model.sampleTopicZ(m, n);
                    model.z[m][n] = newTopic;
                }
            }
            
            // 定期更新参数估计和收敛检测
            if ((i >= model.beginSaveIters) && (((i - model.beginSaveIters) % convergenceCheckInterval) == 0)) {
                model.updateEstimatedParameters();
                
                // 计算矩阵变化量
                double phiChange = calculateMatrixChange(prevPhi, model.phi);
                double thetaChange = calculateMatrixChange(prevTheta, model.theta);
                
                // 保存变化量历史
                phiChangeHistory.add(phiChange);
                thetaChangeHistory.add(thetaChange);
                
                // 保存当前矩阵用于下次比较
                prevPhi = cloneMatrix(model.phi);
                prevTheta = cloneMatrix(model.theta);
                
                // 输出详细收敛信息
                System.out.printf("%n迭代 %d/%d, Phi变化量: %.8f, Theta变化量: %.8f", 
                    i + 1, totalIterations, phiChange, thetaChange);
                
                // 计算最近几次迭代的平均变化量
                if (phiChangeHistory.size() >= 10) {
                    double avgPhiChange = calculateAverage(phiChangeHistory, 10);
                    double avgThetaChange = calculateAverage(thetaChangeHistory, 10);
                    System.out.printf(", 最近10次平均: Phi=%.8f, Theta=%.8f", avgPhiChange, avgThetaChange);
                }
                
                // 检查收敛性 - 分别判断Phi和Theta的收敛情况
                boolean isPhiConverged = phiChange < phiConvergenceThreshold;
                boolean isThetaConverged = thetaChange < thetaConvergenceThreshold;
                
                // 检查是否达到最小变化量
                boolean isMinPhiNotImproved = (phiChange > minPhiChange * 1.1); // 当前变化量比最小值高10%以上
                boolean isMinThetaNotImproved = (thetaChange > minThetaChange * 1.1);
                
                if (isMinPhiNotImproved && isMinThetaNotImproved) {
                    minChangeNotImprovedCount++;
                } else {
                    minChangeNotImprovedCount = 0;
                }
                
                // 如果Phi和Theta都收敛，或者陷入局部最优，则增加收敛计数
                if ((isPhiConverged && isThetaConverged) || 
                    (minChangeNotImprovedCount >= maxMinChangeNotImprovedCount)) {
                    iterationsWithoutImprovement++;
                    System.out.printf(" (连续收敛次数: %d/%d)", iterationsWithoutImprovement, maxIterationsWithoutImprovement);
                    if (iterationsWithoutImprovement >= maxIterationsWithoutImprovement) {
                        if (minChangeNotImprovedCount >= maxMinChangeNotImprovedCount) {
                            System.out.println("\n模型可能陷入局部最优，提前停止训练");
                        } else {
                            System.out.println("\n模型已收敛，提前停止训练");
                        }
                        break;
                    }
                } else {
                    if (iterationsWithoutImprovement > 0) {
                        System.out.printf(" (收敛中断，变化量超过阈值)");
                    }
                    iterationsWithoutImprovement = 0; // 重置计数器
                }
                
                // 检查是否达到最大迭代次数
                if (i + 1 >= totalIterations) {
                    System.out.println("\n达到最大迭代次数，停止训练");
                    break;
                }
                
                // 每50次迭代输出一次统计信息
                if ((i + 1) % 50 == 0 && i > 0) {
                    System.out.println();
                    printConvergenceStats(phiChangeHistory, thetaChangeHistory, i + 1);
                }
            } else if ((i + 1) % progressInterval == 0 || i == totalIterations - 1) {
                // 定期更新进度条（不进行收敛检测时）
                printProgressBar(i + 1, totalIterations);
                
                // 关键修复：即使不进行收敛检测，也要定期更新参数估计
                // 这确保了模型参数在整个训练过程中持续更新
                if (i >= model.beginSaveIters) {
                    model.updateEstimatedParameters();
                }
            }
        }
        
        // 确保最后更新参数
        model.updateEstimatedParameters();
        System.out.println(); // 换行，避免进度条影响后续输出
        
        // 输出最终收敛统计
        printFinalConvergenceStats(phiChangeHistory, thetaChangeHistory);
    }
    
    /**
     * 计算列表最后n个元素的平均值
     * @param list 数据列表
     * @param n 元素个数
     * @return 平均值
     */
    private static double calculateAverage(List<Double> list, int n) {
        if (list.size() < n) return 0.0;
        
        double sum = 0.0;
        for (int i = list.size() - n; i < list.size(); i++) {
            sum += list.get(i);
        }
        return sum / n;
    }
    
    /**
     * 打印收敛统计信息
     * @param phiChangeHistory Phi变化量历史
     * @param thetaChangeHistory Theta变化量历史
     * @param iteration 当前迭代次数
     */
    private static void printConvergenceStats(List<Double> phiChangeHistory, List<Double> thetaChangeHistory, int iteration) {
        if (phiChangeHistory.isEmpty()) return;
        
        double minPhi = Collections.min(phiChangeHistory);
        double maxPhi = Collections.max(phiChangeHistory);
        double avgPhi = phiChangeHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double minTheta = Collections.min(thetaChangeHistory);
        double maxTheta = Collections.max(thetaChangeHistory);
        double avgTheta = thetaChangeHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        System.out.printf("  迭代 %d 统计 - Phi变化量 [最小: %.8f, 最大: %.8f, 平均: %.8f]%n", 
            iteration, minPhi, maxPhi, avgPhi);
        System.out.printf("  迭代 %d 统计 - Theta变化量 [最小: %.8f, 最大: %.8f, 平均: %.8f]%n", 
            iteration, minTheta, maxTheta, avgTheta);
    }
    
    /**
     * 打印最终收敛统计信息
     * @param phiChangeHistory Phi变化量历史
     * @param thetaChangeHistory Theta变化量历史
     */
    private static void printFinalConvergenceStats(List<Double> phiChangeHistory, List<Double> thetaChangeHistory) {
        if (phiChangeHistory.isEmpty()) return;
        
        System.out.println("\n=== 最终收敛统计 ===");
        System.out.println("总迭代次数: " + phiChangeHistory.size());
        
        if (phiChangeHistory.size() >= 10) {
            double recentAvgPhi = calculateAverage(phiChangeHistory, 10);
            double recentAvgTheta = calculateAverage(thetaChangeHistory, 10);
            System.out.printf("最近10次迭代平均变化量 - Phi: %.8f, Theta: %.8f%n", recentAvgPhi, recentAvgTheta);
        }
        
        if (phiChangeHistory.size() >= 50) {
            double recentAvgPhi = calculateAverage(phiChangeHistory, 50);
            double recentAvgTheta = calculateAverage(thetaChangeHistory, 50);
            System.out.printf("最近50次迭代平均变化量 - Phi: %.8f, Theta: %.8f%n", recentAvgPhi, recentAvgTheta);
        }
        
        double overallAvgPhi = phiChangeHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double overallAvgTheta = thetaChangeHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        System.out.printf("整体平均变化量 - Phi: %.8f, Theta: %.8f%n", overallAvgPhi, overallAvgTheta);
    }
    
    /**
     * 计算两个矩阵之间的变化量（Frobenius范数）
     * @param prevMatrix 前一次的矩阵
     * @param currMatrix 当前矩阵
     * @return 矩阵变化量
     */
    private static double calculateMatrixChange(double[][] prevMatrix, double[][] currMatrix) {
        if (prevMatrix == null || currMatrix == null) {
            return Double.MAX_VALUE;
        }
        
        double sumSquaredDiff = 0.0;
        int rows = prevMatrix.length;
        int cols = prevMatrix[0].length;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double diff = currMatrix[i][j] - prevMatrix[i][j];
                sumSquaredDiff += diff * diff;
            }
        }
        
        return Math.sqrt(sumSquaredDiff / (rows * cols)); // 归一化处理
    }
    
    /**
     * 克隆矩阵
     * @param matrix 原矩阵
     * @return 克隆的矩阵
     */
    private static double[][] cloneMatrix(double[][] matrix) {
        if (matrix == null) {
            return null;
        }
        
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] cloned = new double[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrix[i], 0, cloned[i], 0, cols);
        }
        
        return cloned;
    }
    
    /**
     * 打印进度条
     * @param current 当前迭代次数
     * @param total 总迭代次数
     */
    private static void printProgressBar(int current, int total) {
        int barLength = 50; // 进度条长度
        double progress = (double) current / total;
        int completedLength = (int) (progress * barLength);
        
        StringBuilder sb = new StringBuilder();
        sb.append("\r[");
        
        // 已完成部分
        for (int i = 0; i < completedLength; i++) {
            sb.append("=");
        }
        
        // 当前位置
        if (completedLength < barLength) {
            sb.append(">");
            completedLength++;
        }
        
        // 未完成部分
        for (int i = completedLength; i < barLength; i++) {
            sb.append(" ");
        }
        
        sb.append("] ");
        sb.append(String.format("%.1f", progress * 100)).append("% ");
        sb.append("(").append(current).append("/").append(total).append(")");
        
        System.out.print(sb.toString());
        System.out.flush();
    }
    
    /**
     * 保存模型参数到文件
     */
    private static void saveModel(LDAModel model) {
        try {
            // 先保存配置信息
            saveConfig(model, CONFIG_FILE);
            System.out.println("配置信息已保存至 " + CONFIG_FILE);

            // 保存phi矩阵 (主题-耗材分布)
            saveMatrix(model.getPhi(), PHI_FILE);
            System.out.println("Phi矩阵已保存至 " + PHI_FILE);

            // 保存theta矩阵 (病种-主题分布)
            saveMatrix(model.getTheta(), THETA_FILE);
            System.out.println("Theta矩阵已保存至 " + THETA_FILE);

        } catch (IOException e) {
            System.err.println("保存模型时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从文件加载预训练模型
     */
    public static LDAModel loadPretrainedModel() {
        System.out.println("当前工作目录: " + new File(".").getAbsolutePath());
        System.out.println("检查模型文件路径:");
        System.out.println("  PHI_FILE: " + new File(PHI_FILE).getAbsolutePath());
        System.out.println("  THETA_FILE: " + new File(THETA_FILE).getAbsolutePath());
        System.out.println("  CONFIG_FILE: " + new File(CONFIG_FILE).getAbsolutePath());
        
        // 检查模型文件是否存在
        if (!isPretrainedModelExists()) {
            System.out.println("预训练模型文件不存在，需要先进行预训练");
            return null;
        }
        
        try {
            System.out.println("正在加载预训练模型...");
            
            // 加载配置信息
            Properties config = loadConfig(CONFIG_FILE);
            int topicAmount = Integer.parseInt(config.getProperty("topicAmount"));
            int caseAmount = Integer.parseInt(config.getProperty("caseAmount"));
            int supplyAmount = Integer.parseInt(config.getProperty("supplyAmount"));
            
            // 创建LDA模型实例
            LDAModel model = new LDAModel();
            model.setTopicAmount(topicAmount);
            model.setCaseAmount(caseAmount);
            model.setSupplyAmount(supplyAmount);
            model.setAlpha(Double.parseDouble(config.getProperty("alpha")));
            model.setBeta(Double.parseDouble(config.getProperty("beta")));
            model.setIterations(Integer.parseInt(config.getProperty("iterations")));
            
            // 加载phi矩阵 (主题-耗材分布)
            model.setPhi(loadMatrix(PHI_FILE, topicAmount, supplyAmount));
            
            // 加载theta矩阵 (病案-主题分布)
            model.setTheta(loadMatrix(THETA_FILE, caseAmount, topicAmount));
            
            System.out.println("预训练模型加载成功");
            System.out.println("模型信息 - 主题数: " + model.getTopicAmount() + 
                             ", 病案数: " + model.getCaseAmount() + 
                             ", 耗材数: " + model.getSupplyAmount());
            return model;
            
        } catch (Exception e) {
            System.err.println("加载预训练模型时出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 保存矩阵到文件
     */
    private static void saveMatrix(double[][] matrix, String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(matrix);
        }
    }
    
    /**
     * 从文件加载矩阵
     */
    private static double[][] loadMatrix(String filename, int rows, int cols) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (double[][]) ois.readObject();
        }
    }
    
    /**
     * 保存配置信息
     */
    private static void saveConfig(LDAModel model, String filename) throws IOException {
        Properties config = new Properties();
        config.setProperty("topicAmount", String.valueOf(model.getTopicAmount()));
        config.setProperty("caseAmount", String.valueOf(model.getCaseAmount()));
        config.setProperty("supplyAmount", String.valueOf(model.getSupplyAmount()));
        config.setProperty("alpha", String.valueOf(model.getAlpha()));
        config.setProperty("beta", String.valueOf(model.getBeta()));
        config.setProperty("iterations", String.valueOf(model.getIterations()));
        
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            config.store(fos, "LDA Model Configuration");
        }
    }
    
    /**
     * 加载配置信息
     */
    private static Properties loadConfig(String filename) throws IOException {
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(filename)) {
            config.load(fis);
        }
        return config;
    }
    
    /**
     * 检查预训练模型是否存在
     */
    public static boolean isPretrainedModelExists() {
        File phiFile = new File(PHI_FILE);
        File thetaFile = new File(THETA_FILE);
        File configFile = new File(CONFIG_FILE);
        
        boolean exists = phiFile.exists() && thetaFile.exists() && configFile.exists();
        
        System.out.println("模型文件检查结果:");
        System.out.println("  Phi文件存在: " + phiFile.exists() + " (" + phiFile.getAbsolutePath() + ")");
        System.out.println("  Theta文件存在: " + thetaFile.exists() + " (" + thetaFile.getAbsolutePath() + ")");
        System.out.println("  配置文件存在: " + configFile.exists() + " (" + configFile.getAbsolutePath() + ")");
        
        return exists;
    }
}