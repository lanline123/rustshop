<template>
    <view class="groupon-list-layout">
        <view v-if="layoutMode === 'list'" class="goods-list">
            <view v-for="item in grouponItemList" :key="item.grouponPromotionItemId">
                <slot name="default" :item="item" />
            </view>
        </view>
        <view v-if="layoutMode === 'waterfall'" class="goods-list-waterfall" :style="{ '--waterfall-columns': props.columns }">
            <view ref="waterfallContainerRef" class="waterfall-container">
                <view v-for="(columnData, index) in columnList" :key="index" class="waterfall-column">
                    <template v-for="item in columnData" :key="item.grouponPromotionItemId">
                        <slot name="default" :item="item" />
                    </template>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts" setup>
/**
 * 拼团列表布局组件
 * - 提供列表和瀑布流两种布局
 * - 动态计算瀑布流列高度
 */
import { getImageInfo } from "@/utils";
import type { GrouponItem } from "@/types/home/home";
import { ref, watch } from "vue";

interface IProps {
    layoutMode: "list" | "waterfall";
    grouponItemList: GrouponItem[];
    columns?: number;
}
const props = withDefaults(defineProps<IProps>(), {
    columns: 2
});

// 记录每个列的list
const columnList = ref<any[]>([]);
// 存储列高度信息
const columnHeights = ref<number[]>([]);
// 列宽
const columnWidth = ref(0);
// 存储已排列过的商品
const itemPositions = ref<Record<number, number>>({});
const waterfallContainerRef = ref<HTMLElement | null>(null);

/**
 * 计算瀑布流布局
 * @param recomputed 是否重新计算布局
 */
const calculateWaterfallLayout = async (recomputed?: boolean) => {
    try {
        const rect = await new Promise<UniApp.NodeInfo>((resolve) => {
            let resolved = false;
            const query = uni.createSelectorQuery().in(waterfallContainerRef.value);
            query
                .select(".waterfall-container")
                .boundingClientRect((res) => {
                    if (!resolved) {
                        resolved = true;
                        resolve(res as UniApp.NodeInfo);
                    }
                })
                .exec();

            // 设置超时防止永远不resolve
            setTimeout(() => {
                if (!resolved) {
                    resolved = true;
                    resolve({} as UniApp.NodeInfo);
                }
            }, 1000);
        });

        if (rect && rect.width) {
            // 计算列宽（容器宽度 - 间距）/ 列数
            const containerWidth = rect.width;
            const gap = 10; // 间距10px
            columnWidth.value = (containerWidth - gap) / 2;

            // 确定需要计算位置的项目范围
            let startIndex = 0;
            // 不是重新计算时,获取之前已计算位置的个数
            if (!recomputed) {
                startIndex = Object.keys(itemPositions.value).length;
            }

            // 为每个商品项计算位置
            for (let i = startIndex; i < props.grouponItemList.length; i++) {
                const item = props.grouponItemList[i];

                // 异步获取每个商品项的高度
                const itemHeight = await estimateItemHeight(item);

                // 找到高度最小的列
                const minHeightColumnIndex = columnHeights.value.indexOf(Math.min(...columnHeights.value));

                // 往对应的列添加商品项
                columnList.value[minHeightColumnIndex].push(item);

                // 存储位置信息
                itemPositions.value[item.grouponPromotionItemId] = columnList.value[minHeightColumnIndex].length;

                // 更新列高度
                columnHeights.value[minHeightColumnIndex] += itemHeight;
            }
        }
    } catch (error) {
        console.error("布局计算出错:", error);
    }
};

// 估算商品项高度（异步获取图片高度）
const estimateItemHeight = async (item: GrouponItem): Promise<number> => {
    // 获取图片实际高度
    let imageHeight = 345; // 默认高度
    let detailHeight = 240; // 默认detail高度

    try {
        const imageInfo = await getImageInfo(item.picThumb);
        if (imageInfo) {
            // 根据容器宽度计算图片实际显示高度（保持宽高比）
            const containerWidth = columnWidth.value || 345; // 如果还没计算出列宽，使用默认值
            // 计算图片缩放比例
            const scale = containerWidth / imageInfo.width;
            imageHeight = imageInfo.height * scale;
        }
    } catch (error) {
        console.warn("获取图片信息失败", error);
    }

    try {
        // 获取detail元素的实际高度
        detailHeight = await new Promise<number>((resolve) => {
            // 等待DOM更新后再查询
            setTimeout(() => {
                const query = uni.createSelectorQuery();
                query
                    .select(`#item-${item.grouponPromotionItemId} .detail`)
                    .boundingClientRect((rect) => {
                        const detailRect = rect as UniApp.NodeInfo;
                        if (detailRect && detailRect.height && detailRect.height > 0) {
                            resolve(detailRect.height);
                        } else {
                            // 如果获取不到实际高度，使用估算值
                            // 标题高度（根据字符数和行高估算）
                            let titleLines = Math.ceil(item.productName.length / 11); // 假设每行11个字符
                            titleLines = titleLines > 2 ? 2 : titleLines; // 最多显示2行
                            const titleHeight = titleLines * 20; // 每行20px高度

                            // 其他信息高度
                            const infoHeight = 120; // 团信息、价格等固定高度

                            resolve(titleHeight + infoHeight);
                        }
                    })
                    .exec();
            }, 50); // 短暂延迟确保元素已渲染
        });
    } catch (error) {
        console.warn("获取detail高度失败", error);
        // 使用默认估算值
        let titleLines = Math.ceil(item.productName.length / 11);
        titleLines = titleLines > 2 ? 2 : titleLines;
        const titleHeight = titleLines * 20;
        detailHeight = titleHeight + 120;
    }
    return imageHeight + detailHeight + uni.upx2px(10);
};

const _init = () => {
    // 根据指定列数，初始化存储列的数组
    for (let i = 0; i < props.columns; i++) {
        columnList.value.push([]);
        columnHeights.value.push(0);
    }
    layout();
};

const layout = async () => {
    // 如果是瀑布流模式，则计算瀑布流布局
    if (props.layoutMode === "waterfall") {
        calculateWaterfallLayout();
    }
};

_init();

watch(
    () => [props.grouponItemList, props.columns, props.layoutMode],
    () => {
        layout();
    }
);
</script>

<style lang="scss" scoped>
.goods-list-waterfall {
    padding: 20rpx;
    background: #fff;
    position: relative;

    .waterfall-container {
        position: relative;
        display: grid;
        grid-template-columns: repeat(var(--waterfall-columns), 1fr);
        gap: 20rpx;

        .waterfall-column {
            display: flex;
            flex-direction: column;
            gap: 20rpx;
        }
    }
}
</style>
