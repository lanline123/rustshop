<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="bonus-wrap">
                <p class="subtitle">{{ $t('按使用场景快速创建') }}</p>
                <div class="card-wrap">
                    <div class="card-wrap__container">
                        <div v-if="!isShop" class="item-bg gift-card-item">
                            <div>
                                <div class="title">{{ $t('储值卡') }}</div>
                                <div class="desc">
                                    <p>{{ $t('例：储值卡购买可进行兑换') }}<br />{{ $t('金额直接充入账户余额中') }}</p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                @okCallback="loadFilter"
                                :title="$t('添加储值卡')"
                                width="800px"
                                path="promotion/giftCard/Info"
                                :params="{ act: 'add', groupType: 1 }"
                            >
                                <el-button type="primary">{{ $t('立即创建') }}</el-button>
                            </DialogForm>
                        </div>
                        <div class="item-bg gift-card-item">
                            <div>
                                <div class="title">{{ $t('兑换卡') }}</div>
                                <div class="desc">
                                    <p>{{ $t('例：可兑换该卡绑定的商品') }}<br />{{ $t('增强品牌与营销效果') }}</p>
                                </div>
                            </div>
                            <DialogForm
                                isDrawer
                                @okCallback="loadFilter"
                                :title="$t('添加兑换卡')"
                                width="800px"
                                path="promotion/giftCard/Info"
                                :params="{ act: 'add', groupType: 2 }"
                            >
                                <el-button type="primary">{{ $t('立即创建') }}</el-button>
                            </DialogForm>
                        </div>
                    </div>
                </div>
            </div>
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams" @submit.native.prevent="onSearchSubmit">
                        <div class="list-table-tool-row">
                            <div class="simple-form-warp">
                                <!-- <div v-if="filterParams.shopId == -1" class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <SelectShop v-model:shopId="filterParams.shopId"></SelectShop>
                                        </div>
                                    </div>
                                </div> -->
                                <div class="simple-form-field">
                                    <div class="form-group">
                                        <div class="control-container">
                                            <TigInput
                                                v-model="filterParams.keyword"
                                                name="keyword"
                                                :placeholder="$t('输入礼品卡名称')"
                                                clearable
                                                @clear="onSearchSubmit"
                                            >
                                                <template #append>
                                                    <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                                </template>
                                            </TigInput>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="cardGroupsId" :total="total" @sort-change="onSortChange" :loading="loading">
                            <el-table-column :label="$t('礼品卡名称')" prop="cardGroupName" :width="200"></el-table-column>
                            <el-table-column prop="status" :label="$t('状态')" align="center">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.status"
                                        :requestApi="updateGiftCardGroupStatus"
                                        :params="{ cardGroupsId: row.cardGroupsId }"
                                        field="status"
                                        :disabled="!row.canDelete"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('备注')" prop="remark" :width="300">
                                <template #default="{ row }">
                                    {{ row.remark || "-" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('卡类型')" prop="groupTypeName" align="center"></el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime" :min-width="150"></el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="220">
                                <template #default="{ row }">
                                    <el-space :size="0">
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadFilter"
                                            :title="$t('查看卡列表')"
                                            width="1200px"
                                            path="promotion/giftCard/giftCardList/List"
                                            :params="{
                                                act: 'detail',
                                                cardGroupsId: row.cardGroupsId,
                                                giftGroupsName: row.cardGroupName,
                                                groupType: row.groupType,
                                                cardType: row.cardType
                                            }"
                                        >
                                            <a class="btn-link">{{ $t('查看卡列表') }}</a>
                                        </DialogForm>
                                        <el-divider direction="vertical" />
                                        <DialogForm
                                            isDrawer
                                            @okCallback="loadFilter"
                                            :title="$t('编辑') + (row.groupType === 1 ? $t('储值卡') : $t('兑换卡'))"
                                            width="800px"
                                            path="promotion/giftCard/Info"
                                            :params="{ act: 'detail', id: row.cardGroupsId, groupType: row.groupType }"
                                        >
                                            <a class="btn-link">{{ $t('编辑') }}</a>
                                        </DialogForm>
                                        <el-divider v-if="row.canDelete || row.groupType === 2" direction="vertical" />
                                        <el-dropdown
                                            v-if="row.canDelete || row.groupType === 2"
                                            :ref="(el: any) => setMoreDropdownRef(el, row.cardGroupsId)"
                                            trigger="hover"
                                        >
                                            <el-icon class="btn-link" size="12">
                                                <MoreFilled />
                                            </el-icon>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <template v-if="row.groupType === 2">
                                                        <el-dropdown-item @click.capture="closeMoreDropdown(row.cardGroupsId)">
                                                            <BindProducts
                                                                :card-groups-id="row.cardGroupsId"
                                                                :products="row.giftCardProducts"
                                                                @ok-callback="loadFilter"
                                                            />
                                                        </el-dropdown-item>
                                                    </template>
                                                    <template v-if="row.canDelete">
                                                        <DeleteRecord
                                                            @afterDelete="loadFilter"
                                                            :requestApi="delGiftGroupCard"
                                                            :params="{ giftCardGroupsId: row.cardGroupsId }"
                                                            :disabled="!row.canDelete"
                                                        >
                                                            <el-dropdown-item>
                                                                <a class="btn-link">{{ $t('删除') }}</a>
                                                            </el-dropdown-item></DeleteRecord
                                                        >
                                                    </template>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </el-space>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord, Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import Switch from "./src/Switch.vue";
import BindProducts from "./src/BindProducts.vue";
import { useListRequest } from "@/hooks/useListRequest";
import type { GiftCardGroupFilterParams, GiftCardGroupItem } from "@/types/promotion/giftCard";
import { delGiftGroupCard, getGiftCardGroupList, updateGiftCardGroupStatus } from "@/api/promotion/giftCard";
import { MoreFilled } from "@element-plus/icons-vue";
import { getAdminType, getShopType } from "@/utils/storage";
import { computed } from "vue";
const config = useConfigStore();

// 是否是店铺
const isShop = computed(() => {
    return getAdminType() === "shop" && [1, 2].includes(getShopType());
});
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction,
    resetParams
} = useListRequest<GiftCardGroupItem, GiftCardGroupFilterParams>({
    apiFunction: getGiftCardGroupList,
    idKey: "cardGroupsId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        page: 1,
        size: config.get("pageSize")
    }
});

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

// 初始化加载
loadFilter();
</script>
<style lang="scss" scoped>
.bonus-wrap {
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-bottom: 15px;

    .subtitle {
        font-weight: 500;
        font-size: 14px;
        color: #101010;
        line-height: 17px;
    }

    .card-wrap {
        z-index: 0;
        .card-wrap__container {
            transition: transform 0.6s ease-in-out;
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
            .item-bg {
                position: relative;
                white-space: nowrap;
                padding: 15px 20px 17px;
                display: flex;
                flex-direction: column;
                background: var(--tig-item-active-bg);
                text-align: center;
                align-items: center;
                justify-content: space-between;
                min-width: 170px;
                width: 210px;
                margin: 0;
            }
        }
    }
}

.bonus-wrap .gift-card-item {
    .title {
        font-size: 16px;
        color: #333;
    }

    .desc {
        padding-top: 10px;
        line-height: 18px;
        font-size: 12px;
        color: #666;
    }

    button {
        margin-top: 10px;
        padding: 0 16px;
    }
}
</style>
