/** 会员等级管理列表（普通版） - 展示和编辑会员等级方案 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="notice-warp">
                    <p>{{ $t('提示：') }}</p>
                    <p>{{ $t('该版本为普通会员版本，普通会员版本仅提供基础功能，如需高级功能请购买Pro版本。') }}</p>
                </div>
                <div class="list-table-tool lyecs-search-warp mb10">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <DialogForm
                                            :params="{ act: 'detail' }"
                                            isDrawer
                                            path="user/levelManage/Info"
                                            :title="$t('编辑会员方案')"
                                            width="650px"
                                            @okCallback="loadFilter"
                                        >
                                            <el-button type="primary">{{ $t('编辑会员方案') }}</el-button>
                                        </DialogForm>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" row-key="id">
                            <el-table-column :label="$t('等级')" prop="level">
                                <template #default="{ row }">
                                    <div class="level_txt">vip{{ row.rankLevel }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('会员图标')" prop="level">
                                <template #default="{ row }">
                                    <el-image style="height: 20px" :src="row.rankLogo" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('会员名称')" prop="rankName"></el-table-column>
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

<script lang="ts" setup>
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { UserFilterParams, UserFilterState } from "@/types/user/levelManage.d";
import { getUserRankList } from "@/api/user/levelManage";

const config: any = useConfigStore();
// 基本参数定义
const filterState = ref(<UserFilterState[]>[]);
const rankConfig = ref<any>({});
const loading = ref<boolean>(true);
const filterParams = reactive<UserFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    rankName: ""
});
/**
 * @description 获取会员等级列表和等级配置信息
 * 普通版本仅显示基础等级功能
 */
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getUserRankList({ ...filterParams });
        filterState.value = result.userRank.records;
        rankConfig.value = result.rankConfig;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.level_txt {
    font-weight: 600;
    color: #c95a0a;
    font-size: 14px;
}
.avatar-info {
    display: flex;
    flex-direction: row;
    width: 100%;
    padding: 0 8px;
    align-items: center;
    gap: 14px;

    .info {
        display: flex;
        flex-direction: column;
    }
}

.rank-container {
    display: flex;
    align-items: center; /* 垂直居中 */

    .image {
        width: 18px;
        height: 18px;
    }

    .text {
        margin-left: 3px;
    }
}
.card_item {
    border: 1px solid #fff;
    border-radius: 3px;
    width: 56px;
    height: 30px;
    overflow: visible;
    background-repeat: no-repeat;
    background-position: 0% 0%;
    background-size: cover;
}
</style>
