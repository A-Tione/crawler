CREATE TABLE news (
    id bigint PRIMARY KEY AUTO_INCREMENT,
    title text,
    content text,
    url varchar(1000),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp DEFAULT CURRENT_TIMESTAMP
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE LINKS_TO_BE_PROCESSED (link varchar(3000));
CREATE TABLE LINKS_ALREADY_PROCESSED (link varchar(3000));
