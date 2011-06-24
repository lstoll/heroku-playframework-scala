# Initial schema

# --- !Ups

CREATE TABLE contact (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    name varchar(255) NOT NULL,
    firstname varchar(255) NOT NULL,
    birthdate datetime NOT NULL,
    email varchar(255),
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE contact;