<template>
    <div class="list-table-tool lyecs-search-warp">
        <el-form :model="localParams" name="form">
            <div class="advanced-search-warp list-table-tool-row">
                <div class="simple-form-warp">
                    <!-- 关键词搜索 -->
                    <div class="simple-form-field">
                        <div class="form-group">
                            <div class="control-container">
                                <TigInput
                                    v-model="localParams.keyword"
                                    name="keyword"
                                    :placeholder="$t('{recordTypedelivery}姓名/订单编号', { recordTypedelivery: recordType === 'delivery' ? '配送员' : '服务员' })"
                                    @keyup.enter="handleSearch"
                                    clearable
                                    @clear="handleSearch"
                                >
                                    <template #append>
                                        <el-button @click="handleSearch">
                                            <span class="iconfont icon-chakan1"></span>
                                        </el-button>
                                    </template>
                                </TigInput>
                            </div>
                        </div>
                    </div>
                    <!-- 下单时间 -->
                    <div class="simple-form-field">
                        <div class="form-group">
                            <label class="control-label"><span>{{ $t('下单时间：') }}</span></label>
                            <SelectTimeInterval
                                v-model:end-date="localParams.endDate"
                                v-model:start-date="localParams.startDate"
                                :clearable="true"
                                type="date"
                                value-format="YYYY-MM-DD"
                            />
                        </div>
                    </div>
                    <!-- 操作按钮 -->
                    <div class="simple-form-warp">
                        <div class="simple-form-field">
                            <label class="control-label"></label>
                            <div class="control-container">
                                <el-button type="primary" plain @click="handleSearch">{{ $t('搜索') }}</el-button>
                                <el-button plain @click="handleReset">{{ $t('重置') }}</el-button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import SelectTimeInterval from "@/components/select/src/SelectTimeInterval.vue";
import type { DeliveryRecordFilterParams } from "@/types/organize/deliveryRecord";
import type { ServiceRecordFilterParams } from "@/types/organize/serviceRecord";

/**
 * 记录搜索表单组件
 * 功能：提供配送记录/服务记录的搜索筛选功能
 * 检索项：关键词（配送员/服务员姓名、订单编号）、下单时间
 */

// ===== Props =====
interface Props {
    modelValue: DeliveryRecordFilterParams | ServiceRecordFilterParams;
    recordType: "delivery" | "service";
}

const props = defineProps<Props>();

// ===== Emits =====
const emit = defineEmits<{
    "update:modelValue": [value: DeliveryRecordFilterParams | ServiceRecordFilterParams];
    search: [];
    reset: [];
}>();

// ===== 响应式状态 =====
const localParams = computed({
    get: () => props.modelValue,
    set: (val) => emit("update:modelValue", val)
});

// ===== 方法 =====

/**
 * 搜索
 */
const handleSearch = () => {
    emit("search");
};

/**
 * 重置
 */
const handleReset = () => {
    emit("reset");
};
</script>

<style lang="less" scoped>
// 使用全局样式类 lyecs-search-warp / simple-form-warp 等
</style>
