<!--
 * @file 店铺端 客户详情抽屉（D1116-shopMember，2026-07-08 对齐后端接口文档）
 * @description GET user/shopMember/detail?userId=（shopId 取登录店铺）。字段同列表单条。
 *   由客户管理列表「详情」打开。脱敏由后端服务端完成，前端直接展示。
-->
<template>
    <a-spin :spinning="loading">
        <div class="customer-detail">
            <div class="user-head">
                <el-avatar :size="48" :src="avatarFormat(info.avatar)">
                    <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <div class="user-head-meta">
                    <div class="name">{{ info.nickname || "--" }}</div>
                    <div class="mobile">{{ info.mobile || "--" }}</div>
                </div>
            </div>
            <div class="basic-info-list">
                <div class="info-item">
                    <div class="label">客户ID：</div>
                    <div class="value">{{ info.userId || userId || "--" }}</div>
                </div>
                <div class="info-item">
                    <div class="label">累计消费：</div>
                    <div class="value">{{ priceFormat(Number(info.orderAmount) || 0) }}</div>
                </div>
                <div class="info-item">
                    <div class="label">订单数：</div>
                    <div class="value">{{ info.orderCount ?? 0 }}</div>
                </div>
                <div class="info-item">
                    <div class="label">入会时间：</div>
                    <div class="value">{{ info.joinTime ? timestampToDate(info.joinTime) : "--" }}</div>
                </div>
                <div class="info-item">
                    <div class="label">最近消费：</div>
                    <div class="value">{{ info.lastConsumeTime ? timestampToDate(info.lastConsumeTime) : "--" }}</div>
                </div>
            </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import { onMounted, ref } from "vue";
import { message } from "ant-design-vue";
import { avatarFormat, priceFormat } from "@/utils/format";
import { timestampToDate } from "@/utils/time";
import { UserFilled } from "@element-plus/icons-vue";
import { getShopCustomerDetail } from "@/api/merchant/shopMember";
import type { ShopCustomerItem } from "@/types/merchant/shopMember.d";

const props = defineProps({
    userId: { type: Number, default: 0 },
    nickname: { type: String, default: "" },
    isDialog: Boolean
});

const loading = ref<boolean>(true);
const info = ref<ShopCustomerItem>({ userId: props.userId, nickname: props.nickname, mobile: "" });

const loadDetail = async () => {
    try {
        const result = await getShopCustomerDetail({ userId: props.userId });
        if (result) info.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadDetail();
});
</script>

<style lang="less" scoped>
.customer-detail {
    padding: 8px 0;
}
.user-head {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-bottom: 16px;
    margin-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    .user-head-meta {
        .name {
            font-size: 15px;
            font-weight: 500;
        }
        .mobile {
            color: #999;
            font-size: 13px;
            margin-top: 4px;
        }
    }
}
.basic-info-list {
    .info-item {
        display: flex;
        align-items: center;
        margin-bottom: 14px;
        font-size: 14px;
        gap: 10px;
        .label {
            width: 90px;
            font-weight: 500;
            text-align: right;
            color: #666;
            flex-shrink: 0;
        }
        .value {
            flex: 1;
            word-break: break-all;
        }
    }
}
</style>
