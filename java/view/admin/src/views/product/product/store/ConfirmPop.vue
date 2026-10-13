<template>
    <div class="pop-row">
        <div class="title">
            <el-icon size="20"><WarningFilled /></el-icon>{{ $t('确定将该商品分配给全部{title}吗？', { title }) }}</div>
        <div class="content">
            <div class="label">{{ $t('全部{title}包括现有全部{title1}', { title, title1: title }) }}</div>
            <div class="tips">
                <el-icon size="16"><WarningFilled /></el-icon>
                <p> {{ $t('商品分配给组织后，默认为') }} <span>{{ storePostAllocationStatus ? $t('上架') : $t('下架') }}</span> {{ $t('状态') }} </p>
                <router-link to="/organize/store-settings/info">
                    <el-button type="primary" link> {{ $t('修改设置') }} </el-button>
                </router-link>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { message } from "ant-design-vue";
import { WarningFilled } from "@element-plus/icons-vue";
import router from "@/router";
import { allocationProduct } from "@/api/product/product";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "close", "update:confirmLoading"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    type: {
        type: String,
        default: "ALL"
    },
    storePostAllocationStatus: {
        type: Number,
        default: 0
    }
});
const title = computed(() => {
    return t('{typeAREA}', { typeAREA: props.type == "ALL" ? t('门店') : props.type == "AREA" ? t('区域') : "" });
});

const onSubmit = async () => {
    try {
        emit("update:confirmLoading", true);
        const result = await allocationProduct({ type: props.type, productIds: [props.id] });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

// 表单提交
const onFormSubmit = () => {
    onSubmit();
};
defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.pop-row {
    :deep(.el-icon) {
        color: var(--tig-primary) !important;
    }
    .title {
        display: flex;
        align-items: center;
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 8px;
        gap: 10px;
    }
    .content {
        padding-left: 30px;
        padding-bottom: 40px;
        .tips {
            display: flex;
            align-items: center;
            border: 1px solid var(--tig-primary);
            background-color: #f2f7ff;
            padding: 10px 15px;
            gap: 8px;
            margin-top: 5px;
            span {
                font-weight: bold;
            }
        }
    }
}
</style>
