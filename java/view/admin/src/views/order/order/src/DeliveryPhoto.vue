<template>
    <div class="container-card" v-if="deliveryInfo">
        <div class="title">{{ deliveryInfo.typeName === $t('配送订单') ? $t('配送照片') : $t('服务照片') }}</div>
        <div class="delivery-info">
            <div class="staff-info">
                <el-avatar :size="48" :src="deliveryInfo.staffAvatar || defaultAvatar">
                    <el-icon :size="24"><User /></el-icon>
                </el-avatar>
                <div class="staff-detail">
                    <div class="staff-name">{{ deliveryInfo.typeName === $t('配送订单') ? $t('配送员') : $t('服务员') }}：{{ deliveryInfo.staffName }}</div>
                    <div class="staff-phone">{{ $t('联系电话：{staffPhone}', { staffPhone: deliveryInfo.staffPhone || '--' }) }}</div>
                    <div class="order-info">
                        <span>{{ $t('订单编号：{orderSn}', { orderSn: orderSn || deliveryInfo.deliveryOrderSn }) }}</span>
                        <el-tag :type="statusTagType" size="small" class="ml10">{{ deliveryInfo.statusName }}</el-tag>
                    </div>
                </div>
            </div>
        </div>
        <!-- 配送订单：取货照和送达照 -->
        <template v-if="deliveryInfo.typeName === '配送订单'">
            <div class="photo-section" v-if="deliveryInfo.pickupPhoto && deliveryInfo.pickupPhoto.length > 0">
                <div class="photo-title">{{ $t('取货照片') }}</div>
                <div class="photo-list">
                    <el-image
                        v-for="(photo, index) in deliveryInfo.pickupPhoto"
                        :key="'pickup-' + index"
                        :src="photo"
                        :preview-src-list="deliveryInfo.pickupPhoto"
                        :initial-index="index"
                        fit="cover"
                        class="photo-item"
                        preview-teleported
                    >
                        <template #error>
                            <div class="image-error">
                                <el-icon><Picture /></el-icon>
                            </div>
                        </template>
                    </el-image>
                </div>
            </div>
            <div class="photo-section" v-if="deliveryInfo.completePhoto && deliveryInfo.completePhoto.length > 0">
                <div class="photo-title">{{ $t('送达照片') }}</div>
                <div class="photo-list">
                    <el-image
                        v-for="(photo, index) in deliveryInfo.completePhoto"
                        :key="'complete-' + index"
                        :src="photo"
                        :preview-src-list="deliveryInfo.completePhoto"
                        :initial-index="index"
                        fit="cover"
                        class="photo-item"
                        preview-teleported
                    >
                        <template #error>
                            <div class="image-error">
                                <el-icon><Picture /></el-icon>
                            </div>
                        </template>
                    </el-image>
                </div>
            </div>
            <!-- <el-empty v-if="!hasDeliveryPhotos" description="暂无配送照片" :image-size="60" /> -->
        </template>
        <!-- 服务订单：服务前照片和服务后照片 -->
        <template v-else>
            <div class="photo-section" v-if="deliveryInfo.beforeServicePhoto && deliveryInfo.beforeServicePhoto.length > 0">
                <div class="photo-title">{{ $t('服务前照片') }}</div>
                <div class="photo-list">
                    <el-image
                        v-for="(photo, index) in deliveryInfo.beforeServicePhoto"
                        :key="'before-' + index"
                        :src="photo"
                        :preview-src-list="deliveryInfo.beforeServicePhoto"
                        :initial-index="index"
                        fit="cover"
                        class="photo-item"
                        preview-teleported
                    >
                        <template #error>
                            <div class="image-error">
                                <el-icon><Picture /></el-icon>
                            </div>
                        </template>
                    </el-image>
                </div>
            </div>
            <div class="photo-section" v-if="deliveryInfo.afterServicePhoto && deliveryInfo.afterServicePhoto.length > 0">
                <div class="photo-title">{{ $t('服务后照片') }}</div>
                <div class="photo-list">
                    <el-image
                        v-for="(photo, index) in deliveryInfo.afterServicePhoto"
                        :key="'after-' + index"
                        :src="photo"
                        :preview-src-list="deliveryInfo.afterServicePhoto"
                        :initial-index="index"
                        fit="cover"
                        class="photo-item"
                        preview-teleported
                    >
                        <template #error>
                            <div class="image-error">
                                <el-icon><Picture /></el-icon>
                            </div>
                        </template>
                    </el-image>
                </div>
            </div>
            <!-- <el-empty v-if="!hasServicePhotos" description="暂无服务照片" :image-size="60" /> -->
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { User, Picture } from "@element-plus/icons-vue";

