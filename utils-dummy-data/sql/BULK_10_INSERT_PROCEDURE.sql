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