create database if not exists cstp default character set utf8;
create table user(
     id bigint not null primary key comment '用户id',
     username varchar(20) not null comment '用户名',
     password varchar(100) not null comment '密码',
     phone varchar(11) not null comment '手机号',
     name varchar(20) comment '姓名',
     birthday date comment '生日',
     gender tinyint comment '性别:1-男,2-女',
     avatar varchar(100) comment '头像',
     create_time datetime not null default current_timestamp comment '创建时间',
     update_time datetime not null default current_timestamp on update current_timestamp comment '更新时间',
     is_delete tinyint not null default 0 comment '是否删除:0-否,1-是'
)comment '用户表';
create table goods(
      id bigint not null primary key comment '商品id',
      user_id bigint  not null comment '关联user表id',
      name varchar(100) not null comment '商品名',
      price decimal(10,2) not null comment '价格',
      type varchar(20) not null comment '类型',
      image varchar(100) comment '图片',
      description text comment '描述',
      status tinyint not null default 1 comment '状态:0-下架,1-上架,2-已售出',
      create_time datetime not null default current_timestamp comment '创建时间',
      update_time datetime not null default current_timestamp on update current_timestamp comment '更新时间',
      is_delete tinyint not null default 0 comment '是否删除:0-否,1-是'
)comment '商品表';
create table goods_image(
    id bigint not null primary key comment '图片id',
    goods_id bigint not null comment '关联商品id',
    url varchar(255) not null comment '图片路径',
    sort int default 0 comment '排序',
    create_time datetime not null default current_timestamp comment '创建时间',
    update_time datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    is_delete tinyint not null default 0 comment '是否删除:0-否,1-是'

)comment '商品图片表';
-- 会话表：一个"买家+商品"唯一对应一个会话
create table conversation(
     id bigint not null primary key comment '会话id',
     goods_id bigint not null comment '关联商品id',
     min_id bigint not null comment '买家id',
     max_id bigint not null comment '卖家id(冗余存,免每次查goods)',
     last_message varchar(500) comment '最后一条消息内容(会话列表展示用)',
     last_time datetime comment '最后一条消息时间',
     create_time datetime not null default current_timestamp,
     update_time datetime not null default current_timestamp on update current_timestamp,
     is_delete tinyint not null default 0,
     unique key uk_min_max(min_id, max_id)  -- 防止重复建会话
)comment '会话表';

-- 消息表
create table message(
    id bigint not null primary key comment '消息id',
    conversation_id bigint not null comment '会话id',
    sender_id bigint not null comment '发送者id',
    content varchar(500) not null comment '内容',
    is_read tinyint not null default 0 comment '是否已读:0-否,1-是',
    create_time datetime not null default current_timestamp,
    is_delete tinyint not null default 0,
    index idx_conversation(conversation_id, id)  -- 分页查询要这个组合索引
)comment '消息表';
create table if not exists goods_order(
      id bigint not null primary key comment '订单id(雪花)',
      order_no varchar(32) not null comment '业务订单号,唯一,展示用',
      goods_id bigint not null comment '商品id',
      goods_name varchar(100) not null comment '商品名快照',
      goods_image varchar(255) comment '商品主图快照',
      price decimal(10,2) not null comment '成交价快照',
      buyer_id bigint not null comment '买家id',
      seller_id bigint not null comment '卖家id',
      receiver_name varchar(20) not null comment '收货人',
      receiver_phone varchar(11) not null comment '收货手机号',
      receiver_address varchar(200) not null comment '收货地址',
      status tinyint not null default 1 comment '状态:1-待发货,2-待收货,3-已完成,4-已取消',
      ship_company varchar(50) comment '物流公司',
      ship_no varchar(50) comment '物流单号',
      ship_time datetime comment '发货时间',
      finish_time datetime comment '完成时间',
      cancel_time datetime comment '取消时间',
      create_time datetime not null default current_timestamp comment '下单时间',
      update_time datetime not null default current_timestamp on update current_timestamp comment '更新时间',
      is_delete tinyint not null default 0 comment '是否删除:0-否,1-是',
      unique key uk_order_no(order_no),
      index idx_buyer(buyer_id, status),
      index idx_seller(seller_id, status),
      index idx_goods(goods_id)
)comment '订单表';