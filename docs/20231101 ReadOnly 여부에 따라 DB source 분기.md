# 20231101 ReadOnly 여부에 따라 DB source 분기

DB replication 을 처리하고 MASTER, SLAVE DB 를 transaction readOnly 옵션에 따라 DB source 를 분기하여 읽기는 SLAVE 에서, 쓰기는 MASTER 에서 처리될 수 있도록 할 생각이다.

## 1. DB source 를 정의한다.

연결할 DB 정보를 설정한다.

```
spring.datasource.master.username={USER_NAME}
spring.datasource.master.password={PASSWORD}
spring.datasource.master.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.master.jdbc-url= jdbc:mysql://{SOURCE_URL}/{DB}

spring.datasource.slave.username={USER_NAME}
spring.datasource.slave.password={PASSWORD}
spring.datasource.slave.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.slave.jdbc-url= jdbc:mysql://{SOURCE_URL}/{DB}
```

``` java
@Bean(value = DB_SOURCE_BEAN_ALIAS_MASTER)
@ConfigurationProperties(prefix = "spring.datasource.master")
public DataSource masterDataSource() {
    return DataSourceBuilder.create().build();
}

@Bean(value = DB_SOURCE_BEAN_ALIAS_SLAVE)
@ConfigurationProperties(prefix = "spring.datasource.slave")
public DataSource slaveDataSource() {
    return DataSourceBuilder.create().build();
}
```

## 2. DB 타입을 정의하고 ReadOnly 여부에 따라 사용할 DB source 를 반환한다.

determineCurrentLookupKey() 를 재정의하여 쿼리를 전송할 DB source 를 나타낼 lookup key 를 반환할 수 있다.
TransactionSynchronizationManager.isCurrentTransactionReadOnly() 를 사용하면 현재 transaction 의 readOnly 옵션 여부를 확인할 수 있다.

transaction 이 readOnly 면 SLAVE type 을 키로, 그렇지 않으면 MASTER type 을 키로 반환할 것이다.

```
enum DBType {
    MASTER,
    SLAVE
}
```

```
class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        var isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        if (isReadOnly) {
            return SLAVE;
        }
        return MASTER;
    }
}
```

## 3. RoutingDataSource 를 정의하여 DB type 과 DB source 를 mapping 한다.

lookup 키로 반환 받은 DB type 만으론 어떤 type 에 어떤 DB source 가 매핑 되었는지 알 수 없다.
RoutingDataSource 를 정의하고 { DB type : DB source } 를 mapping 하여 type 별 target DB source 를 지정한다.

```
@Bean
public DataSource routingDataSource(
    @Qualifier(DB_SOURCE_BEAN_ALIAS_MASTER) DataSource masterDataSource,
    @Qualifier(DB_SOURCE_BEAN_ALIAS_SLAVE) DataSource slaveDataSource
) {
    var routingDataSource = new RoutingDataSource();
    routingDataSource.setDefaultTargetDataSource(masterDataSource);
    routingDataSource.setTargetDataSources(
        Map.of(
            MASTER, masterDataSource,
            SLAVE, slaveDataSource
        )
    );
    return routingDataSource;
}
```

## 4. LazyConnectionDataSourceProxy 를 앞서 설정한 routingDataSource 와 함께 생성한다.

Spring default 로 Transactional 이 시작되는 시점부터 DataSource 를 미리 정해둔다.
지금 상황처럼 Master, Slave 로 나누거나, 쿼리 동작에 따라 DB1, DB2 로 나누는 등 transaction 진입 이후에 DB source 분기가 필요한 상황에서
이미 transaction 이 시작된 상황에서 DB source 가 결정 되었기에 이후 분기가 불가능하다.

LazyConnectionDataSourceProxy 를 사용하여 DB source 결정을 transaction 진입 이후로 미뤄 쿼리가 실행될 때 DB source 를 결정할 수 있도록 한다.

```
@Bean
@Primary
public DataSource dataSource() {
    var determinedDataSource = routingDataSource(
        masterDataSource(),
        slaveDataSource()
    );
    return new LazyConnectionDataSourceProxy(determinedDataSource);
}
```

