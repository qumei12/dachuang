
## 系统概述

本系统是一个基于LDA（Latent Dirichlet Allocation）主题模型的医疗耗材推荐系统。通过分析病种与耗材之间的关联关系，为特定病种推荐最相关的耗材组合，辅助医生进行医疗决策。

## 系统架构

系统采用Java Web技术栈，基于Tomcat服务器运行，主要包含以下组件：

- **前端**: JSP + HTML + CSS + JavaScript
- **后端**: Java Servlet
- **数据库**: MySQL
- **推荐算法**: LDA主题模型
- **数据**: 病种、病例、耗材三类数据

## 目录结构

```
dachuang/
├── src/                 # Java源代码
├── WebContent/          # Web前端资源
├── 数据/                # CSV数据文件
├── models/              # 预训练模型文件
├── build/               # 编译输出目录
└── README.md            # 系统文档
```

## 数据源

系统使用两种数据源：

1. **主数据源**：`D:\数据\单病种用耗推荐模型_数据源.csv` - 包含完整的病案、耗材及相关信息的综合数据文件
2. **分表数据源**（用于训练）：
   - `D:\数据\病例表.csv` - 病例数据
   - `D:\数据\病种表.csv` - 病种数据
   - `D:\数据\耗材表.csv` - 耗材数据

## 数据结构

系统使用三种核心数据表：

1. **病种表(tb_disease)**
   - N_ID: 病种ID
   - C_NAME: 病种名称
   - C_DESCRIPTION: 病种描述

2. **病例表(tb_case)**
   - N_ID: 病例ID
   - N_MASHUP_ID: 关联的病种ID
   - C_CASE_ID: 病例编号

3. **耗材表(tb_supply)**
   - N_ID: 耗材ID
   - N_CASE_ID: 关联的病例ID
   - C_NAME: 耗材名称
   - C_PRODUCT_NAME: 产品名称
   - C_SPECIFICATION: 规格
   - C_PRICE: 单价
   - C_QUANTITY: 数量

## 系统启动流程

### 1. 环境准备

确保已安装以下软件：
- Java JDK 8或以上版本
- Tomcat 8.0或以上版本
- MySQL数据库

### 2. 数据库配置

在`src/dbhelper/DBHelper.java`中配置数据库连接信息：
```java
private static String dbClassName = "com.mysql.cj.jdbc.Driver";
private static String dbUrl = "jdbc:mysql://localhost:3306/db_mashup?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static String dbUser = "root";
private static String dbPassword = "root";
```

### 3. 数据库初始化和数据导入

#### 3.1 初始化数据库
运行`src/dbhelper/DatabaseInitializer.java`创建数据库和表结构：
```bash
javac -cp "src;WebContent/WEB-INF/lib/*" src/dbhelper/DatabaseInitializer.java
java -cp "src;WebContent/WEB-INF/lib/*" dbhelper.DatabaseInitializer
```

#### 3.2 导入数据
运行`src/filehelper/CompleteImportProcess.java`导入CSV数据：
```bash
javac -cp "src;WebContent/WEB-INF/lib/*" src/filehelper/CompleteImportProcess.java
java -cp "src;WebContent/WEB-INF/lib/*" filehelper.CompleteImportProcess
```

### 4. 模型预处理

#### 4.1 模型训练
系统支持两种模型使用方式：

1. **预训练模型**（推荐）
   运行`src/model/ModelTrainer.java`进行模型预训练：
   ```bash
   javac -cp "src;WebContent/WEB-INF/lib/*" src/model/ModelTrainer.java
   java -cp "src;WebContent/WEB-INF/lib/*" model.ModelTrainer
   ```

2. **实时训练**
   如果没有预训练模型，系统会在首次请求时进行实时训练。

### 5. 部署到Tomcat

1. 将项目编译打包为WAR文件，或直接将项目目录复制到Tomcat的webapps目录下
2. 启动Tomcat服务器
3. 访问系统：http://localhost:8080/dachuang/

## 系统功能

### 1. 首页搜索
在首页输入病种名称进行搜索，系统会推荐相关耗材。

### 2. 耗材推荐
系统基于LDA模型为指定病种推荐最相关的耗材：
1. 根据病种名称查找病种索引
2. 使用theta矩阵获取病种在各主题上的概率分布
3. 对主题按概率排序，选择TopK个主题
4. 对每个主题，使用phi矩阵找到最相关的耗材
5. 从数据库获取耗材详细信息并展示

### 3. 继续推荐
在推荐结果页面，可以点击"继续推荐"为特定耗材寻找替代品。

## LDA模型说明

### 模型参数
- 主题数量：60
- alpha参数：1.0
- beta参数：0.001
- 迭代次数：10000

### 模型矩阵
- **theta矩阵**：病案-主题分布矩阵，维度为[病案数 × 主题数]
- **phi矩阵**：主题-耗材分布矩阵，维度为[主题数 × 耗材数]

### 收敛检测
模型训练过程中会进行收敛检测，当满足以下条件时提前停止训练：
- Phi矩阵变化量 < 1e-4
- Theta矩阵变化量 < 1e-3
- 连续10次迭代满足收敛条件

## 注意事项

1. 确保数据文件位于`D:\数据\`目录下
2. 首次运行系统前必须完成数据库初始化和数据导入
3. 建议使用预训练模型以提高系统响应速度
4. 系统根据病种在各主题上的概率分布动态确定推荐主题数量
5. 系统使用UTF-8编码，确保数据文件编码一致
6. 系统启动时会自动加载预训练模型，如果未找到模型文件，将在首次请求时进行实时训练

## 故障排除

### 1. 数据库连接失败
- 检查数据库服务是否启动
- 确认数据库连接配置是否正确
- 验证用户名和密码是否正确

### 2. 数据导入失败
- 检查CSV文件是否存在且格式正确
- 确认文件编码为UTF-8
- 验证数据库表是否已创建

### 3. 模型加载失败
- 检查models目录中是否存在预训练模型文件
- 确认模型文件路径配置是否正确
- 验证模型文件是否完整

### 4. 推荐结果为空
- 检查输入的病种名称是否正确
- 确认数据是否已正确导入
- 验证模型是否正确加载

### 5. 数据文件路径错误
- 确认数据文件是否位于`D:\数据\`目录下
- 检查文件名是否正确（包括扩展名）
- 确保应用程序有权限访问该目录