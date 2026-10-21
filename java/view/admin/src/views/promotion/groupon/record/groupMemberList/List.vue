<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" key="username" :loading="loading">
                            <el-table-column :label="$t('客户昵称')" prop="username" width="200">
                                <template #default="{ row }">
                                    <el-space>
                                        <el-avatar :size="30" :src="avatarFormat(row.avatar)" shape="circle" />
                                        <div class="name-wrap">
                                            <div class="name">{{ row.username }}</div>
                                        </div>
                                        <div v-if="row.joinType === 1" class="group-leader">{{ $t('团长') }}</div>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('身份')" align="center">
                                <template #default="{ row }">
                                    <div>{{ row.userId === 0 ? $t('虚拟团员') : (row.joinType === 1 ? $t('团长') : $t('团员')) }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('参与组织')" prop="shopTitle"></el-table-column>
                            <el-table-column :label="$t('参团时间')" prop="addTime" min-width="160">
                                <template #default="{ row }">
                                    {{ formatTimestamp(row.addTime) }}
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { imageFormat, avatarFormat } from "@/utils/format";
import type { GrouponRecordGroupMemberFilterState } from "@/types/promotion/groupon";
import { getGrouponRecordGroupMemberList } from "@/api/promotion/groupon";
import { formatTimestamp } from "@/utils/time";
import { ref } from "vue";
import { message } from "ant-design-vue";
const props = defineProps<{
    id?: number;
}>()
const loading = ref(true);
const filterState = ref<GrouponRecordGroupMemberFilterState[]>([]);
const loadFilter = async () => {
    try {
        const result = await getGrouponRecordGroupMemberList({
            grouponPromotionRecordId: props.id,
        });
        console.log(result);
        filterState.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
    finally {
        loading.value = false;
    }
}

// 初始化加载
loadFilter();

</script>
<style lang="less" scoped>
.bonus-wrap .card-wrap {
    display: -ms-flexbox;
    display: flex;
    margin-bottom: 24px;
    -ms-flex-pack: start;
    justify-content: flex-start;
    overflow-x: auto;
    overflow-y: hidden;
    z-index: -1;
}

.bonus-wrap .card-wrap__container {
    display: -ms-flexbox;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    flex: 1;
}

.bonus-wrap .item-bg {
    position: relative;
    white-space: nowrap;
    padding: 20px 27px 17px;
    display: -ms-flexbox;
    display: flex;
    -ms-flex-direction: column;
    flex-direction: column;
    background: rgba(51, 136, 255, 0.07);
    margin-right: 15px;
    text-align: center;
    -ms-flex-align: center;
    align-items: center;
    -ms-flex-pack: justify;
    justify-content: space-between;
    flex: 1;
}

.bonus-wrap .item-bg:last-child {
    margin-right: 0;
    /* 最后一个元素不需要右边距 */
}

.bonus-wrap .coupon-cards-item {
    flex: 1;
    min-width: 402px;
}

.bonus-wrap .coupon-cards-item .title {
    font-size: 16px;
    color: #333;
}

.bonus-wrap .coupon-cards-item .desc {
    padding-top: 10px;
    line-height: 18px;
    font-size: 12px;
    color: #666;
}

.bonus-wrap .coupon-cards-item button {
    margin-top: 15px;
    padding: 0 16px;
}

.bonus-wrap .subtitle {
    font-weight: 500;
    font-size: 14px;
    color: #323233;
    line-height: 20px;
    margin-bottom: 16px;
}

.bonus-wrap .item-bg2 {
    background: #fff;
    border: 1px solid #ebedf0;
    box-sizing: border-box;
}

.group-leader {
    background-color: rgba(65, 108, 234, 0.05);
    width: 35px;
    height: 22px;
    border: 1px solid #416CEA;
    line-height: 21px;
    text-align: center;
    color: #416CEA;
    font-size: 12px;
    border-radius: 2px;
    box-sizing: border-box;
}
</style>
