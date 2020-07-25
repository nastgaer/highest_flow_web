-- Project Name : 高级引流
-- Date/Time    : 2020/7/26 4:11:07
-- Author       : KKK
-- RDBMS Type   : MySQL
-- Application  : A5:SQL Mk-2

-- 系统记录
create table sys_log (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_name VARCHAR(20) comment '会员名'
  , operation VARCHAR(32) comment '操作'
  , method VARCHAR(32) comment '请求方法'
  , params TEXT comment '请求参数'
  , result TEXT comment '返回内容'
  , duration INT comment '执行时长(毫秒)'
  , ip VARCHAR(24) comment 'IP地址'
  , created_time DATETIME comment '创建时间'
  , constraint sys_log_PKC primary key (id)
) comment '系统记录' ;

-- 淘宝账号延期记录
create table tbl_accounts_logs (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_id INT comment '会员ID'
  , kind TINYINT comment '类型:0：重登，1：延期，2：新增，3：删除'
  , uid VARCHAR(32) comment '账号uid'
  , nick VARCHAR(32) comment '账号昵称'
  , success TINYINT comment '是否成功:1：成功，0：失败'
  , expires DATETIME comment '过期时间'
  , content TEXT comment '内容'
  , created_time DATETIME comment '创建时间'
  , constraint tbl_accounts_logs_PKC primary key (id)
) comment '淘宝账号延期记录' ;

-- 会员
create table tbl_members (
  id INT AUTO_INCREMENT comment 'id'
  , member_name VARCHAR(20) not null comment '用户名'
  , password VARCHAR(72) comment '密码'
  , level TINYINT comment '会员等级:0：普通会员，999：管理员'
  , salt VARCHAR(48) comment 'Salt'
  , mobile VARCHAR(20) comment '手机号'
  , comment TEXT comment '备注'
  , state TINYINT comment '状态:0: 正常，1: 禁用'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_members_PKC primary key (id)
) comment '会员' ;

-- 淘宝小号
create table tbl_accounts (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_id INT not null comment '添加的会员名'
  , nick VARCHAR(32) comment '会员名'
  , sid VARCHAR(48) comment 'SID'
  , uid VARCHAR(32) not null comment 'UserID'
  , utdid VARCHAR(32) comment 'Utdid'
  , devid VARCHAR(48) comment 'Devid'
  , auto_login_token TEXT comment 'AutoLoginToken'
  , umid_token VARCHAR(48) comment 'UmidToken'
  , cookie TEXT comment 'Cookie'
  , expires DATETIME comment '过期时间'
  , state TINYINT comment '状态:0：正常，1：过期，2：失败'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_accounts_PKC primary key (id)
) comment '淘宝小号' ;

-- 直播间列表
create table tbl_liveroom_history (
  id INT not null AUTO_INCREMENT comment 'ID'
  , taobao_account_nick VARCHAR(20) comment '淘宝用户昵称'
  , live_room_name VARCHAR(32) comment '直播间名称'
  , live_id VARCHAR(32) comment '直播间ID'
  , live_kind TINYINT comment '预告类型:0: 正式预告，1：高级预告'
  , live_cover_img VARCHAR(256) comment '主封面图'
  , live_cover_img169 VARCHAR(256) comment '附封面图'
  , live_title VARCHAR(32) comment '标题'
  , live_intro VARCHAR(256) comment '简介'
  , live_appointment_time TIME comment '开播时间'
  , live_started_time DATETIME comment '开播的时间'
  , live_end_time DATETIME comment '结束时间'
  , live_channel_id INT(4) comment '直播频道'
  , live_column_id INT(4) comment '直播标签'
  , live_location VARCHAR(32) comment '地点'
  , hot_product_url VARCHAR(256) comment '热推商品链接'
  , psc_channel_id INT(4) comment '商品大分类'
  , psc_category_id INT(4) comment '商品小分类'
  , psc_start_price INT(4) comment '商品价格范围'
  , psc_end_price INT(4) comment '商品价格范围'
  , psc_min_sales INT(4) comment '商品最小销量'
  , psc_product_count INT(4) comment '商品数'
  , psc_is_tmall BOOLEAN comment '是否天猫'
  , psc_sort_kind TINYINT comment '商品采集排序'
  , live_state TINYINT comment '直播间状态'
  , created_time DATETIME comment '创建使劲'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_liveroom_history_PKC primary key (id)
) comment '直播间列表' ;

