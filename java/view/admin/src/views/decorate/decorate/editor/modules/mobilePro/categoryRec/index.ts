import { cloneDeep } from "lodash";
import { registerModule } from "../../../config/module-registry";
import { defaultSubTitle, defaultModuleStyle, defaultContentStyle, defaultAnimation } from "@/views/decorate/decorate/src/modules/index";
import { isPro } from "@/utils/version";

export type { CategoryRecModule } from "./types";

const categoryList = [
    {
        title: "小零食",
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1462,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143iynI3ymaLzxGaRLk5h.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143iynI3ymaLzxGaRLk5h.jpg",
            picName: "FkVzGj1HD2aeDYmJ7E_VaFrIdr0H"
        }
    },
    {
        title: "粮油米面",
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1465,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143tV11a96dva46rixHbe.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143tV11a96dva46rixHbe.jpg",
            picName: "FmJJUiXFcIwl09Uo9QGJVuiDWhSt"
        }
    },
    {
        title: "洗发水",
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1464,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143OEttGMFtuKVqGX0JGF.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143OEttGMFtuKVqGX0JGF.jpg",
            picName: "FjJhor6dmekuWG2RdOJkpwDL2JOY"
        }
    },
    {
        title: "肉干",
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1466,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/17318961436JZDXeYmtceZlmj3GK.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/17318961436JZDXeYmtceZlmj3GK.jpg",
            picName: "Ftv7Xo3nQ9qZRYPnN9oQB"
        }
    },
    {
        title: "饮料",
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1463,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143uW4eqHtyOfGZ1zl7hO.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143uW4eqHtyOfGZ1zl7hO.jpg",
            picName: "FgboIgXyCabilx_7jFjqAc2vBrye"
        }
    },
    {
        title: "可爱桌面收纳盒",
        link: {
            link: {
                link: "",
                title: ""
            },
            linkTitle: ""
        },
        titleColor: "",
        backgroundColor: "",
        catPic: {
            picId: 1461,
            picThumb: "https://oss.tigshop.com/img/gallery/202411/1731896143OQ05zAtqjDwO7YD6jA.jpg?x-oss-process=image/resize,m_pad,h_200,h_200",
            picUrl: "https://oss.tigshop.com/img/gallery/202411/1731896143OQ05zAtqjDwO7YD6jA.jpg",
            picName: "FkVNwFY9oMelIYCDC7ZTNLwpeC"
        }
    }
];

/** 与旧 mobilePro/categoryRec/View.vue 的 defaultModule 逐字段一致（契约） */
export const categoryRecDefaults = {
    categoryList: categoryList, //分类列表
    animation: defaultAnimation, //动画
    categoryPadding: 3, // 分类内部间距
    radius: 25, // 圆角
    subTitle: defaultSubTitle, //分类标题文字
    backgroundColor: "#fff", // 背景颜色
    catBorder: 0, // 分类边框设置 1显示 0不显示
    catBorderWidth: 1, // 分类边框宽度
    catBorderColor: "#ffffff", // 分类边框颜色
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
};

registerModule({
    type: "categoryRec",
    label: "分类推荐",
    icon: '<i class="iconfont-admin icon-category_rec"></i>',
    url: "mobilePro",
    platform: "mobile",
    order: 6,
    enabled: isPro,
    preview: () => import("./Preview.vue"),
    settings: () => import("./Settings.vue"),
    getDefaultValue: () => cloneDeep(categoryRecDefaults)
});
