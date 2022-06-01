CREATE TABLE `user`(
    `id`          int          NOT NULL AUTO_INCREMENT,
    `create_at`   datetime     NOT NULL DEFAULT '0000-00-00 00:00:00',
    `updated_at`  datetime     NOT NULL DEFAULT '0000-00-00 00:00:00',
    `telphone`    varchar(40)  NOT NULL DEFAULT '',
    `password`    varchar(200) NOT NULL DEFAULT '',
    `nick_name`   varchar(40)  NOT NULL DEFAULT '',
    `gender`      int          NOT NULL DEFAULT 0,
    PRIMARY KEY(`id`),
    UNIQUE `telphone_unique_index` USING BTREE(`telphone`) COMMENT ''
)ENGINE = InnoDB
  DEFAULT CHARSET = utf8;