-- 商品列表
create table tbl_liveroom_products (
  id INT not null AUTO_INCREMENT comment 'ID'
  , live_id VARCHAR(32) not null comment '直播间ID'
  , category_id VARCHAR(32) comment '类目'
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
  , constraint tbl_liveroom_products_PKC primary key (id)
) comment '商品列表' ;

-- 操作记录
create table tbl_logs (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_id INT not null comment '用户名'
  , category VARCHAR(32) comment '分类'
  , msg TEXT comment '内容'
  , created_time DATETIME comment '创建时间'
  , constraint tbl_logs_PKC primary key (id)
) comment '操作记录' ;

-- 系统参数
create table sys_config (
  id INT not null AUTO_INCREMENT comment 'ID'
  , param_key VARCHAR(32) comment '参数'
  , param_value VARCHAR(256) comment '内容'
  , constraint sys_config_PKC primary key (id)
) comment '系统参数' ;

-- Token列表
create table tbl_member_tokens (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_id INT not null comment '会员ID'
  , token VARCHAR(32) comment 'Token'
  , expire_time DATETIME comment '过期时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_member_tokens_PKC primary key (id)
) comment 'Token列表' ;

-- 用户卡密
create table tbl_codes (
  id INT not null AUTO_INCREMENT comment 'ID'
  , code_type TINYINT comment '卡密类型:0：测试，1：正式'
  , service_type TINYINT comment '服务类型:0：刷热度，1：淘宝直播'
  , hours TINYINT comment '授权时间'
  , code VARCHAR(72) comment '卡密'
  , state TINYINT comment '状态:0: 创建，1: 验证，2: 绑定'
  , machine_code VARCHAR(32) comment '机器码'
  , member_id INT comment '会员ID'
  , service_start_time DATETIME comment '服务开始时间'
  , service_end_time DATETIME comment '服务结束时间'
  , created_time DATETIME comment '创建时间'
  , accepted_time DATETIME comment '验证时间'
  , constraint tbl_codes_PKC primary key (id,code)
) comment '用户卡密' ;

-- 日程表
create table schedule_job (
  id INT not null AUTO_INCREMENT comment 'ID'
  , bean_name VARCHAR(200) comment 'BEAN名称'
  , params TEXT comment '参数'
  , cron_expression VARCHAR(32) comment 'cron表达式'
  , state TINYINT comment '状态'
  , remark TEXT comment '备注'
  , created_time DATETIME comment '创建时间'
  , constraint schedule_job_PKC primary key (id)
) comment '日程表' ;

-- 日程表记录
create table schedule_job_log (
  id INT not null AUTO_INCREMENT comment 'ID'
  , job_id INT not null comment '日程表ID'
  , bean_name VARCHAR(32) comment 'BEAN名称'
  , params TEXT comment '参数'
  , state TINYINT comment '状态'
  , error TEXT comment '报错内容'
  , times INT comment '反复次数'
  , created_time DATETIME comment '创建时间'
  , constraint schedule_job_log_PKC primary key (id)
) comment '日程表记录' ;

-- 权限
create table tbl_roles (
  id INT not null AUTO_INCREMENT comment 'ID'
  , name VARCHAR(20) comment '权限名称'
  , constraint tbl_roles_PKC primary key (id)
) comment '权限' ;

-- 权限表
create table tbl_member_roles (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_id INT not null comment '会员ID'
  , role_id INT not null comment '权限ID'
  , created_time DATETIME comment '创建时间'
  , constraint tbl_member_roles_PKC primary key (id)
) comment '权限表' ;

-- 直播间信息
create table tbl_prelive_spec (
  id INT not null AUTO_INCREMENT comment 'ID'
  , taobao_account_nick VARCHAR(20) comment '淘宝用户昵称'
  , live_cover_img VARCHAR(256) comment '封面图'
  , live_cover_img169 VARCHAR(256) comment '封面图'
  , live_title VARCHAR(32) comment '标题'
  , live_intro VARCHAR(256) comment '简介'
  , live_appointment_time TIME comment '开始时间'
  , live_channel_id INT(4) comment '频道'
  , live_column_id INT(4) comment '分类'
  , live_location VARCHAR(32) comment '地点'
  , hot_product_url VARCHAR(256) comment '热门连接'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_prelive_spec_PKC primary key (id)
) comment '直播间信息' ;

