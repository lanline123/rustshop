import type { ModuleFrameType, ModuleStyle, ModuleSubTitle, ModuleContentStyle } from "@/types/decorate/decorate.d";

// 格式化模块设置
export const formatFrame = (frame: ModuleFrameType) => {
    return {
        innerPadding: "padding:" + frame.innerPadding + "px;",
        boxPadding: "padding-left:" + frame.boxPadding + "px;padding-right:" + frame.boxPadding + "px;",
        boxPaddingTop: "padding-top:" + frame.boxPaddingTop + "px;",
        boxPaddingBottom: "padding-bottom:" + frame.boxPaddingBottom + "px;",
        backgroundColor: frame.backgroundColor ? "background-color:" + frame.backgroundColor + ";" : "",
        boxRadius: "border-radius:" + frame.boxRadius + "px;",
        textFontSize: "font-size:" + frame.titleFontSize + "px;",
        textBlod: "font-weight:" + frame.titleFontWeight + ";"
    };
};

export const gradientMap: { [key: string]: string } = {
    upDown: "to bottom",
    leftRight: "to right",
    diagonal: "to right bottom"
};
type CommonStyle = {
    moduleStyle: any;
    moduleContentStyle: any;
    moduleSubTitle: any;
};
export const formatCommonStyle = (moduleStyle: ModuleStyle, moduleContentStyle?: ModuleContentStyle, moduleSubTitle?: ModuleSubTitle) => {
    const obj: CommonStyle = {
        moduleStyle: {} as ModuleStyle,
        moduleContentStyle: {} as ModuleContentStyle,
        moduleSubTitle: {}
    };
    if (moduleStyle) {
        obj.moduleStyle = {
            boxPaddingTopBottom: `padding-top:${moduleStyle.boxPaddingTopBottom}px;padding-bottom:${moduleStyle.boxPaddingTopBottom}px;`,
            boxPadding: "padding-left:" + moduleStyle.boxPadding + "px;padding-right:" + moduleStyle.boxPadding + "px;",
            boxMargin: "margin-left:" + (moduleStyle.boxMargin ?? 0) + "px;margin-right:" + (moduleStyle.boxMargin ?? 0) + "px;",
            boxPaddingTop: "padding-top:" + moduleStyle.boxPaddingTop + "px;",
            boxPaddingBottom: "padding-bottom:" + moduleStyle.boxPaddingBottom + "px;",
            backgroundColor: moduleStyle.backgroundColor ? "background-color:" + moduleStyle.backgroundColor + ";" : "",
            backgroundImage: moduleStyle.backgroundPic?.picUrl
                ? "background-image:url(" + moduleStyle.backgroundPic.picUrl + ");"
                : `background-color:${moduleStyle.backgroundColor};`,
            backgroundSize: `background-size: ${moduleStyle.backgroundPicFillType};`
        };
    }
    if (moduleContentStyle) {
        obj.moduleContentStyle = {
            paddingLeftRight: "padding-left:" + moduleContentStyle.paddingLeftRight + "px;padding-right:" + moduleContentStyle.paddingLeftRight + "px;",
            paddingTopBottom: "padding-top:" + moduleContentStyle.paddingTopBottom + "px;padding-bottom:" + moduleContentStyle.paddingTopBottom + "px;",
            paddingTop: "padding-top:" + moduleContentStyle.paddingTop + "px;",
            paddingBottom: "padding-bottom:" + moduleContentStyle.paddingBottom + "px;",
            innerPadding: "padding:" + moduleContentStyle.padding + "px;",
            backgroundColor: moduleContentStyle.backgroundColor ? "background-color:" + moduleContentStyle.backgroundColor + ";" : "",
            backgroundImage: moduleContentStyle.backgroundPic?.picUrl
                ? "background-image:url(" + moduleContentStyle.backgroundPic.picUrl + ");"
                : `background-image: linear-gradient( ${gradientMap[moduleContentStyle.gradientType]}, ${moduleContentStyle.gradientColorA}, ${
                      moduleContentStyle.gradientColorB
                  });`,
            backgroundSize: `background-size: ${moduleContentStyle.backgroundPicFillType};`,
            boxRadius: `border-radius:
              ${moduleContentStyle.boxRadiusTop}px ${moduleContentStyle.boxRadiusTop}px ${moduleContentStyle.boxRadiusBottom}px ${moduleContentStyle.boxRadiusBottom}px;`,
            boxRadius2: `border-radius:${moduleContentStyle.boxRadius}px;`,
            boxShadow: moduleContentStyle.boxShadow === 1 ? "box-shadow:rgba(0, 0, 0, 0.06) 0px 2px 4px;" : ""
        };
    }

    if (moduleSubTitle) {
        obj.moduleSubTitle = {
            textFontSize: "font-size:" + moduleSubTitle.titleFontSize + "px;",
            textBlod: "font-weight:" + moduleSubTitle.titleFontBold + ";",
            textColor: "color:" + moduleSubTitle.titleColor + ";"
        };
    }
    return obj;
};

export const formatAnimation = (type: string, range: number) => {
    const num = range ? 50 * range : 50 * 0.3;
    switch (type) {
        case "close":
            return ``;
        case "scale":
            return `transform: translate(0px, 0px) scale(${range});opacity:0;`;
        case "slideLeft":
            return `transform: translate(-${num}px, 0px) scale(1);opacity:0;`;
        case "slideRight":
            return `transform: translate(${num}px, 0px) scale(1);opacity:0;`;
        case "slideTop":
            return `transform: translate(0px, ${num}px) scale(1);opacity:0;`;
        case "flip":
            return `transform: translate(0px, 0px) scale(1) rotateY(-180deg);opacity:0;`;
        default:
            return "";
    }
};

export const copyArray = (data: any, num = 1) => {
    let result: any = [];
    for (let i = 0; i < num; i++) {
        if (data && typeof data === "object") {
            result = [...result, ...data];
        } else {
            result = [...result, data];
        }
    }
    return result;
};

// 图片橱窗：比例字面量 → padding-top 百分比（兼容微信小程序，不依赖 aspect-ratio）
// 公式：padding-top = (高 / 宽) * 100%
// 与 admin 端 Tigshop-Admin/src/views/decorate/decorate/src/modules/index.ts 保持一致
export const imageShowcasePaddingMap: Record<string, string> = {
    default: "",
    "3:2": "66.667%",
    "1:1": "100%",
    "3:4": "133.333%",
    "2:1": "50%",
    "16:9": "56.25%"
};