interface DeliveryOrderInfo {
    deliveryOrderId: number;
    deliveryOrderSn: string;
    status: number;
    statusName: string;
    staffId: number;
    staffName: string;
    staffPhone: string;
    staffAvatar: string | null;
    type: number;
    typeName: string;
    pickupPhoto: string[] | null;
    completePhoto: string[] | null;
    beforeServicePhoto: string[] | null;
    afterServicePhoto: string[] | null;
}

const props = defineProps<{
    deliveryInfo: DeliveryOrderInfo | null;
    /** 使用组件时传入的订单编号（优先展示） */
    orderSn?: string;
}>();

const defaultAvatar = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";

// 状态标签类型
const statusTagType = computed(() => {
    const status = props.deliveryInfo?.status;
    if (status === 40) return "success"; // 已完成
    if (status === 30) return "warning"; // 进行中
    if (status === 20) return "info"; // 待处理
    return "info";
});

// 是否有配送照片
const hasDeliveryPhotos = computed(() => {
    const info = props.deliveryInfo;
    return (info?.pickupPhoto && info.pickupPhoto.length > 0) || 
           (info?.completePhoto && info.completePhoto.length > 0);
});

// 是否有服务照片
const hasServicePhotos = computed(() => {
    const info = props.deliveryInfo;
    return (info?.beforeServicePhoto && info.beforeServicePhoto.length > 0) || 
           (info?.afterServicePhoto && info.afterServicePhoto.length > 0);
});
</script>

<style scoped lang="less">
.container-card {
    border: 1px solid #ececec;
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;

    .title {
        font-size: 16px;
        line-height: 25px;
        color: #323233;
        margin-bottom: 15px;
        font-weight: 700;
    }
}

.delivery-info {
    margin-bottom: 20px;
    padding-bottom: 16px;
    // border-bottom: 1px solid #f0f0f0;

    .staff-info {
        display: flex;
        align-items: flex-start;
        gap: 16px;

        .staff-detail {
            flex: 1;

            .staff-name {
                font-size: 14px;
                font-weight: 500;
                color: #323233;
                margin-bottom: 10px;
            }

            .staff-phone {
                font-size: 12px;
                color: #646566;
                margin-bottom: 4px;
            }

            .order-info {
                font-size: 12px;
                color: #969799;
                display: flex;
                align-items: center;
            }
        }
    }
}

.photo-section {
    margin-bottom: 16px;

    &:last-child {
        margin-bottom: 0;
    }

    .photo-title {
        font-size: 14px;
        font-weight: 500;
        color: #646566;
        margin-bottom: 12px;
        padding-left: 8px;
        border-left: 3px solid #409eff;
    }

    .photo-list {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;

        .photo-item {
            width: 120px;
            height: 120px;
            border-radius: 6px;
            overflow: hidden;
            cursor: pointer;
            border: 1px solid #ebeef5;
            transition: all 0.3s;

            &:hover {
                border-color: #409eff;
                box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
            }
        }

        .image-error {
            width: 100%;
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
            background: #f5f7fa;
            color: #c0c4cc;
            font-size: 24px;
        }
    }
}

.ml10 {
    margin-left: 10px;
}
</style>

