/**
 * @file 专业版会员等级列表
 * @description 展示专业版会员等级体系的列表页面，包括等级信息、升级条件、会员权益和保级规则。
 * @features
 * - 会员等级列表展示（等级、图标、名称、卡面）
 * - 升级条件显示（无门槛/成长值/消费金额）
 * - 会员权益显示（折扣、积分倍数、包邮、自定义权益）
 * - 保级规则显示（永久有效/周期性重新计算）
 * - 会员数量统计
 * - 编辑会员方案入口
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp mb10">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <DialogForm
                                            :params="{ act: 'detail' }"
                                            isDrawer
                                            path="user/levelManagePro/Info"
                                            :title="$t('编辑会员方案')"
                                            width="1100px"
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
                        <el-table :data="filterState" :loading="loading" row-key="userId">
                            <el-table-column :width="100" :label="$t('等级')" prop="level">
                                <template #default="{ row }">
                                    <div class="level_txt">vip{{ row.rankLevel }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="100" :label="$t('会员图标')" prop="level">
                                <template #default="{ row }">
                                    <el-image style="height: 20px" :src="row.rankLogo" />
                                </template>
                            </el-table-column>
                            <el-table-column :width="100" :label="$t('会员名称')" prop="rankName"></el-table-column>
                            <el-table-column :width="200" :label="$t('会员卡面')" prop="card">
                                <template #default="{ row }">
                                    <div
                                        v-if="row.rankBg !== '' && row.rankCardType == 2"
                                        class="card_item"
                                        :style="{ backgroundImage: 'url(' + row.rankBg + ')' }"
                                    ></div>
                                    <div v-else class="card_item" :class="row.rankIco"></div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('升级条件')" prop="rankName">
                                <template #default="{ row }">
                                    <div class="rank-container">
                                        <div v-if="row.minGrowthPoints == 0">{{ $t('无门槛') }}</div>
                                        <div v-if="row.rankType == 1 && row.minGrowthPoints > 0" class="text">{{ row.minGrowthPoints }}{{ $t('成长值') }}</div>
                                        <div v-if="row.rankType == 2 && row.minGrowthPoints > 0" class="text">{{ $t('消费满') }}{{ row.minGrowthPoints }}{{ config.config.amountUnit }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('会员权益')" prop="isCompanyAuth">
                                <template #default="{ row }">
                                    <div v-if="row.discount != '' && Number(row.discount) > 0">{{ $t('会员折扣{discount}折', { discount: row.discount }) }}</div>
                                    <div v-if="row.rankPoint != '' && Number(row.rankPoint) > 0">{{ $t('{rankPoint}倍会员积分', { rankPoint: row.rankPoint }) }}</div>
                                    <div v-if="row.freeShipping == 1">{{ $t('享受会员包邮') }}</div>
                                    <div v-for="(item, index) in row.rights">
                                        <div v-if="item.isChecked == 1">{{ item.name }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('保级规则')" prop="userCount">
                                <template #default="{ row, $index }">
                                    <div v-if="$index === 0">{{ $t('不降级') }}</div>
                                    <div v-else>
                                        <div v-if="rankConfig.data.type == 1">{{ $t('永久有效') }}</div>
                                        <div v-if="rankConfig.data.type == 2">
                                            <span v-if="row.rankType == 1"
                                            >{{ $t('获得等级的{rankAftermonth}个月内，成长值达到{minGrowthPoints}可保级', { rankAftermonth: rankConfig.data.rankAftermonth, minGrowthPoints: row.minGrowthPoints }) }}</span
                                            >
                                            <span v-if="row.rankType == 2"
                                            >{{ $t('获得等级的') }}{{ rankConfig.data.rankAfterMonth }}{{ $t('个月内，消费满') }}{{ row.minGrowthPoints }}{{ config.config.amountUnit }}{{ $t('可保级') }}</span
                                            >
                                        </div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('会员数')" prop="userCount">
                                <template #default="{ row }">
                                    <div>{{ row.userCount }}</div>
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

<script lang="ts" setup>
import "./card.less";
import { DialogForm } from "@/components/dialog";
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { UserFilterParams, UserFilterState } from "@/types/user/levelManage.d";
import { getUserRankListPro } from "@/api/user/levelManage";
import { imageFormat } from "@/utils/format";

const config: any = useConfigStore();
/** 会员等级列表数据 */
const filterState = ref(<UserFilterState[]>[]);
/** 等级规则配置 */
const rankConfig = ref<any>({});
/** 加载状态 */
const loading = ref<boolean>(true);
/**
 * @description 查询参数
 * @property {number} page - 当前页码
 * @property {number} size - 每页数量
 * @property {string} sortField - 排序字段
 * @property {string} sortOrder - 排序方式
 * @property {string} rankName - 会员名称搜索关键词
 */
const filterParams = reactive<UserFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    rankName: ""
});
/**
 * @description 获取会员等级列表数据
 * 同时获取等级规则配置用于展示保级信息
 */
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getUserRankListPro({ ...filterParams });
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
