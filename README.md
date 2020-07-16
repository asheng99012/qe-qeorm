## 添加引用
```xml
        <dependency>
            <groupId>com.danke.arch</groupId>
            <artifactId>dk-common-qeorm</artifactId>
            <version>2.0.0-SNAPSHOT</version>
        </dependency>
```
> 如果想用 qeorm-mongo ，还需要添加以下依赖
```xml
        <dependency>
            <groupId>com.danke.arch</groupId>
            <artifactId>dk-common-qeorm-mongodb</artifactId>
            <version>2.0.0-SNAPSHOT</version>
        </dependency>   

```

## 在启动类上添加
``` java
@Import({Config.class, QeormConfig.class})
```

## 在配置文件中添加配置
``` yaml
qeorm:
  mapper:
    basePackage: com.danke.rundata
    mapperLocations: classpath*:qeorm/**/*.xml
  datasource:
    defaultDataSource: default
    dataSourcesMap:
      defaultConfig:
        class: com.alibaba.druid.pool.DruidDataSource
        driverClassName: com.mysql.jdbc.Driver
        maxActive: 100
        initialSize: 5
        maxWait: 6000
        minIdle: 1
        timeBetweenEvictionRunsMillis: 3000
        minEvictableIdleTimeMillis: 300000
        validationQuery: select 1
        testWhileIdle: true
        testOnBorrow: false
        testOnReturn: true
        poolPreparedStatements: false
        maxPoolPreparedStatementPerConnectionSize: 20
        removeAbandoned: true
        removeAbandonedTimeout: 1800
        logAbandoned: true
      laputa:
        url: jdbc:mysql://172.21.32.5:3306/Laputa?useUnicode=true&characterEncoding=utf-8
        username: 23423
        password: 234234
        
      mongodb:
        class: com.github.vincentrussell.query.mongodb.sql.converter.jdbc.MongodbDataSource
        url: mongodb://172.18.130.50:27017
        username: 234
        password: 1234566
        database: installment
        maxPoolSize: 10
        waitQueueMultiple: 100
        safe: true
        connectTimeout: 10000
        serverSelectionTimeout: 30000
        readPreference: secondaryPreferred
        authMechanism: SCRAM-SHA-1
        

```
