package com.healthtrail.infrastructure.mybatisplus;

import com.healthtrail.common.core.base.BaseController;
import com.healthtrail.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig.Builder;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.builder.Entity;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.PostgreSqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.fill.Property;
import com.baomidou.mybatisplus.generator.keywords.PostgreSqlKeyWordsHandler;
import java.util.Collections;
import lombok.Data;

/**
 * MyBatis-Plus代码生成器，根据数据库表自动生成Entity/Mapper/Service/Controller代码。
 * @author valarchie
 */
@Data
@lombok.Builder
public class CodeGenerator {

    /** 代码作者 */
    private String author;
    /** 输出模块路径 */
    private String module;
    /** 目标表名 */
    private String tableName;
    /** 数据库连接URL */
    private String databaseUrl;
    /** 数据库用户名 */
    private String username;
    /** 数据库密码 */
    private String password;
    /** 父包名 */
    private String parentPackage;
    /** 是否继承BaseEntity */
    private Boolean isExtendsFromBaseEntity;

    /**
     * 为了避免直接覆盖人工维护中的业务代码，这里默认把生成结果输出到目标模块自己的
     * `target/generated-code` 目录下，开发人员确认无误后再按需手动迁移到正式源码目录。
     */
    public static void main(String[] args) {
        // 默认给出当前项目新的 PostgreSQL 示例连接，方便直接对照本地 healthtrail 库生成代码。
        String databaseUrl = "jdbc:postgresql://localhost:5432/healthtrail";
        String username = "postgres";
        String password = "12345";

        CodeGenerator generator = CodeGenerator.builder()
            .databaseUrl(databaseUrl)
            .username(username)
            .password(password)
            .author("valarchie")
            // 当前仓库没有独立 orm 模块，默认先把生成结果落到 healthtrail-domain 的 target/generated-code 下。
            .module("/healthtrail-domain/target/generated-code")
            .parentPackage("com.healthtrail")
            .tableName("sys_menu")
            // 决定是否继承基类
            .isExtendsFromBaseEntity(true)
            .build();

        generator.generateCode();
    }

    public void generateCode() {
        FastAutoGenerator generator = FastAutoGenerator.create(
            new Builder(databaseUrl, username, password)
//            .schema("mybatis-plus")
                // all these three options
                .dbQuery(new PostgreSqlQuery())
                .typeConvert(new PostgreSqlTypeConvert())
                .keyWordsHandler(new PostgreSqlKeyWordsHandler()));

        globalConfig(generator);
        packageConfig(generator);
//        templateConfig(generator);
        injectionConfig(generator);
        strategyConfig(generator);
        // 默认的是Velocity引擎模板
        generator.templateEngine(new VelocityTemplateEngine());
        generator.execute();
    }


    /**
     * 为了避免  覆盖掉service中的方法
     * @param generator 生成器
     */
    private void globalConfig(FastAutoGenerator generator) {
        generator.globalConfig(
            builder -> builder
                // override old code of file
                .fileOverride()
                .outputDir(System.getProperty("user.dir") + module + "/src/main/java")
                // use date type under package of java utils
                .dateType(DateType.ONLY_DATE)
                // 配置生成文件中的author
                .author(author)
//                    .enableKotlin()
                // generate swagger annotations.
                .enableSwagger()
                // 注释日期的格式
                .commentDate("yyyy-MM-dd")
                .build());
    }


