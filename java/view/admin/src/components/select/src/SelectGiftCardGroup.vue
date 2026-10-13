/**
 * @file 礼品卡分组选择器组件
 * @description 用于选择礼品卡分组的下拉选择器
 * @features
 * - 支持电子礼品卡和实体礼品卡分类筛选
 * - 支持单选和多选模式
 * - 自动加载礼品卡分组列表
 * - 支持回显已选礼品卡详情
 * - 根据 shopId 控制禁用状态
 */
<template>
    <el-select
        :loading="loading"
        :multiple="multiple"
        v-model="selectedValue"
        :allowClear="true"
        :fieldNames="fieldNames"
        :disabled="isDisabled"
        clearable
        filterable
        :placeholder="$t('选择礼品卡')"
        show-search
        @change="onChange"
    >
        <template #default>
            <div class="option">
                <div class="right">
                    <div class="scroll-container">
                        <el-option v-for="item in options" :key="item.cardGroupsId" :label="item.cardGroupName" :value="item.cardGroupsId">
                            {{ item.cardGroupName }}
                        </el-option>
                    </div>
                </div>
            </div>
        </template>
        <template #empty>
            <!-- <a-spin :spinning="!loaded" size="small" style="width: 100%; padding-top: 150px" /> -->
            <span v-if="loaded">{{ $t('暂无数据') }}</span>
        </template>
    </el-select>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { getGiftCardGroups, getGiftCardGroupDetail } from "@/api/promotion/giftCard";
import { GiftCardGroupItem } from "@/types/promotion/giftCard";
import { useConfigStore } from "@/store/config";

// 传值
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    multiple: { type: Boolean, default: false },
    cardType: { type: Number, default: 1 }, // 1电子礼品卡 2实体礼品卡
    disabled: { type: Boolean, default: false }, // 是否禁用选择
    productShopId: { type: Number, default: undefined } // 产品的 shopId，用于判断是否可以修改
});
const emit = defineEmits(["update:cardGroupsId", "update:cardGroupsIds", "onChange"]);
const cardGroupsId = defineModel<number>("cardGroupsId");
// 内部变量用于绑定 el-select，避免选中值不在列表中时显示数值 ID
const selectedValue = ref<number>();
const fieldNames = { label: "cardGroupName", value: "cardGroupsId" };
const loading = ref(true);
const configStore = useConfigStore();

// 计算是否禁用：如果传入了 disabled prop，或者 productShopId 与当前 shopId 不一致，则禁用
const isDisabled = computed(() => {
    if (props.disabled) {
        return true;
    }
    // 如果传入了 productShopId，需要与当前用户的 shopId 比较
    if (props.productShopId !== undefined) {
        const currentShopId = Number(localStorage.getItem("shopId")) || 0;
        // 如果产品的 shopId 与当前用户的 shopId 不一致，则禁用
        if (props.productShopId !== currentShopId) {
            return true;
        }
    }
    return false;
});
const onChange = (e: any) => {
    if (!props.multiple) {
        emit("update:cardGroupsId", e);
    } else {
        emit("update:cardGroupsIds", e);
    }
    emit("onChange", e);
};
// 第一次加载时，如果 cardGroupsId 有值，先获取详情
const loadDetailIfNeeded = async () => {
    if (cardGroupsId.value && cardGroupsId.value > 0) {
        try {
            const detail = await getGiftCardGroupDetail({ cardGroupsId: cardGroupsId.value });
            // 将详情转换为 GiftCardGroupItem 格式并添加到列表中（去重）
            // 使用传入的 cardGroupsId.value 作为 cardGroupsId，因为 detail 可能没有这个字段
            const detailItem: GiftCardGroupItem = {
                cardGroupsId: cardGroupsId.value,
                cardGroupName: detail.cardGroupName || "",
                groupType: detail.groupType || 1,
                groupTypeName: detail.groupType === 1 ? t('兑换卡') : t('储值卡'),
                cardType: detail.cardType || props.cardType,
                cardTypeName: detail.cardType === 1 ? t('电子卡') : t('实体卡'),
                imageUrl: detail.imageUrl || "",
                status: detail.status || 1,
                remark: detail.remark || "",
                addTime: "",
                updateTime: "",
                validityType: detail.validityType || 1,
                validityTypeName: detail.validityType === 1 ? t('永久有效') : detail.validityType === 2 ? t('天数') : t('固定时间'),
                validityDays: detail.validityDays,
                expireTime: detail.expireTime,
                faceAmount: detail.faceAmount,
                canDelete: detail.canDelete ?? true
            };
            // 检查是否已存在，不存在则添加
            const exists = options.value.some((item) => item.cardGroupsId === detailItem.cardGroupsId);
            if (!exists) {
                options.value.unshift(detailItem);
            }
            // 设置选中值
            selectedValue.value = detailItem.cardGroupsId;
            cardGroupsId.value = selectedValue.value;
        } catch (error: any) {
            // 如果获取详情失败，选择框展示空值
            console.warn("获取礼品卡详情失败:", error.message);
            selectedValue.value = undefined;
        }
    }
};

onMounted(async () => {
    if (cardGroupsId.value && cardGroupsId.value > 0) {
        filterParams.value.giftCardGroupsId = cardGroupsId.value;
    }
    // 先加载列表
    await loadList();
    // 如果 cardGroupsId 有值但不在列表中，尝试获取详情
    if (cardGroupsId.value && cardGroupsId.value > 0) {
        const exists = options.value.some((item) => item.cardGroupsId === cardGroupsId.value);
        if (!exists) {
            await loadDetailIfNeeded();
        }
    }
});
const loaded = ref(false);
// 实际数据
const options = ref<GiftCardGroupItem[]>([]);
// 筛选参数
const filterParams = ref<{ cardType: number; giftCardGroupsId?: number }>({
    cardType: props.cardType
});
const loadList = async () => {
    try {
        const result = await getGiftCardGroups(filterParams.value);
        options.value = result;
        // 只有当 cardGroupsId 在列表中时才赋值给 selectedValue，否则保持为空
        if (cardGroupsId.value && cardGroupsId.value > 0) {
            const exists = result.some((item) => item.cardGroupsId === cardGroupsId.value);
            selectedValue.value = exists ? cardGroupsId.value : undefined;
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loaded.value = true;
        loading.value = false;
    }
};
watch(
    () => props.cardType,
    () => {
        filterParams.value.cardType = props.cardType;
        loadList();
    }
);
// 监听 selectedValue 变化，同步更新 cardGroupsId 给父组件
watch(selectedValue, (newVal) => {
    if (newVal !== cardGroupsId.value) {
        cardGroupsId.value = newVal;
    }
});
// 监听父组件传入的 cardGroupsId 变化，同步更新 selectedValue（仅当值在列表中时）
watch(cardGroupsId, (newVal) => {
    if (newVal && newVal > 0 && options.value.length > 0) {
        const exists = options.value.some((item) => item.cardGroupsId === newVal);
        selectedValue.value = exists ? newVal : undefined;
    } else if (!newVal) {
        selectedValue.value = undefined;
    }
});
defineExpose({
    loadList,
    loaded
});
</script>
<style lang="less" scoped>
.option {
    display: flex;
    overflow: hidden;
    max-height: 590px !important;
    height: 150px;

    .right {
        overflow: auto;
        flex: 1;
    }
}
</style>
