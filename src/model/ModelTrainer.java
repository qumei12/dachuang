package model;

import java.io.*;
import java.util.*;

import filehelper.GetUserService;

public class ModelTrainer {
    private static final String MODEL_DIR = "D:/dachuang/models";
    private static final String PHI_FILE = MODEL_DIR + "/phi.dat";
    private static final String THETA_FILE = MODEL_DIR + "/theta.dat";
    private static final String CONFIG_FILE = MODEL_DIR + "/config.dat";
    
    /**
     * 预训练LDA模型并保存到文件
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
        ldaModel.inferenceModel();
        long endTime = System.currentTimeMillis();
        
        System.out.println("模型训练完成，耗时: " + (endTime - startTime) + "ms");
        
        // 保存模型参数
        saveModel(ldaModel);
        
        System.out.println("模型已保存到 " + MODEL_DIR + " 目录");
    }
    
    /**
     * 保存模型参数到文件
     */
    private static void saveModel(LDAModel model) {
        try {
            // 保存phi矩阵 (主题-耗材分布)
            saveMatrix(model.getPhi(), PHI_FILE);
            
            // 保存theta矩阵 (病种-主题分布)
            saveMatrix(model.getTheta(), THETA_FILE);
            
            // 保存配置信息
            saveConfig(model, CONFIG_FILE);
            
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
            int interestAmount = Integer.parseInt(config.getProperty("interestAmount"));
            int diseaseAmount = Integer.parseInt(config.getProperty("diseaseAmount"));
            int supplyAmount = Integer.parseInt(config.getProperty("supplyAmount"));
            
            // 创建LDA模型实例
            LDAModel model = new LDAModel();
            model.setInterestAmount(interestAmount);
            model.setDiseaseAmount(diseaseAmount);
            model.setSupplyAmount(supplyAmount);
            model.setAlpha(Double.parseDouble(config.getProperty("alpha")));
            model.setBeta(Double.parseDouble(config.getProperty("beta")));
            model.setIterations(Integer.parseInt(config.getProperty("iterations")));
            
            // 加载phi矩阵 (主题-耗材分布)
            model.setPhi(loadMatrix(PHI_FILE, interestAmount, supplyAmount));
            
            // 加载theta矩阵 (病种-主题分布)
            model.setTheta(loadMatrix(THETA_FILE, diseaseAmount, interestAmount));
            
            System.out.println("预训练模型加载成功");
            System.out.println("模型信息 - 主题数: " + model.getInterestAmount() + 
                             ", 病种数: " + model.getDiseaseAmount() + 
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
        config.setProperty("interestAmount", String.valueOf(model.getInterestAmount()));
        config.setProperty("diseaseAmount", String.valueOf(model.getDiseaseAmount()));
        config.setProperty("supplyAmount", String.valueOf(model.getSupplyAmount()));
        config.setProperty("alpha", String.valueOf(model.alpha));
        config.setProperty("beta", String.valueOf(model.beta));
        config.setProperty("iterations", String.valueOf(model.iterations));
        
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