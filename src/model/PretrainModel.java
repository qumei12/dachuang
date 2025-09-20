package model;

/**
 * 预训练模型启动类
 * 用于在系统部署时预先训练并保存LDA模型
 */
public class PretrainModel {
    public static void main(String[] args) {
        System.out.println("开始预训练LDA模型...");
        long startTime = System.currentTimeMillis();
        
        // 训练并保存模型
        ModelTrainer.trainAndSaveModel();
        
        long endTime = System.currentTimeMillis();
        System.out.println("预训练完成，总耗时: " + (endTime - startTime) + "ms");
        
        // 测试加载模型
        System.out.println("测试加载预训练模型...");
        LDAModel model = ModelTrainer.loadPretrainedModel();
        if (model != null) {
            System.out.println("模型加载成功");
            System.out.println("主题数量: " + model.getInterestAmount());
            System.out.println("病种数量: " + model.getDiseaseAmount());
            System.out.println("耗材数量: " + model.getSupplyAmount());
        } else {
            System.out.println("模型加载失败");
        }
    }
}