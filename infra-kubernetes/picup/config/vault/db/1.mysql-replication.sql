/*
master
 */

show master status\G;

CREATE USER 'replica'@'%' IDENTIFIED BY 'password';
GRANT REPLICATION SLAVE ON *.* TO 'replica'@'%';

/*
slave
 */

CHANGE MASTER TO MASTER_HOST='${HOST_IP_ADDRESS}',\
                 MASTER_USER='replica', \
                 MASTER_PASSWORD='password', \
                 MASTER_LOG_FILE='${MASTER_LOG_FILE_TO_READ}', \
                 MASTER_LOG_POS=0, GET_MASTER_PUBLIC_KEY=1;
start slave;

show slave status\G;

/*
mysql-album-master-bin.000003   172.18.0.5
mysql-album-slave-bin.000003  172.18.0.2
mysql-member-bin.000003    172.18.0.8
 */


