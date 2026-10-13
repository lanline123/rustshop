<template>
    <div class="container">
        <div class="lyecs-table-list-warp" style="margin-bottom: 0">
            <div class="list-table-tool lyecs-search-warp">
                <el-form :model="filterParams">
                    <div class="advanced-search-warp list-table-tool-row" style="margin-bottom: -20px">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <label class="control-label"><span>{{ $t('搜索客服：') }}</span></label>
                                    <div class="control-container">
                                        <TigInput v-model="filterParams.username"></TigInput>
                                    </div>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <el-button type="primary" @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                            </div>
                        </div>
                    </div>
                </el-form>
            </div>
            <div class="table-container">
                <a-spin :spinning="loading">
                    <div class="table-list">
                        <div @click="selectedChange(item?.user?.adminId, index)" class="table-item" v-for="(item,index) in filterState" :key="index">
                            <div class="zent-badge">
                                <template v-if="extractContent(String(item.user?.avatar ?? ''))=='def'">
                                    <el-avatar class="zent-avatar-image" :src="returnAvatar(item.user?.avatar)" />
                                </template>
                                <template v-else>
                                    <el-avatar class="zent-avatar-image" :src="imageFormat(item.user?.avatar)" />
                                </template>
                                <StatusDot :flicker="item.status==1" :size="10" :color="item.status==1?'#3ebd00':(item.status==2?'#f44444':'#d0d0d0')" class="dot"></StatusDot>
                            </div>
                            <div class="servant-select-dialog-m_item-content_3qFlV">
                                <div class="servant-select-dialog-m_item-name_1Cf5B">{{ item.user?.username }}</div>
                                <div class="servant-select-dialog-m_item-count_2JOT0">
                                    {{ $t('当前接待人数：{conversationNum}', { conversationNum: item.conversationNum }) }}
                                </div>
                            </div>
                            <div class="zenticon-check-circle">
<!--                                <CheckIcon @selected-change="selectedChange(index)" v-model:selected="item.selected" />-->
                                <CheckIcon  v-model:selected="item.selected" />
                            </div>
                        </div>

                    </div>
                </a-spin>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>

import { computed, onMounted, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { getWaitServantList } from "@/api/im/conversation";
import { extractContent } from "@/utils/util";
import { returnAvatar } from "@/utils/avatar";
import { imageFormat } from "@/utils/format";
import { getAdminUserList } from "@/api/authority/adminUser";
import StatusDot from "@/components/form/src/StatusDot.vue";
import { getTransferList } from "@/api/im/servant";
import CheckIcon from "@/components/form/src/CheckIcon.vue";
const userInfo = computed(() => JSON.parse(localStorage.getItem('user') || '{}')?.userInfo);
const props = defineProps({
    conversation_id: {
        type: Number,
        default: 0
    }
})
const servantId = defineModel<Number>("servantId");
// 基本参数定义
const filterState = ref<any[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const filterParams = reactive({
    //初使化用于查询的参数
    page: 1,
    size: 9999,
    username: ""
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getTransferList({ ...filterParams });
        filterState.value = (result?.records || []).map((item: any) => ({
            ...item,
            selected: false
        }));
        total.value = result?.total || 0;
        prioritizeAdminId();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

function prioritizeAdminId() {
    const adminId = userInfo.value?.adminId;
    if (adminId == null) return filterState.value;

    const match = filterState.value.find((item: any) => item.user?.adminId === adminId);

    if (match) {
        const index = filterState.value.indexOf(match);
        filterState.value.splice(index, 1);
        filterState.value.unshift(match);
    }

    return filterState.value;
}

onMounted(() => {
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter();
};
// 定义当前选中的项的 ID
const selectedItemId = ref<number>(0);

// 监听 selected 的变化，实现单选功能
const selectedChange = (itemId: number, index: number) => {
    if (itemId === userInfo.value?.adminId) return;
    const row = filterState.value[index];
    if (!row) return;
    let temp = row.selected;
    filterState.value.forEach((item: any) => {
        item.selected = false;
    });
    selectedItemId.value = itemId;
    row.selected = !temp;
    servantId.value = row.servantId;
};

defineExpose({
    loadFilter
});
</script>
<style lang="less" scoped>
.batch {
    margin-top: 20px;
}

.kehu {
    display: flex;
    align-items: center;
    gap: 8px;

    .av {
        min-width: 40px;
        min-height: 40px;
    }

    .title {
        /* 占满剩余宽度 */
        flex-grow: 1;
        /* 文字不换行 */
        white-space: nowrap;
        /* 超出部分隐藏 */
        overflow: hidden;
        /* 超出部分显示为省略号 */
        text-overflow: ellipsis;
    }
}

.table-list {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .table-item {
        display: flex;
        align-items: center;
        height: 60px;
        box-sizing: border-box;
        padding: 0 20px;
        cursor: pointer;

        .zent-badge {
            position: relative;
            display: inline-block;
            vertical-align: middle;
            flex: 0 0;
            margin-right: 10px;

            .zent-avatar-image {
                width: 40px;
                height: 40px;
                line-height: 40px;
                border-radius: 50%;
            }

            .dot {
                position: absolute;
                top: 0;
                right: 0;
            }
        }

        .servant-select-dialog-m_item-content_3qFlV {
            flex: 1 1 100%;
            display: flex;

            .servant-select-dialog-m_item-name_1Cf5B {
                color: #222;
                flex: 1;
            }

            .servant-select-dialog-m_item-count_2JOT0 {
                font-size: 12px;
                color: #999999;
                font-weight: 200;
                line-height: 20px;
            }
        }

        .zenticon-check-circle {
            margin-left: 10px;
        }
    }

    .table-item:hover {
        background-color: #f2f2f2;
    }
}
</style>
