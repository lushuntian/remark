
CREATE TABLE IF NOT EXISTS consumer
(
   id INT primary key auto_increment,
   gmt_create datetime not null default CURRENT_TIMESTAMP,
   gmt_modified datetime not NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   mobile VARCHAR(20) not null unique,
   nickname VARCHAR(20),
   password VARCHAR(50),
   face VARCHAR(500),
   level TINYINT  not null default 1 ,
   email VARCHAR(100),
   extension VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS remark(
	id INT primary key auto_increment,
   	gmt_create datetime not null default CURRENT_TIMESTAMP,
  	gmt_modified datetime not NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  	consumer_id INT not null,
  	item_id varchar(50) not null,
  	order_id varchar(50) not null,
  	score TINYINT not null,
  	header VARCHAR(20) not null,
  	content VARCHAR(200) not null,
  	images VARCHAR(1000) ,
  	votes INT not null default 0,
    user_name VARCHAR(20),
    user_face VARCHAR(100),
  	status TINYINT not null default 1,
  	UNIQUE(order_id)
);