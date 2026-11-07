package com.atguigu.hospital.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MybatisPlus配置类
 */
@EnableTransactionManagement
@Configuration
@MapperScan("com.atguigu.hospital.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 分页插件配置（3.4.x 新版本）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置最大单页限制数量，默认 500 条，小于 0 如 -1 不受限制
        paginationInterceptor.setMaxLimit(500L);
        // 溢出总页数后是否进行处理（默认不处理）
        paginationInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }

    /**
     * SQL执行效率插件
     */
    /*
    @Bean
    @Profile({"dev","test"})// 设置 dev test 环境开启
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        performanceInterceptor.setMaxTime(2000);
        performanceInterceptor.setFormat(true);
        return performanceInterceptor;
    }*/
}