    private void packageConfig(FastAutoGenerator generator) {
        generator.packageConfig(builder -> builder
            // parent package name
            .parent(parentPackage)
            .moduleName("orm")
            .entity("entity")
            .service("service")
            .serviceImpl("service.impl")
            .mapper("mapper")
            .xml("mapper.xml")
            .controller("controller")
            .other("other")
            // define dir related to OutputFileType(entity,mapper,service,controller,mapper.xml)
            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, System.getProperty("user.dir") + module
                + "/src/main/resources/mapper/system/test"))
            .build());
    }

    private void templateConfig(FastAutoGenerator generator) {
        //  customization code template. disable if you don't have specific requirement.
        generator.templateConfig(builder -> builder
            .disable(TemplateType.ENTITY)
            .entity("/templates/entity.java")
            .service("/templates/service.java")
            .serviceImpl("/templates/serviceImpl.java")
            .mapper("/templates/mapper.java")
            .mapperXml("/templates/mapper.xml")
            .controller("/templates/controller.java")
            .build());
    }

    private void injectionConfig(FastAutoGenerator generator) {
        //  customization code template. disable if you don't have specific requirement.
        generator.injectionConfig(builder -> {
            // Customization
            builder.beforeOutputFile((tableInfo, objectMap) -> System.out.println("tableInfo: " +
                    tableInfo.getEntityName() + " objectMap: " + objectMap.size()))
//                .customMap(Collections.singletonMap("test", "baomidou"))
//                .customFile(Collections.singletonMap("test.txt", "/templates/test.vm"))
                .build();
        });
    }


    private void strategyConfig(FastAutoGenerator generator) {
        //  customization code template. disable if you don't have specific requirement.
        generator.strategyConfig(builder -> {
            builder
                // Global Configuration
                .enableCapitalMode()
                // does not generate view
                .enableSkipView()
                .disableSqlFilter()
                // filter which tables need to be generated
//                    .likeTable(new LikeTable("USER"))
//                    .addInclude("t_simple")
//                    .addTablePrefix("t_", "c_")
//                    .addFieldSuffix("_flag")
                .addInclude(tableName);

            entityConfig(builder);
            controllerConfig(builder);
            serviceConfig(builder);
            mapperConfig(builder);
        });
    }


    private void entityConfig(StrategyConfig.Builder builder) {
        Entity.Builder entityBuilder = builder.entityBuilder();

        entityBuilder
//                    .superClass(BaseEntity.class)
//                    .disableSerialVersionUID()
//                    .enableChainModel()
            .enableLombok()
            // boolean field
//                    .enableRemoveIsPrefix()
            .enableTableFieldAnnotation()
            // operate entity like JPA.
            .enableActiveRecord()
//                    .versionColumnName("version")
//                    .versionPropertyName("version")
            // deleted的字段设置成tinyint  长度为1
            .logicDeleteColumnName("deleted")
//                    .logicDeletePropertyName("deleteFlag")
            .naming(NamingStrategy.underline_to_camel)
            .columnNaming(NamingStrategy.underline_to_camel)
            // 如果不需要BaseEntity  请注释掉以下两行
//            .superClass(BaseEntity.class)
//            .addSuperEntityColumns("creator_id", "create_time", "creator_name", "updater_id", "update_time", "updater_name", "deleted")
//                    .addIgnoreColumns("age")
            // 两种配置方式 都可以
            .addTableFills(new Column("create_time", FieldFill.INSERT))
            .addTableFills(new Column("creator_id", FieldFill.INSERT))
            .addTableFills(new Property("updateTime", FieldFill.INSERT_UPDATE))
            .addTableFills(new Property("updaterId", FieldFill.INSERT_UPDATE))
            // ID strategy AUTO, NONE, INPUT, ASSIGN_ID, ASSIGN_UUID;
            .idType(IdType.AUTO)
            .formatFileName("%sEntity");

        if (isExtendsFromBaseEntity) {
            entityBuilder
                .superClass(BaseEntity.class)
                .addSuperEntityColumns("creator_id", "create_time", "creator_name", "updater_id", "update_time",
                    "updater_name", "deleted");
        }

        entityBuilder.build();
    }


    private void controllerConfig(StrategyConfig.Builder builder) {
        builder.controllerBuilder()
            .superClass(BaseController.class)
            .enableHyphenStyle()
            .enableRestStyle()
            .formatFileName("%sController")
            .build();
    }

    private void serviceConfig(StrategyConfig.Builder builder) {
        builder.serviceBuilder()
//                    .superServiceClass(BaseService.class)
//                    .superServiceImplClass(BaseServiceImpl.class)
            .formatServiceFileName("%sService")
            .formatServiceImplFileName("%sServiceImpl")
            .build();
    }

    private void mapperConfig(StrategyConfig.Builder builder) {
        builder.mapperBuilder()
//                    .superClass(BaseMapper.class)
//                    .enableMapperAnnotation()
//                    .enableBaseResultMap()
//                    .enableBaseColumnList()
//                    .cache(MyMapperCache.class)
            .formatMapperFileName("%sMapper")
            .formatXmlFileName("%sMapper")
            .build();
    }


}
