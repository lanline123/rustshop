<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" name="keyword" :placeholder="$t('输入标签名称')">
                                        <template #append>
                                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                        </template>
                                    </TigInput>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="tips-list">
                    <div class="list-box">
                        <div class="item" v-for="item in filterState" :key="item.tipId" :class="{ active: selectedIds.some((val) => val.tipId == item.tipId) }" @click="onSelect(item)">
                            {{ item.tipName }}
                            <div class="triangle" v-if="selectedIds.some((val) => val.tipId == item.tipId)"></div>
                        </div>
                    </div>
                </div>
                <div class="selected-list">
                    <div class="title">
                        <span>{{ $t('已选标签({length})', { length: selectedIds.length }) }}</span>
                        <div class="tips">{{ $t('至多支持选择6个标签') }}</div>
                    </div>
                    <div class="tips-list">
                        <div class="list-box">
                            <div class="item" v-for="item in selectedIds" :key="item.tipId">
                                {{ item.tipName }}
                                <i class="iconfont-admin icon-cha1" @click="onRemove(item)"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import type { Record } from "@/types/store/storeTipManage";
import { selectAllTip } from "@/api/store/storeTipManage";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    // 已选择的项，用于排除重复，禁止选择
    selectedIds: {
        type: Array as () => Record[],
        default: []
    }
});

const emit = defineEmits(["submitCallback", "okType"]);
// 基本参数定义
const filterState = ref<Record[]>([]);
const loading = ref<boolean>(true);
const selectedIds = ref<Record[]>(props.selectedIds);
const filterParams = reactive<any>({
    keyword: ""
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await selectAllTip(filterParams);
        filterState.value = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};


const onSelect = (item: any) => {
    if (selectedIds.value.some((val) => val.tipId === item.tipId)) {
        selectedIds.value = selectedIds.value.filter((val) => val.tipId !== item.tipId);
        return;
    }
    if (selectedIds.value.length >= 6) {
        message.warning(t('最多支持选择6个标签'));
        return;
    }
    selectedIds.value.push(item);
};

const onRemove = (item: any) => {
    const index = selectedIds.value.findIndex((val) => val.tipId === item.tipId);
    selectedIds.value.splice(index, 1);
};

// 弹窗回调
const onFormSubmit = () => {
    // 弹窗确认按钮提交
    emit("submitCallback", selectedIds);
};
defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.tips-list {
    border: 1px solid #e8e8e8;
    min-height: 250px;
}
.list-box {
    padding: 15px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    position: relative;
    .item {
        background-color: #f0f0f0;
        padding: 3px 8px;
        border-radius: 3px;
        display: flex;
        align-items: center;
        cursor: pointer;
        border: 1px solid #f0f0f0;
        position: relative;
        overflow: hidden;
        i {
            font-size: 10px;
            color: #9598a6;
            margin-left: 5px;
            font-weight: bold;
            margin-top: 3px;
        }
    }
    .active {
        border-color: var(--tig-primary);
        background-color: var(--tig-item-active-bg);
    }
    .triangle {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 0;
        height: 0;
        border-left: 16px solid transparent;
        border-bottom: 16px solid var(--el-color-primary);
        border-bottom-right-radius: 0px;
    }

    .triangle::after {
        content: "\2713";
        position: absolute;
        right: 1px;
        color: white;
        z-index: 2;
        top: 11px;
        transform: translateY(-50%);
        font-size: 10px;
    }
}
.selected-list {
    .title {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        span {
            font-weight: bold;
        }
        .tips {
            color: #9598a6;
            margin-left: 5px;
        }
    }
}
</style>
