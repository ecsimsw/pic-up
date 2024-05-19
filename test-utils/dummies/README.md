## Utils-dummies
DB 더미 데이터를 File upload를 위한 csv 파일을 생성한다.

### 업로드 방식 비교
- 단일 삽입 반복
```
- insert 1_000 : 2.28 sec
- Insert 10_000 : 21.90 sec
- Insert 100_000 : 3 min 39.99 sec
```
- Bulk Insert (10, 100, 300)
```
1. 10개씩 Bulk insert
- Insert 100_000 : 24.84 sec
- Insert 1_000_000 : 4 min 12.68 sec

2. 100개씩 Bulk insert
- Insert 100_000 : 3.36 sec
- Insert 1_000_000 : 31.96 sec

3. 300개씩 Bulk insert
- Insert 100_000 : 1.65 sec
- Insert 1_000_000 : 15.17 sec
- Insert 10_000_000 : 3 min 26.18 sec
```

- CSV 파일 업로드 속도 비교
```
- Insert 100_000 : 1.68 sec
- Insert 1_000_000 : 12.26 sec
- Insert 10_000_000 : 3 min 13.22 sec
```

### 단일 삽입 반복
```
DELIMITER $$
DROP PROCEDURE IF EXISTS BASIC_INSERT;
CREATE PROCEDURE BASIC_INSERT()
BEGIN
 DECLARE i INT DEFAULT 1;
 DECLARE maxIndex INT DEFAULT 100000;
 WHILE( i < maxIndex) DO
  INSERT INTO MEMBER(USERNAME, PASSWORD)
  VALUES(
   CONCAT("user-", i),
   UUID()
  );
  SET i = i+1;
END WHILE;
END $$
DELIMITER ;
```

### Bulk insertion (10, 100, 300)
```
DELIMITER $$
DROP PROCEDURE IF EXISTS BULK_INSERT_10;
CREATE PROCEDURE BULK_INSERT_10()
BEGIN
 DECLARE i INT DEFAULT 1;
 DECLARE maxIndex INT DEFAULT 100000;
 WHILE( i < maxIndex) DO
  INSERT INTO MEMBER(USERNAME, PASSWORD)
  VALUES
      (CONCAT("user-", i),UUID()),
      (CONCAT("user-", i+1),UUID()),
      (CONCAT("user-", i+2),UUID()),
      (CONCAT("user-", i+3),UUID()),
      (CONCAT("user-", i+4),UUID()),
      (CONCAT("user-", i+5),UUID()),
      (CONCAT("user-", i+6),UUID()),
      (CONCAT("user-", i+7),UUID()),
      (CONCAT("user-", i+8),UUID()),
      (CONCAT("user-", i+9),UUID());
  SET i = i+10;
END WHILE;
END $$
DELIMITER ;
```

### CSV 파일 업로드

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
