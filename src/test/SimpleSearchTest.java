package test;

import org.junit.Test;
import org.junit.Before;
import java.util.ArrayList;
import javabean.Disease;
import javabean.Supply;
import dbhelper.DBSearch;

/**
 * 简单的搜索功能单元测试
 */
public class SimpleSearchTest {
    
    private DBSearch dbSearch;
    
    @Before
    public void setUp() {
        dbSearch = new DBSearch();
    }
    
    @Test
    public void testGetDiseaseSupplyRelation() {
        // 测试获取疾病相关耗材功能
        // 使用一个假定存在的疾病ID进行测试（ID为1）
        ArrayList<Supply> supplies = dbSearch.getDiseaseSupplyRelation(1);
        
        // 验证返回结果不为null
        if (supplies == null) {
            throw new AssertionError("耗材关联结果不应为null");
        }
        
        // 验证返回的是ArrayList类型
        if (!(supplies instanceof ArrayList)) {
            throw new AssertionError("耗材关联结果应为ArrayList类型");
        }
        
        System.out.println("疾病ID为1的关联耗材数量: " + supplies.size());
        
        // 额外测试：验证列表中的元素也是Supply类型
        for (Supply supply : supplies) {
            if (!(supply instanceof Supply)) {
                throw new AssertionError("列表中元素应为Supply类型");
            }
        }
    }
    
    @Test
    public void testDiseaseBean() {
        // 测试Disease bean的基本功能
        Disease disease = new Disease();
        disease.setID(1);
        disease.setNAME("测试疾病");
        disease.setDESCRIPTION("测试疾病描述");
        
        if (disease.getID() != 1) {
            throw new AssertionError("疾病ID应为1");
        }
        
        if (!"测试疾病".equals(disease.getNAME())) {
            throw new AssertionError("疾病名称应为'测试疾病'");
        }
        
        if (!"测试疾病描述".equals(disease.getDESCRIPTION())) {
            throw new AssertionError("疾病描述应为'测试疾病描述'");
        }
    }
    
    @Test
    public void testSupplyBean() {
        // 测试Supply bean的基本功能
        Supply supply = new Supply();
        supply.setID(1);
        supply.setNAME("测试耗材");
        supply.setPRODUCT_NAME("测试产品");
        supply.setPRICE("100.0");
        
        if (supply.getID() != 1) {
            throw new AssertionError("耗材ID应为1");
        }
        
        if (!"测试耗材".equals(supply.getNAME())) {
            throw new AssertionError("耗材名称应为'测试耗材'");
        }
        
        if (!"测试产品".equals(supply.getPRODUCT_NAME())) {
            throw new AssertionError("产品名称应为'测试产品'");
        }
        
        if (!"100.0".equals(supply.getPRICE())) {
            throw new AssertionError("价格应为'100.0'");
        }
    }
    
    @Test
    public void testEmptyDiseaseSupplyRelation() {
        // 测试传入不存在的疾病ID的情况
        ArrayList<Supply> supplies = dbSearch.getDiseaseSupplyRelation(-1);
        
        // 即使ID不存在，也应该返回一个空列表而不是null
        if (supplies == null) {
            throw new AssertionError("耗材关联结果不应为null，即使疾病ID不存在");
        }
        
        if (!(supplies instanceof ArrayList)) {
            throw new AssertionError("耗材关联结果应为ArrayList类型");
        }
        
        System.out.println("不存在疾病ID的关联耗材数量: " + supplies.size());
    }
}