-- 会员与淘宝账号
create table tbl_member_tcc (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_id INT not null comment '会员ID'
  , taobao_account_nick VARCHAR(32) comment '淘宝用户昵称'
  , room_name VARCHAR(32) comment '直播间名称'
  , comment TEXT comment '备注'
  , state TINYINT comment '直播间状态:0：等待，1：正常，2：暂停，3：停止'
  , service_start_date DATETIME comment '服务开始时间'
  , service_end_date DATETIME comment '服务停止时间'
  , operation_start_time TIME comment '操作开始时间'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_member_tcc_PKC primary key (id)
) comment '会员与淘宝账号' ;

-- 引流预告模板
create table tbl_prelive_template (
  id INT not null AUTO_INCREMENT comment 'ID'
  , template_id INT not null comment '模板ID'
  , live_kind TINYINT comment '预告类型:0: 正式预告，1：高级预告'
  , live_channel_id INT(4) comment '直播频道'
  , live_column_id INT(4) comment '直播标签'
  , psc_channel_id INT(4) comment '商品频道'
  , psc_category_id INT(4) comment '商品分类'
  , psc_start_price INT(4) comment '商品低价格'
  , psc_end_price INT(4) comment '商品高价格'
  , psc_min_sales INT(4) comment '商品最小销量'
  , psc_product_count INT(4) comment '商品数'
  , psc_is_tmall BOOLEAN comment '是否天猫'
  , psc_sort_kind TINYINT comment '排序:0：综合排序，1：销量排序'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_prelive_template_PKC primary key (id,template_id)
) comment '引流预告模板' ;

-- 打助力任务
create table tbl_ranking_task (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_id INT comment '会员ID'
  , taocode VARCHAR(24) comment '淘口令'
  , live_id VARCHAR(32) comment '直播间ID'
  , room_name VARCHAR(32) comment '直播间名称'
  , start_score INT comment '起始助力值'
  , end_score INT comment '最后助力值'
  , target_score INT comment '目标助力值'
  , double_buy BOOLEAN comment '是否加购'
  , start_time DATETIME comment '开始时间'
  , end_time DATETIME comment '停止时间'
  , state TINYINT comment '状态:0：未开始，1：执行中，2：停止，3：结束，4：错误'
  , msg VARCHAR(256) comment '错误内容'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_ranking_task_PKC primary key (id)
) comment '打助力任务' ;

-- 模板
create table tbl_template (
  id INT not null AUTO_INCREMENT comment 'ID'
  , member_id INT comment '会员ID'
  , template_name VARCHAR(32) comment '模板名称'
  , template_kind TINYINT comment '模板类型:0：灵魂模板，1：盲眼模板'
  , is_del BOOLEAN comment '是否删除'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_template_PKC primary key (id)
) comment '模板' ;

-- 直播间引流操作
create table tbl_liveroom_strategy (
  id INT not null AUTO_INCREMENT comment 'ID'
  , taobao_account_nick VARCHAR(20) comment '淘宝账号昵称'
  , live_kind TINYINT comment '预告类型'
  , live_cover_img VARCHAR(256) comment '封面图'
  , live_cover_img169 VARCHAR(256) comment '封面图'
  , live_title VARCHAR(32) comment '标题'
  , live_intro VARCHAR(256) comment '简介'
  , live_appointment_time TIME comment '开始时间'
  , live_channel_id INT(4) comment '频道'
  , live_column_id INT(4) comment '分类'
  , live_location VARCHAR(32) comment '地点'
  , hot_product_url VARCHAR(256) comment '热门连接'
  , psc_channel_id INT(4) comment '商品频道'
  , psc_category_id INT(4) comment '商品类目'
  , psc_start_price INT(4) comment '商品低价格'
  , psc_end_price INT(4) comment '商品高价格'
  , psc_min_sales INT(4) comment '商品最小销量'
  , psc_product_count INT(4) comment '商品数'
  , psc_is_tmall BOOLEAN comment '是否天猫'
  , psc_sort_kind TINYINT comment '排序'
  , isdel BOOLEAN comment '是否删除'
  , created_time DATETIME comment '创建时间'
  , updated_time DATETIME comment '更新时间'
  , constraint tbl_liveroom_strategy_PKC primary key (id)
) comment '直播间引流操作' ;
