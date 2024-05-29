create table member_event (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL,
    user_id BIGINT(19) NOT NULL,
    limit_as_bytes BIGINT(19) NOT NULL,
    type VARCHAR(128) NOT NULL,
    primary key (id)
);