/**
 * 拼团模块共享滚动工具（移植自 Vort-Admin _shared/groupon/utils/scroll）
 * 说明：提供横向滚动列表的步长计算与滚动控制
 */
import type { Ref } from "vue";

/**
 * 计算滚动步长（基于第一个列表项宽度 + gap）
 * @param el 滚动容器元素
 * @param itemSelector 列表项选择器，默认为 ".proditem"
 * @returns 滚动步长（px）
 */
export function getScrollStep(el: HTMLElement, itemSelector = ".proditem"): number {
    const firstItem = el.querySelector<HTMLElement>(itemSelector);
    if (!firstItem) {
        // 未找到子项时，回退到容器宽度的 80%
        return Math.max(200, Math.floor(el.clientWidth * 0.8));
    }
    const style = window.getComputedStyle(el);
    const gapRaw = style.columnGap && style.columnGap !== "normal" ? style.columnGap : style.gap;
    const gap = Number.parseFloat(gapRaw || "0") || 0;
    const w = firstItem.getBoundingClientRect().width;
    return Math.max(200, Math.floor(w + gap));
}

/**
 * 向左滚动一步
 * @param el 滚动容器元素
 * @param itemSelector 列表项选择器
 */
export function scrollPrev(el: HTMLElement | null, itemSelector?: string): void {
    if (!el) return;
    el.scrollBy({ left: -getScrollStep(el, itemSelector), behavior: "smooth" });
}

/**
 * 向右滚动一步
 * @param el 滚动容器元素
 * @param itemSelector 列表项选择器
 */
export function scrollNext(el: HTMLElement | null, itemSelector?: string): void {
    if (!el) return;
    el.scrollBy({ left: getScrollStep(el, itemSelector), behavior: "smooth" });
}

/**
 * 创建滚动控制方法（便于在组件中使用）
 * @param listRef 滚动容器的 ref
 * @param itemSelector 列表项选择器
 * @returns { scrollPrev, scrollNext } 滚动方法
 */
export function useScrollControl(listRef: Ref<HTMLElement | null>, itemSelector?: string) {
    return {
        scrollPrev: () => scrollPrev(listRef.value, itemSelector),
        scrollNext: () => scrollNext(listRef.value, itemSelector)
    };
}
