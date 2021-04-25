CREATE TABLE IF NOT EXISTS vote_document
(
   id INT primary key auto_increment,
   gmt_create datetime not null default CURRENT_TIMESTAMP,
   voter_id INT not null,
   content_id INT not null,
   voting TINYINT not null,
   votes INT not null,
   create_date INT not null
);
