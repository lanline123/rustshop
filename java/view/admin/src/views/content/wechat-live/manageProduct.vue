<template>
    <div class="manage-live-product-page">
        <div class="room-card">
            <Image :src="room.feedsImg || defaultCover" fit="cover" style="width: 120px; height: 80px; border-radius: 4px" />
            <div class="room-card__info">
                <div class="room-card__title">
                    <span class="room-name">「{{ room.wechatLiveTitle || $t('直播间') }}」</span>
                    <el-tag size="small" :type="liveStatusTagType">{{ room.liveStatusText || $t('未开始') }}</el-tag>
                </div>
                <div class="room-card__meta">{{ $t('开播时段：{startTime}至{endTime}', { startTime: room.startTime || "-", endTime: room.endTime || "-" }) }}</div>
                <div class="room-card__meta">{{ $t('房间号：{roomIdStr}', { roomIdStr: room.roomIdStr || room.roomId || "-" }) }}</div>
                <div class="room-card__meta">{{ $t('主播：{anchorName}', { anchorName: room.anchorName || "-" }) }}</div>
            </div>
        </div>

        <div class="toolbar">
            <DialogForm
                :params="{
                    selectedIds: selectedGoodsIds
                }"
                isDrawer
                path="content/wechat-live-product/src/SelectProduct"
                :title="$t('选择直播商品')"
                width="1000px"
                @okCallback="onSelectProductOk"
            >
                <el-button type="primary">{{ $t('添加商品') }}</el-button>
            </DialogForm>
        </div>

        <el-table :data="productList" row-key="id">
            <el-table-column :label="$t('序号')" type="index" width="70" />
            <el-table-column :label="$t('商品信息')" min-width="360">
                <template #default="{ row }">
                    <div class="product-info">
                        <Image :src="row.productImage" fit="cover" style="width: 50px; height: 50px; border-radius: 4px" />
                        <div class="product-info__text">
                            <div class="product-name">{{ row.productName }}</div>
                            <div class="product-price">¥ {{ row.price }}</div>
                        </div>
                    </div>
                </template>
            </el-table-column>
            <el-table-column :label="$t('状态')" width="140">
                <template #default="{ row }">
                    <span>{{ row.statusText }}</span>
                </template>
            </el-table-column>
            <el-table-column :label="$t('操作')" width="140" fixed="right">
                <template #default="{ row }">
                    <el-button type="danger" link @click="handleRemove(row.id)">{{ $t('移除商品') }}</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { Image } from "@/components/image";
import { DialogForm } from "@/components/dialog";
import { addWechatLiveGoods, getWechatLiveGoods, removeWechatLiveGoods } from "@/api/content/wechatLive";
import type { WechatLiveProductState } from "@/types/content/wechatLiveProduct.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
interface LiveRoomInfo {
    roomId?: number;
    roomIdStr?: string;
    wechatLiveTitle?: string;
    coverImg?: string;
    anchorName?: string;
    liveStatus?: number;
    liveStatusText?: string;
    startTime?: string;
    endTime?: string;
}

interface LiveProductItem {
    id: number;
    productId: number;
    productName: string;
    productImage: string;
    price: string;
    statusText: string;
}

const props = defineProps<{
    id?: number;
    roomData?: LiveRoomInfo;
}>();

const defaultCover = "https://via.placeholder.com/120x80?text=LIVE";

const room = computed<LiveRoomInfo>(() => props.roomData || {});

const liveStatusTagType = computed(() => {
    const map: Record<number, "info" | "success" | "warning" | "danger"> = {
        101: "success",
        102: "info",
        103: "warning",
        104: "danger",
        105: "warning",
        106: "danger",
        107: "info"
    };
    return map[Number(room.value.liveStatus)] || "info";
});

const productList = ref<LiveProductItem[]>([]);
const currentRoomId = computed(() => Number(props.id || room.value.roomId || 0));
const selectedGoodsIds = computed(() => productList.value.map((item) => item.id));

const loadProductList = async () => {
    if (!currentRoomId.value) return;
    try {
        const result = await getWechatLiveGoods(currentRoomId.value);
        productList.value = result.map((item) => ({
            id: Number(item.id || 0),
            productId: Number(item.productId || 0),
            productName: item.goodsName || "-",
            productImage: item.coverImg || "",
            price: item.price !== undefined ? (Number(item.price) / 100).toFixed(2) : "0.00",
            statusText: item.auditStatusText || "-"
        }));
    } catch (error: any) {
        message.error(error.message || t('加载商品失败'));
    }
};

// 选择直播商品弹窗确认回调
const onSelectProductOk = async (selectedProducts: WechatLiveProductState[]) => {
    const safeList = Array.isArray(selectedProducts) ? selectedProducts : [];

    try {
        const goodsIds = Array.from(new Set(safeList.map((item) => Number(item.id || 0)).filter((item) => item > 0)));

        if (goodsIds.length === 0) {
            message.warning(t('请选择商品'));
            return;
        }

        await addWechatLiveGoods({
            roomId: currentRoomId.value,
            goodsIds
        });
        message.success(t('添加成功'));
        loadProductList();
    } catch (error: any) {
        message.error(error.message || t('添加失败'));
    }
};

const handleRemove = async (goodsId: number) => {
    try {
        await removeWechatLiveGoods({
            roomId: currentRoomId.value,
            goodsId
        });
        message.success(t('移除成功'));
        loadProductList();
    } catch (error: any) {
        message.error(error.message || t('移除失败'));
    }
};

onMounted(() => {
    loadProductList();
});
</script>

<style lang="less" scoped>
.manage-live-product-page {
    padding: 20px;
}

.room-card {
    display: flex;
    gap: 14px;
    padding: 16px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    margin-bottom: 16px;
    background: #fafafa;
}

.room-card__title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}

.room-name {
    font-size: 18px;
    font-weight: 500;
}

.room-card__meta {
    color: #666;
    line-height: 22px;
}

.toolbar {
    margin-bottom: 14px;
}

.product-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.product-info__text {
    min-width: 0;
}

.product-name {
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-price {
    margin-top: 4px;
    color: #333;
}
</style>
