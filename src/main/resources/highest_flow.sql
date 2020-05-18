-- Project Name : 高级引流
-- Date/Time    : 2020/5/18 20:11:14
-- Author       : KKK
-- RDBMS Type   : MySQL
-- Application  : A5:SQL Mk-2

-- Token列表
create table tbl_user_token (
  id INT not null AUTO_INCREMENT comment 'ID'
  , tbl_user_id INT not null comment '用户ID'
  , token VARCHAR(32) comment 'Token'
  , expire_time DATETIME comment '过期时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_user_token_PKC primary key (id)
) comment 'Token列表' ;

-- 系统参数
create table sys_config (
  id INT not null AUTO_INCREMENT comment 'ID'
  , param_key VARCHAR(32) comment '参数'
  , param_value VARCHAR(256) comment '内容'
  , constraint sys_config_PKC primary key (id)
) comment '系统参数' ;

-- 操作记录
create table tbl_logs (
  id INT not null AUTO_INCREMENT comment 'ID'
  , tbl_user_id INT not null comment '用户ID'
  , msg VARCHAR(256) comment '内容'
  , created_time DATETIME comment '创建时间'
  , constraint tbl_logs_PKC primary key (id)
) comment '操作记录' ;

-- 商品列表
create table tbl_products (
  id INT not null AUTO_INCREMENT comment 'ID'
  , category_id INT comment '类目'
  , category_title VARCHAR(48) comment '类目名称'
  , product_id VARCHAR(32) comment '商品ID'
  , title VARCHAR(256) comment '商品名称'
  , price VARCHAR(16) comment '商品价格'
  , month_sales INT comment '月销量'
  , shop_name VARCHAR(32) comment '店铺名称'
  , url VARCHAR(256) comment '地址'
  , picurl VARCHAR(256) comment '主图片'
  , state TINYINT comment '状态'
  , remark VARCHAR(256) comment '备注'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_products_PKC primary key (id)
) comment '商品列表' ;

-- 直播间列表
create table tbl_liveroom (
  id INT not null AUTO_INCREMENT comment 'ID'
  , tbl_user_id INT not null comment '用户id'
  , taobao_user_id VARCHAR(20) not null comment '淘宝用户id'
  , room_title VARCHAR(32) comment '直播间名称'
  , cover_img VARCHAR(256) comment '主封面图'
  , cover_img169 VARCHAR(256) comment '附封面图'
  , title VARCHAR(32) comment '标题'
  , intro VARCHAR(256) comment '简介'
  , start_time DATETIME comment '开播时间'
  , end_time DATETIME comment '结束时间'
  , channel_id TINYINT comment '直播频道'
  , column_id TINYINT comment '直播标签'
  , first_product_url VARCHAR(256) comment '热推商品链接'
  , state TINYINT comment '状态:0：正常，1：删除，2：停止'
  , keywords TEXT comment '主关键词:用;来区分'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_liveroom_PKC primary key (id)
) comment '直播间列表' ;

-- 淘宝小号
create table tbl_accounts (
  id INT not null AUTO_INCREMENT comment 'ID'
  , account_id VARCHAR(20) comment '用户名'
  , sid VARCHAR(48) comment 'SID'
  , utdid VARCHAR(32) comment 'Utdid'
  , devid VARCHAR(48) comment 'Devid'
  , auto_login_token TEXT comment 'AutoLoginToken'
  , cookie TEXT comment 'Cookie'
  , expires INT comment '过期时间'
  , state TINYINT comment '状态:0：正常，1：重登失败，2：延期失败'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_accounts_PKC primary key (id)
) comment '淘宝小号' ;

-- 用户
create table tbl_user (
  id INT not null AUTO_INCREMENT comment 'id'
  , user_id VARCHAR(20) not null comment '用户名'
  , password VARCHAR(50) comment '密码'
  , machine_code VARCHAR(32) comment '机器号'
  , mobile VARCHAR(20) comment '手机号'
  , weixin VARCHAR(20) comment '微信号'
  , level TINYINT comment '用户等级:99：高级管理员，0：普通会员'
  , service_type TINYINT comment '服务类型:0：热度，1：高级引流，3：真人注入'
  , state TINYINT comment '状态:0: 正常，1: 删除，2：过期'
  , service_start_time DATETIME comment '服务开始时间'
  , service_end_time DATETIME comment '服务结束时间'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_user_PKC primary key (id)
) comment '用户' ;
