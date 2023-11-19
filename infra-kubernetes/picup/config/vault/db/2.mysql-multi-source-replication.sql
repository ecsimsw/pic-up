/*
 MASTER
 */
SHOW MASTER STATUS\G

CREATE USER 'bakup'@'%' IDENTIFIED BY 'password';
GRANT REPLICATION SLAVE ON *.* TO 'bakup'@'%';

/*
SLAVE
 */

CHANGE REPLICATION SOURCE TO SOURCE_HOST="${SERVER_URL}", \
                             SOURCE_PORT=${SERVER_PORT}, \
                             SOURCE_USER="${REPLICA_USER_NAME}", \
                             SOURCE_PASSWORD="${REPLICA_USER_PASSWORD}", \
                             SOURCE_LOG_FILE="${BIN_LOG_FILE_NAME}", \
                             SOURCE_LOG_POS=${BIN_FILE_POSITION} \
                             FOR CHANNEL "${CHANNEL_NAME}";



START REPLICA FOR CHANNEL "{CHANNEL_NAME}";
START REPLICA;

show slave status\G;

STOP REPLICA FOR CHANNEL "{CHANNEL_NAME}";
STOP REPLICA;
RESET REPLICA ALL;

/*
example
 */

mysql-album-slave-bin.000003  172.18.0.2 3449
mysql-member-bin.000003    172.18.0.8 1906

CHANGE REPLICATION SOURCE TO SOURCE_HOST="172.18.0.2", \
                             SOURCE_PORT=3306, \
                             SOURCE_USER="bakup", \
                             SOURCE_PASSWORD="password", \
                             SOURCE_LOG_FILE="mysql-album-slave-bin.000003", \
                             SOURCE_LOG_POS=3449 \
                             FOR CHANNEL "album-slave";

CHANGE REPLICATION SOURCE TO SOURCE_HOST="172.18.0.8", \
                             SOURCE_PORT=3306, \
                             SOURCE_USER="bakup", \
                             SOURCE_PASSWORD="password", \
                             SOURCE_LOG_FILE="mysql-member-bin.000003", \
                             SOURCE_LOG_POS=1906 \
                             FOR CHANNEL "member";

START REPLICA FOR CHANNEL "album-slave";
START REPLICA FOR CHANNEL "member";
START REPLICA;