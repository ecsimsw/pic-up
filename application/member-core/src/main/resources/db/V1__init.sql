create table member (
    id BIGINT(19) NOT NULL AUTO_INCREMENT,
    encrypted VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    username VARCHAR(30) NOT NULL,
    PRIMARY KEY (id)
);

create index idx_username on member (username);