create table album (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    name VARCHAR(255),
    resource_key VARCHAR(255) NOT NULL,
    user_id bigint NOT NULL,
    PRIMARY KEY (id)
)

create index idx_userId_createdAt on album (user_id, created_at)

create table picture (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    file_resource VARCHAR(50),
    file_size BIGINT(19) NOT NULL,
    thumbnail VARCHAR(50,
    album_id BIGINT(19) NOT NULL,
    PRIMARY KEY (id)
)

create index idx_albumId_createdAt_id on picture (album_id, created_at)

create table file_pre_upload_event (
    resource_key VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    file_size BIGINT(19) NOT NULL,
    PRIMARY KEY (resource_key)
)

create table file_deletion_event (
     id BIGINT(19) NOT NULL AUTO_INCREMENT,
     creation_time TIMESTAMP NOT NULL,
     delete_failed_counts INT NOT NULL,
     resource_key VARCHAR(255) NOT NULL,
     PRIMARY KEY (id)
)

create table member (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    encrypted VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    username VARCHAR(30) NOT NULL,
    PRIMARY KEY (id)
)

create table storage_usage (
    user_id BIGINT(19) NOT NULL,
    limit_as_byte BIGINT(19) NOT NULL,
    usage_as_byte BIGINT(19) NOT NULL,
    PRIMARY KEY (user_id)
)
