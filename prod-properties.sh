# This is sample in local env.
# Make this file private.

export PICUP_MASTER_DB_URL="jdbc:mysql://localhost:13306/picup?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC"
export PICUP_MASTER_DB_USERNAME="root"
export PICUP_MASTER_DB_PASSWORD="root"

export PICUP_SLAVE_DB_URL="jdbc:mysql://localhost:13306/picup?useSSL=false&characterEncoding=UTF-8&serverTimezone=UTC"
export PICUP_SLAVE_DB_PASSWORD="root"
export PICUP_SLAVE_DB_PASSWORD="root"

export PICUP_REDIS_URL="localhost"
export PICUP_REDIS_PORT="6379"

export PICUP_RABBITMQ_URL="localhost"
export PICUP_RABBITMQ_PORT="5672"
export PICUP_RABBITMQ_USERNAME="admin"
export PICUP_RABBITMQ_PASSWORD="password"

export PICUP_TOKEN_SECRET="ecsimswtemptokensecretqwertyqwerty123123123"
export PICUP_AES_KEY="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
export PICUP_AES_IV="0123456789012345"

export PICUP_STORAGE_SERVER_URL="http://localhost:8083"
export PICUP_STORAGE_SERVER_AUTH_KEY="storageServerAuthKey"
export PICUP_STORAGE_SERVER_AUTH_VALUE="storageServerAuthValue"
