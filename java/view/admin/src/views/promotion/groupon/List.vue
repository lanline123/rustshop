<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="bonus-wrap">
                <p class="subtitle">{{ $t('按使用场景快速创建') }}</p>
                <div class="card-wrap">
                    <div class="card-wrap__container">
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('普通团') }}</div>
                                <div class="desc">
                                    <p>{{ $t('用户下单开团后，邀请好友拼团，以优惠价格购买，提升店铺转化') }}</p>
                                </div>
                            </div>
                            <DialogForm isDrawer @okCallback="loadFilter" :title="$t('普通拼团创建')" width="1040px"
                                path="promotion/groupon/Info" :params="{ act: 'add', groupType: 1 }" :body-style="{
                                    padding: '24px 19px 24px 11px'
                                }">
                                <el-button type="primary">{{ $t('立即创建') }}</el-button>
                            </DialogForm>
                        </div>
                        <div class="item-bg coupon-cards-item">
                            <div>
                                <div class="title">{{ $t('阶梯团') }}</div>
                                <div class="desc">
                                    <p>{{ $t('成团人数越多价格越低，促进裂变转化') }}</p>
                                </div>
                            </div>
                            <DialogForm isDrawer @okCallback="loadFilter" :title="$t('阶梯拼团创建')" width="1040px"
                                path="promotion/groupon/Info" :params="{ act: 'add', groupType: 2 }">
                                <el-button type="primary">{{ $t('立即创建') }}</el-button>
                            </DialogForm>
                        </div>
                    </div>
                </div>
            </div>
            <div class="lyecs-table-list-warp">
                <!--                <div class="tabs-box">-->
                <!--                    <el-tabs v-model="activeKey" tab-position="top" @tab-change="onTabChange">-->
                <!--                        <el-tab-pane :name="1" label="全部"></el-tab-pane>-->
                <!--                        <el-tab-pane :name="2" label="我创建的"></el-tab-pane>-->
                <!--                        <el-tab-pane v-if="adminType === 'admin'" :name="3" label="下级创建"></el-tab-pane>-->
                <!--                    </el-tabs>-->
                <!--                </div>-->
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" name="form">
                        <div class="advanced-search-warp list-table-tool-row">
                            <div class="simple-form-warp">
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动名称：') }}</span></label>
                                        <div class="control-container">
                                            <TigInput v-model="filterParams.grouponName" name="grouponName"
                                                :placeholder="$t('输入活动名称')" @keyup.enter="onSearchSubmit" clearable
                                                @clear="onSearchSubmit">
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span
                                                            class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动状态：') }}</span></label>
                                        <div class="control-container">
                                            <el-select v-model="filterParams.status" clearable @clear="onSearchSubmit"
                                                @change="onSearchSubmit">
                                                <el-option v-for="item in statusList" :label="item.label"
                                                    :value="item.value"></el-option>
                                            </el-select>
                                        </div>
                                    </div>
                                </div>
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <label class="control-label"><span>{{ $t('活动时间：') }}</span></label>
                                        <SelectTimeInterval v-model:end-date="filterParams.endTime"
                                            v-model:start-date="filterParams.startTime" :clearable="false" type="date"
                                            value-format="YYYY-MM-DD"></SelectTimeInterval>
                                    </div>
                                </div>
                                <div class="simple-form-warp">
                                    <div class="simple-form-field">
                                        <label class="control-label"></label>
                                        <div class="control-container">
                                            <el-button type="primary" plain @click="onSearchSubmit">{{ $t('搜索') }}</el-button>
                                            <el-button plain @click="resetParams">{{ $t('重置') }}</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="grouponId" :total="total" @sort-change="onSortChange"
                            :loading="loading">
                            <el-table-column :label="$t('活动名称')" prop="name" :width="150"></el-table-column>
                            <el-table-column :label="$t('活动时间')" :width="330">
                                <template #default="{ row }">
                                    {{ $t('{startTime}至{endTime}', { startTime: row.startTime, endTime: row.endTime }) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('拼团类型')" align="center">
                                <template #default="{ row }">
                                    {{ row.groupType == 1 ? $t('普通团') : row.groupType == 2 ? $t('阶梯团') : "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('应收金额')" prop="receiveAmount" align="center"></el-table-column>
                            <el-table-column :label="$t('成团数')" prop="groupSuccessCount" align="center"></el-table-column>
                            <el-table-column :label="$t('支付单数')" prop="payOrderCount" align="center"></el-table-column>
                            <el-table-column :label="$t('活动状态')" prop="statusName"></el-table-column>
                            <!-- <el-table-column label="截止日期" prop="endTime" sortable="custom"> </el-table-column> -->
                            <!-- <el-table-column label="活动归属" prop="statusName"></el-table-column> -->
                            <el-table-column :width="isMobile ? 120 : 280" :label="$t('操作')" fixed="right">
                                <template #default="{ row }">
                                    <el-space :size="0" wrap>
                                        <DialogForm :params="{ act: 'detail', id: row.grouponPromotionId }" isDrawer
                                            @okCallback="loadFilter" :title="$t('编辑拼团活动')" width="1040px"
                                            path="promotion/groupon/Info">
                                            <a class="btn-link">{{ $t('编辑活动') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm isDrawer :title="$t('管理商品')" width="1012px" :showOnOk="false"
                                            path="promotion/groupon/item/List"
                                            :params="{ act: 'detail', grouponPromotionId: row.grouponPromotionId, groupType: row.groupType }">
                                            <a class="btn-link">{{ $t('管理商品') }}</a>
                                        </DialogForm>
                                        <template v-if="row.status !== 2">
                                            <el-divider direction="vertical" />
                                            <Advertise :title="$t('推广')" :item="row" @callBack="loadFilter">
                                                <a class="btn-link">{{ $t('投放') }}</a>
                                            </Advertise>
                                        </template>
                                        <el-divider direction="vertical" />
                                        <el-dropdown
                                            :ref="(el: any) => setMoreDropdownRef(el, row.grouponPromotionId)"
                                            trigger="hover"
                                        >
                                            <el-icon class="btn-link" size="12">
                                                <MoreFilled />
                                            </el-icon>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <el-dropdown-item @click.capture="closeMoreDropdown(row.grouponPromotionId)">
                                                        <DialogForm
                                                            :params="{ id: row.grouponPromotionId, type: 'shop' }"
                                                            :show-on-ok="false" isDrawer
                                                            path="promotion/groupon/record/List" :title="$t('参与记录')"
                                                            width="1200px" @okCallback="loadFilter">
                                                            <el-button style="width: 100%" link
                                                                type="primary">{{ $t('参与记录') }}</el-button>
                                                        </DialogForm>
                                                    </el-dropdown-item>
                                                    <DeleteRecord v-if="row.status !== 2"
                                                        :params="{ id: row.grouponPromotionId }"
                                                        :requestApi="finishGroupon"
                                                        :title="$t('结束进行中的活动会影响粉丝正常购买，建议您提前告知或说明，确定要结束活动吗？')" :message="$t('停止活动成功！')"
                                                        @afterDelete="loadFilter">
                                                        <el-dropdown-item>
                                                            <el-button style="width: 100%" link
                                                                type="primary">{{ $t('立即结束') }}</el-button>
                                                        </el-dropdown-item>
                                                    </DeleteRecord>
                                                    <DeleteRecord v-if="row.status === 2"
                                                        :params="{ id: row.grouponPromotionId }"
                                                        :requestApi="delGroupon" :title="$t('确认删除该活动？活动删除后，将不能恢复。')"
                                                        @afterDelete="loadFilter">
                                                        <el-dropdown-item>
                                                            <el-button style="width: 100%" link
                                                                type="primary">{{ $t('删除') }}</el-button>
                                                        </el-dropdown-item>
                                                    </DeleteRecord>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="showEmpty" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                            @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { MoreFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { SelectShop } from "@/components/select";
import { DeleteRecord, Pagination } from "@/components/list";
import { GrouponFilterParams, GrouponFilterState } from "@/types/promotion/groupon";
import { finishGroupon, getGrouponList, delGroupon } from "@/api/promotion/groupon";
import { useConfigStore } from "@/store/config";
import { isMerchant } from "@/utils/version";
import { computed, reactive, ref, onMounted, onUnmounted } from "vue";
import { SelectTimeInterval } from "@/components/select";
import { getAdminType } from "@/utils/storage";
import Advertise from "@/views/promotion/groupon/src/Advertise.vue";
import { message } from "ant-design-vue";
import { useUserStore } from "@/store/user";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = getAdminType();
const config = useConfigStore();
const activeKey = ref<1 | 2 | 3>(1);
const loading = ref(false);
const filterState = ref<GrouponFilterState[]>([]);
const total = ref<number>(0);
const userStore = useUserStore();
const filterParams = reactive<GrouponFilterParams>({
    sortField: "",
    sortOrder: "",
    keyword: "",
    page: 1,
    size: config.get("pageSize"),
    grouponName: "",
    endTime: "",
    startTime: "",
    status: ""
});

// 是否显示空数据
const showEmpty = computed(() => {
    return !loading.value && !filterState.value.length;
});

const isMobile = ref(document.body.clientWidth < 768);
const handleResize = () => {
    isMobile.value = document.body.clientWidth < 768;
};

onMounted(() => {
    window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
    window.removeEventListener("resize", handleResize);
});

const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getGrouponList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

// 初始化加载
loadFilter();

const moreDropdownRefs = new Map<number, any>();
const setMoreDropdownRef = (el: any, key: number) => {
    if (el) {
        moreDropdownRefs.set(key, el);
    } else {
        moreDropdownRefs.delete(key);
    }
};
const closeMoreDropdown = (key: number) => {
    moreDropdownRefs.get(key)?.handleClose?.();
};

const onTabChange = (val: 1 | 2 | 3) => {
    activeKey.value = val;
    filterParams.page = 1;
    loadFilter();
};

const onSearchSubmit = () => {
    filterParams.page = 1;
    loadFilter();
};

const resetParams = () => {
    filterParams.keyword = "";
    filterParams.status = "";
    filterParams.grouponName = "";
    filterParams.endTime = "";
    filterParams.startTime = "";
    filterParams.page = 1;
    filterParams.sortField = "";
    filterParams.sortOrder = "";
    loadFilter();
};

// 修改排序
const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == "ascending" ? "asc" : order == "descending" ? "desc" : "";
    loadFilter();
};

const statusList = reactive([
    { value: 1, label: t('活动进行中') },
    { value: 2, label: t('活动已结束') },
    { value: 3, label: t('活动未开始') }
]);

</script>
<style scoped>
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
    padding: 17px 27px 17px;
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
    height: 23px;
    line-height: 23px;
    font-size: 16px;
    color: #101010;
    /* font-family: PingFangSC-bold; */
}

.bonus-wrap .coupon-cards-item .desc {
    padding-top: 9px;
    line-height: 17px;
    font-size: 14px;
    color: rgba(164, 173, 179, 1);
    height: 23px;
}

.bonus-wrap .coupon-cards-item button {
    margin-top: 16px;
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
</style>
