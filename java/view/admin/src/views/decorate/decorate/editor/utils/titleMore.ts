/**
 * 模块标题右侧区（更多链接 / 倒计时）显示推导
 *
 * 拼团、秒杀模块的标题右侧区由 title.showMore（总开关）+ title.moreType（内容类型）共同决定。
 * moreType 是后加的字段，老数据没有，需按各模块原先对 showMore 的语义降级推导，保证行为不变。
 */

/** 标题右侧内容类型 */
export const MORE_TYPE_LINK = 1;
export const MORE_TYPE_COUNTDOWN = 2;

/**
 * 老数据语义，决定 moreType 缺失时的推导方式：
 * - countdown：首页新套拼团/秒杀，showMore=1 原本就是"显示顶部倒计时"
 * - link：自定义页拼团，showMore=1 原本是"显示查看更多"
 * - seckillCustom：自定义页秒杀，showMore=0 时也无条件显示倒计时，仅 showMore=1 时额外显示查看更多
 */
export type TitleMoreLegacySemantic = "countdown" | "link" | "seckillCustom";

/** 标题右侧区可读的最小配置形状（两套 ModuleTitle 类型都满足） */
interface TitleMoreSource {
    showMore?: number;
    moreType?: number;
    countdownLabel?: string;
}

export interface TitleMoreResult {
    /** 标题右侧区是否渲染 */
    visible: boolean;
    /** 渲染哪种内容 */
    mode: typeof MORE_TYPE_LINK | typeof MORE_TYPE_COUNTDOWN;
}

export function resolveTitleMore(title: TitleMoreSource | null | undefined, legacy: TitleMoreLegacySemantic): TitleMoreResult {
    const showMore = Number(title?.showMore) === 1;
    const moreType = Number(title?.moreType);

    if (moreType === MORE_TYPE_LINK || moreType === MORE_TYPE_COUNTDOWN) {
        return { visible: showMore, mode: moreType };
    }

    if (legacy === "seckillCustom") {
        return showMore ? { visible: true, mode: MORE_TYPE_LINK } : { visible: true, mode: MORE_TYPE_COUNTDOWN };
    }

    return { visible: showMore, mode: legacy === "link" ? MORE_TYPE_LINK : MORE_TYPE_COUNTDOWN };
}

/**
 * 倒计时前缀文字：只认 countdownLabel，未配置就用模块默认文案。
 *
 * 不从 moreLink.title 降级：该字段在首页模块里存的是倒计时前缀，在自定义页模块里存的却是
 * 「查看更多」这类链接文案，两种语义无法从数据本身区分，读它会让倒计时渲染成
 * 「查看更多: 02:11:33」。首页存量若改过前缀，需在装修面板的「前缀文字」重填一次。
 */
export function resolveCountdownLabel(title: TitleMoreSource | null | undefined, fallback: string): string {
    return title?.countdownLabel?.trim() || fallback;
}
