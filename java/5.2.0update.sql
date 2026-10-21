alter table cart
    add extra_data json null comment '扩展数据';


create table groupon_promotion
(
    groupon_promotion_id bigint auto_increment comment '拼团 ID'
        primary key,
    name                 varchar(255)  not null comment '活动名称',
    start_time           bigint        not null comment '开始时间',
    end_time             bigint        not null comment '结束时间',
    group_type           tinyint       not null comment '拼团类型；1-普通团，2-阶梯团',
    expiration_time      bigint        not null comment '成团有效时间',
    groupon_image        varchar(500)  null comment '活动图片',
    share_image          varchar(500)  null comment '分享图',
    groupon_explain      varchar(1000) null comment '活动说明',
    shop_id              int           null comment '所属店铺',
    add_time             bigint        not null comment '创建时间'
)
    comment '拼团活动';

create table groupon_promotion_detail
(
    groupon_detail_id         bigint auto_increment comment 'ID'
        primary key,
    groupon_promotion_id      bigint  not null comment '拼团活动 ID',
    groupon_promotion_item_id bigint  not null comment '拼团商品 ID',
    product_id                bigint  not null comment '商品ID',
    sku_id                    bigint  not null comment '规格 id',
    group_price               json    not null comment '拼团价',
    limit_num                 int     not null comment '可售数量',
    is_join                   tinyint not null comment '是否参加；0-否，1-是',
    add_time                  bigint  not null comment '创建时间'
)
    comment '拼团信息';

create index groupon_promotion_detail_groupon_promotion_id_index
    on groupon_promotion_detail (groupon_promotion_id);

create index groupon_promotion_detail_groupon_promotion_item_id_index
    on groupon_promotion_detail (groupon_promotion_item_id);

create index groupon_promotion_detail_product_id_index
    on groupon_promotion_detail (product_id);

create index groupon_promotion_detail_sku_id_index
    on groupon_promotion_detail (sku_id);

create table groupon_promotion_item
(
    groupon_promotion_item_id bigint auto_increment comment '拼团活动商品 ID'
        primary key,
    groupon_promotion_id      bigint        not null comment '拼团活动 ID',
    product_id                bigint        not null comment '商品ID',
    group_type                tinyint       not null comment '拼团类型；1-普通团，2-阶梯团',
    group_num                 json          not null comment '成团人数（json）',
    order_limit_num           int           not null comment '每单限购数量；0-不限购',
    is_auto_group             tinyint       not null comment '是否自动成团；0-否，1-是',
    min_group_num             int           not null comment '成团最低人数',
    sort                      int default 0 not null comment '排序',
    add_time                  bigint        not null comment '创建时间'
)
    comment '拼团活动商品';

create index groupon_promotion_item_groupon_promotion_id_index
    on groupon_promotion_item (groupon_promotion_id);

create index groupon_promotion_item_product_id_index
    on groupon_promotion_item (product_id);

create table groupon_promotion_record
(
    groupon_promotion_record_id bigint auto_increment comment '拼团记录 ID'
        primary key,
    groupon_promotion_id        bigint       not null comment '拼团活动 ID',
    groupon_promotion_item_id   bigint       not null comment '拼团活动商品 ID',
    product_id                  bigint       not null comment '商品ID',
    product_name                varchar(255) not null comment '商品名称',
    join_type                   tinyint      not null comment '参与类型；1-开团，2-参团',
    join_status                 tinyint      not null comment '拼团状态；1-待成团，2-拼团成功，3-拼团失败',
    user_id                     bigint       not null comment '用户 ID',
    order_id                    bigint       not null comment '订单 ID',
    order_sn                    varchar(20)  not null comment '订单号',
    is_virtual                  tinyint      not null comment '是否虚拟；0-否，1-是',
    paid_amount                 decimal      not null comment '支付金额',
    open_group_record_id        bigint       not null comment '开团记录 ID',
    open_group_time             bigint       not null comment '开团时间',
    group_captain_name          varchar(255) not null comment '团长名称',
    join_expiration_time        bigint       not null comment '成团截止时间',
    min_group_num               int          not null comment '成团最低人数',
    shop_id                     int          not null comment '所属店铺',
    add_time                    bigint       not null comment '创建时间'
)
    comment '拼团记录';

create index groupon_promotion_record_groupon_promotion_id_index
    on groupon_promotion_record (groupon_promotion_id);

create index groupon_promotion_record_groupon_promotion_item_id_index
    on groupon_promotion_record (groupon_promotion_item_id);