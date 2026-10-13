INSERT INTO decorate_discrete (decorate_sn, decorate_name, data, shop_id) VALUES ('PcIndexTips', 'PcIndexTips', '{
    noticeGroups: [
    {
        notices: [
            {
                picId: 1227,
                picThumb: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "免运费",
                picDesc: "前三单购物均可免运费！"
            }
        ]
    },
    {
        notices: [
            {
                picId: 1227,
                picThumb: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "24*7客户支持",
                picDesc: "随时随地提供保障"
            },
            {
                picId: 1227,
                picThumb: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "24*7客户支持",
                picDesc: "随时随地提供保障"
            },
            {
                picId: 1227,
                picThumb: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
                picUrl: "https://oss.tigshop.com/img/gallery/202508/1755755295PIlbfbkinsCQQNII1e.jpeg",
                picName: "FmBvbpIyBQcPAjaR1tpYu4NSfM2L",
                picTitle: "24*7客户支持",
                picDesc: "随时随地提供保障"
            }
        ]
    }
],
    isClose: 1,
    titleColor: "#ffffff",
    descColor: "#c7c6c7",
    backgroundColor: "#1a1a1a"
}', 0);

alter table area_code
    modify name varchar(64) default '' not null comment '名称';

alter table user_address
    add mobile_area_code varchar(20) comment '区号';