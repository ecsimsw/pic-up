## Utils-dummies
DB 더미테이터 csv 파일 생성

## How to insert

### Environment
- Mysql 8.0.32
- Index : PK
- Column format
```
ID, USERNAME, PASSWORD
```

### Comparison
### Single insertion loop
```
CALL BASIC_INSERT();
```
- insert 1_000 : 2.28 sec
- Insert 10_000 : 21.90 sec
- Insert 100_000 : 3 min 39.99 sec

### Bulk insertion
1. Bulk insert - 10
```
CALL BULK_INSERT_10();
```
- Insert 100_000 : 24.84 sec
- Insert 1_000_000 : 4 min 12.68 sec

2. Bulk insert - 100
```
CALL BULK_INSERT_100();
```
- Insert 100_000 : 3.36 sec
- Insert 1_000_000 : 31.96 sec

3. Bulk insert - 300
```
CALL BULK_INSERT_300();
```
- Insert 100_000 : 1.65 sec
- Insert 1_000_000 : 15.17 sec
- Insert 10_000_000 : 3 min 26.18 sec

### LOAD DATA FILE
1. check safe fie path
```
mysql> SELECT @@GLOBAL.secure_file_priv;
```

2. load data
```
LOAD DATA INFILE "/var/lib/mysql-files/member-data.txt" INTO TABLE MEMBER FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA INFILE "/var/lib/mysql-files/album-data.txt" INTO TABLE ALBUM FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
LOAD DATA INFILE "/var/lib/mysql-files/picture-data.txt" INTO TABLE PICTURE FIELDS TERMINATED BY ',' ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS;
```

3. performance
- Insert 100_000 : 1.68 sec
- Insert 1_000_000 : 12.26 sec
- Insert 10_000_000 : 3 min 13.22 sec
