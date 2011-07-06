# --- !Ups
 
CREATE TABLE Comment (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    author varchar(255) NOT NULL,
    content text NOT NULL,
    postedAt date NOT NULL,
    post_id bigint(20) NOT NULL,
    FOREIGN KEY (post_id) REFERENCES Post(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);
 
# --- !Downs
 
DROP TABLE Comment;