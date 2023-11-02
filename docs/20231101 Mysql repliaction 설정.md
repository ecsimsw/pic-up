## Mysql replication 설정

Mysql 서버 2대를 준비한다.

### /etc/mysql/conf.d/my.cnf

아래 설정 파일을 각 mysql 의 '/etc/mysql/conf.d/my.cnf' 에 적용한다. 

#### master
```
[mysqld]
log-bin=mysql-bin
server-id=1
```

#### slave
```
[mysqld]
log-bin=mysql-bin
server-id=2
log_slave_updates = 1
read_only = 1
```

- log-bin : log file 의 prefix 를 지정한다. 
- server-id : replication group 에서 식별될 id 이다.
- log_slave_updates : slave server 의 로그도 기록한다. 기본 값은 false 로 기록하지 않는다.
- read_only : 읽기 전용으로 한다.

### MASTER :: config 적용 확인

```
show master status\G;
```

위 명령어를 master, slave 에서 각자 입력하고 file 의 이름으로 설정한 prefix 가 적용되었는지 확인한다.

```
*************************** 1. row ***************************
             File: mysql-bin.000001
         Position: 881
     Binlog_Do_DB: 
 Binlog_Ignore_DB: 
Executed_Gtid_Set: 
1 row in set (0.00 sec)
```

### MASTER :: 권한 생성

master server 에 REPLICATION SLAVE 권한을 갖은 User 계정을 생성한다. 후에 slave 는 이 계정에 연결하여 master 에서의 사용 권한을 얻을 것이다.

```
CREATE USER 'ecsimsw'@'%' IDENTIFIED BY 'password';
GRANT REPLICATION SLAVE ON *.* TO 'ecsimsw'@'%';
```

### SLAVE :: 연결, 시작

```
CHANGE MASTER TO MASTER_HOST='${HOST_IP_ADDRESS}', MASTER_USER='${HOST_USER_TO_CONNECT}', MASTER_PASSWORD='${HOST_PASSWORD_TO_CONNECT}', MASTER_LOG_FILE='${MASTER_LOG_FILE_TO_READ}', MASTER_LOG_POS=0, GET_MASTER_PUBLIC_KEY=1;
```

```
start slave;
```

### Issue

ISSUE : start slave 시 
ERROR : `ERROR 1872 (HY000): Replica failed to initialize applier metadata structure from the repository`
SOL :
SLAVE DB에서
```
1. stop slave;
2. reset slave;
3. start slave;
```
그래도 안된다면 slave 연결 `CHANGE MASTER TO ~` 부분 재실행 

### 테스트 

#### 1. slave 에서 slave status 확인

```
show slave status\G;
```

```
mysql> show slave status\G;
*************************** 1. row ***************************
               Slave_IO_State: 
                  Master_Host: 172.18.0.4
                  Master_User: ecsimsw
                  Master_Port: 3306
                Connect_Retry: 60
              Master_Log_File: mysql-bin.000001
          Read_Master_Log_Pos: 4
               Relay_Log_File: cc07ad22b227-relay-bin.000001
                Relay_Log_Pos: 4
        Relay_Master_Log_File: mysql-bin.000001
             Slave_IO_Running: No
            Slave_SQL_Running: No
              Replicate_Do_DB: 
          Replicate_Ignore_DB: 
           Replicate_Do_Table: 
       Replicate_Ignore_Table: 
      Replicate_Wild_Do_Table: 
  Replicate_Wild_Ignore_Table: 
                   Last_Errno: 0
                   Last_Error: 
                 Skip_Counter: 0
          Exec_Master_Log_Pos: 4
              Relay_Log_Space: 157
              Until_Condition: None
               Until_Log_File: 
                Until_Log_Pos: 0
           Master_SSL_Allowed: No
           Master_SSL_CA_File: 
           Master_SSL_CA_Path: 
              Master_SSL_Cert: 
            Master_SSL_Cipher: 
               Master_SSL_Key: 
        Seconds_Behind_Master: NULL
Master_SSL_Verify_Server_Cert: No
                Last_IO_Errno: 0
                Last_IO_Error: 
               Last_SQL_Errno: 0
               Last_SQL_Error: 
  Replicate_Ignore_Server_Ids: 
             Master_Server_Id: 0
                  Master_UUID: 
             Master_Info_File: mysql.slave_master_info
                    SQL_Delay: 0
          SQL_Remaining_Delay: NULL
      Slave_SQL_Running_State: 
           Master_Retry_Count: 86400
                  Master_Bind: 
      Last_IO_Error_Timestamp: 
     Last_SQL_Error_Timestamp: 
               Master_SSL_Crl: 
           Master_SSL_Crlpath: 
           Retrieved_Gtid_Set: 
            Executed_Gtid_Set: 
                Auto_Position: 0
         Replicate_Rewrite_DB: 
                 Channel_Name: 
           Master_TLS_Version: 
       Master_public_key_path: 
        Get_master_public_key: 1
            Network_Namespace: 
1 row in set, 1 warning (0.00 sec)
```

#### 2. master 에서 작업 후 slave 에서 동기화 확인 
```
ex) create database test_db;
```
