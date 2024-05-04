create table album (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    name VARCHAR(128),
    resource_key VARCHAR(128) NOT NULL,
    user_id BIGINT(19) NOT NULL,
    PRIMARY KEY (id)
)

create table file_deletion_failed_history (
    created_at TIMESTAMP NOT NULL,
    resource_key varchar(128) NOT NULL,
    storage_type varchar(255),
    primary key (created_at)
)

create table member (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    encrypted VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    username VARCHAR(30) NOT NULL,
    PRIMARY KEY (id)
)

create table picture (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    resource_key VARCHAR(50),
    file_size BIGINT(19) NOT NULL,
    has_thumbnail BOOLEAN,
    album_id BIGINT(19) NOT NULL,
    PRIMARY KEY (id)
)

create table storage_resource (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    delete_failed_count INT NOT NULL,
    file_size BIGINT(19) NOT NULL,
    resource_key VARCHAR(128) NOT NULL,
    storage_type VARCHAR(30),
    to_be_deleted BOOLEAN NOT NULL,
    primary key (id)
)

create table storage_usage (
    user_id BIGINT(19) NOT NULL,
    limit_as_byte BIGINT(19) NOT NULL,
    usage_as_byte BIGINT(19) NOT NULL,
    PRIMARY KEY (user_id)
)

create index idx_userId_createdAt on album (user_id, created_at)
create index idx_username on member (username)
create index idx_albumId_createdAt_id on picture (album_id, created_at)
create index idx_resource_key on storage_resource (resource_key)