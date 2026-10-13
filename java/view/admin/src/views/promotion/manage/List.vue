<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="row">
                <div class="col activity">
                    <div>
                        <div class="tip">{{ $t("仅统计优惠券、满减/送、限时折扣、秒杀、多人拼团、赠品 活动") }}</div>
                        <div class="tabs flex">
                            <div
                                v-for="item in promotionTypeList"
                                :class="{ active: item.value == filterParams.timeType }"
                                class="item"
                                @click="changeTimeType(item.value)"
                            >
                                <div class="num">{{ item.num }}</div>
                                <div class="txt">{{ item.label }}</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <div>
                        <h2>{{ $t("商家常用") }}</h2>
                        <el-row :gutter="16" class="app-wrapper cursor-pointer">
                            <el-col :lg="6" :md="8" :sm="12" :xs="12" class="app-wrapper-item" @click="toPage(2)">
                                <div class="item">
                                    <div class="icon-col">
                                        <i class="iconfont-admin icon-youhuiquan"></i>
                                    </div>
                                    <div>
                                        <div class="name">{{ $t("优惠券") }}</div>
                                        <div class="tip">{{ $t("向客户发放店铺优惠券") }}</div>
                                    </div>
                                </div>
                            </el-col>

                            <el-col :lg="6" :md="8" :sm="12" :xs="12" class="app-wrapper-item" @click="toPage(1)">
                                <div class="item">
                                    <div class="icon-col">
                                        <i class="iconfont-admin icon-miaosha"></i>
                                    </div>
                                    <div>
                                        <div class="name">{{ $t("限时秒杀") }}</div>
                                        <div class="tip">{{ $t("快速抢购引导顾客更多消费") }}</div>
                                    </div>
                                </div>
                            </el-col>
                            <el-col :lg="6" :md="8" :sm="12" :xs="12" class="app-wrapper-item" @click="toPage(3)">
                                <div class="item">
                                    <div class="icon-col">
                                        <p>{{ $t("减") }}</p>
                                    </div>
                                    <div>
                                        <div class="name">{{ $t("满减") }}</div>
                                        <div class="tip">{{ $t("购满一定金额或件数享受优惠") }}</div>
                                    </div>
                                </div>
                            </el-col>
                            <el-col :lg="6" :md="8" :sm="12" :xs="12" class="app-wrapper-item" @click="toPage(5)">
                                <div class="item">
                                    <div class="icon-col">
                                        <p>{{ $t("送") }}</p>
                                    </div>
                                    <div>
                                        <div class="name">{{ $t("满送") }}</div>
                                        <div class="tip">{{ $t("购满一定金额或件数享受优惠") }}</div>
                                    </div>
                                </div>
                            </el-col>

                            <el-col :lg="6" :md="8" :sm="12" :xs="12" class="app-wrapper-item" @click="toPage(6)">
                                <div class="item">
                                    <div class="icon-col">
                                        <i class="iconfont-admin icon-time-limit"></i>
                                    </div>
                                    <div>
                                        <div class="name">{{ $t("限时折扣") }}</div>
                                        <div class="tip">{{ $t("设置商品限时打折促销") }}</div>
                                    </div>
                                </div>
                            </el-col>

                            <template v-if="adminType === 'admin'">
                                <el-col :lg="6" :md="8" :sm="12" :xs="12" class="app-wrapper-item" @click="toPage(9)">
                                    <div class="item">
                                        <div class="icon-col">
                                            <i class="iconfont-admin icon-qiandao"></i>
                                        </div>
                                        <div>
                                            <div class="name">{{ $t("日历签到") }}</div>
                                            <div class="tip">{{ $t("每日签到领取积分或奖励") }}</div>
                                        </div>
                                    </div>
                                </el-col>
                            </template>

                            <el-col :lg="6" :md="8" :sm="12" :xs="12" class="app-wrapper-item" @click="toPage(10)">
                                <div class="item">
                                    <div class="icon-col">
                                        <p>{{ $t("赠") }}</p>
                                    </div>
                                    <div>
                                        <div class="name">{{ $t("赠品") }}</div>
                                        <div class="tip">{{ $t("设置赠品, 回馈客户") }}</div>
                                    </div>
                                </div>
                            </el-col>
                            <template v-if="adminType === 'admin'">
                                <el-col :lg="6" :md="8" :sm="12" :xs="12" class="app-wrapper-item" @click="toPage(8)">
                                    <div class="item">
                                        <div class="icon-col">
                                            <p>{{ $t("充") }}</p>
                                        </div>
                                        <div>
                                            <div class="name">{{ $t("余额充值") }}</div>
                                            <div class="tip">{{ $t("充值店铺余额") }}</div>
                                        </div>
                                    </div>
                                </el-col>
                            </template>
                        </el-row>
                    </div>
                </div>
                <div class="col">
                    <h2>{{ $t("活动记录") }}</h2>
                    <el-tabs type="card" @tab-change="onTabChange">
                        <el-tab-pane v-for="item in promotionStatusList" :label="item.label" :value="item.value"></el-tab-pane>
                    </el-tabs>
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="couponId">
                            <el-table-column :label="$t('活动名称')" prop="promotionName" width="300">
                                <template #default="{ row }">
                                    <div style="position: relative">
                                        <div>{{ row.promotionName || "--" }}</div>
                                    </div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('活动类型')" prop="typeText" width="300">
                                <template #default="{ row }">
                                    <div>{{ $t(row.typeText) }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('活动时间')" prop="timeText"></el-table-column>
                            <el-table-column :label="$t('状态')" width="120">
                                <template #default="{ row }">
                                    {{ activityStatusText(row) }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('操作')" width="200">
                                <template #default="{ row }">
                                    <div style="display: flex; align-items: center">
                                        <a class="btn-link" @click="toPage(row.type)">{{ $t("查看") }}</a>
                                        <el-divider direction="vertical" v-if="row.type && ![7, 8].includes(row.type)" />
                                        <DialogForm
                                            v-if="row.type == 1"
                                            :params="{ act: 'detail', id: row.relationId }"
                                            isDrawer
                                            path="promotion/seckill/Info"
                                            :title="$t('编辑限时秒杀')"
                                            width="1040px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t("编辑") }}</a>
                                        </DialogForm>
                                        <DialogForm
                                            v-if="row.type == 2"
                                            :params="{ act: 'detail', id: row.relationId }"
                                            isDrawer
                                            path="promotion/coupon/Info"
                                            :title="$t('编辑优惠券')"
                                            width="1040px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t("编辑") }}</a>
                                        </DialogForm>
                                        <!-- 优惠券操作达到 3 个及以上时，第 3 个及之后收进更多 -->
                                        <template v-if="row.type == 2">
                                            <el-divider direction="vertical" />
                                            <el-dropdown :ref="(el: any) => setMoreDropdownRef(el, row.relationId)" trigger="hover">
                                                <el-icon class="btn-link" size="12">
                                                    <MoreFilled />
                                                </el-icon>
                                                <template #dropdown>
                                                    <el-dropdown-menu>
                                                        <el-dropdown-item v-if="!isPromotionExpired(row)" @click.capture="closeMoreDropdown(row.relationId)">
                                                            <DialogForm
                                                                isDrawer
                                                                @okCallback="loadFilter"
                                                                :title="$t('优惠券发放')"
                                                                width="1000px"
                                                                path="promotion/coupon/src/Injected"
                                                                :params="{ id: row.relationId }"
                                                            >
                                                                <el-button style="width: 100%" link type="primary">{{ $t("发放") }}</el-button>
                                                            </DialogForm>
                                                        </el-dropdown-item>
                                                        <el-dropdown-item @click.capture="closeMoreDropdown(row.relationId)">
                                                            <DialogForm
                                                                :params="{ couponId: row.relationId }"
                                                                :show-on-ok="false"
                                                                :showFooter="false"
                                                                isDrawer
                                                                path="promotion/coupon/SendLog"
                                                                :title="$t('发放记录')"
                                                                width="700px"
                                                            >
                                                                <el-button style="width: 100%" link type="primary">{{ $t("发放记录") }}</el-button>
                                                            </DialogForm>
                                                        </el-dropdown-item>
                                                    </el-dropdown-menu>
                                                </template>
                                            </el-dropdown>
                                        </template>
                                        <DialogForm
                                            v-if="row.type == 6"
                                            :params="{ act: 'detail', id: row.relationId }"
                                            isDrawer
                                            path="promotion/productActivity/limitdiscount/Info"
                                            :title="$t('编辑限时折扣')"
                                            width="1040px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t("编辑") }}</a>
                                        </DialogForm>
                                        <DialogForm
                                            v-if="row.type == 3 || row.type == 5 || row.type == 4"
                                            :params="{ act: 'detail', id: row.relationId, promotionType: row.type == 4 ? 2 : row.type == 3 ? 1 : 3 }"
                                            isDrawer
                                            path="promotion/productActivity/Info"
                                            :title="$t('编辑优惠活动')"
                                            width="1040px"
                                            @okCallback="loadFilter"
                                        >
                                            <a class="btn-link">{{ $t("编辑") }}</a>
                                        </DialogForm>
                                    </div>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t("暂无数据") }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { MoreFilled } from "@element-plus/icons-vue";
import { DialogForm } from "@/components/dialog";
import { Pagination } from "@/components/list";
import { ref, reactive, onMounted } from "vue";
import { useConfigStore } from "@/store/config";
import type { PromotionFilterParams, PromotionFilterState } from "@/types/promotion/manage";
import { getPromotionList, getPromotionCount } from "@/api/promotion/manage";
import { message } from "ant-design-vue";
import { useRouter } from "vue-router";
import { getAdminType } from "@/utils/storage";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const adminType = getAdminType();

const router = useRouter();
const config: any = useConfigStore();

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

// 基本参数定义
const promotionTypeList = reactive([
    { value: 1, label: t("进行中的活动"), num: "-" },
    { value: 2, label: t("7天内到期活动"), num: "-" },
    { value: 3, label: t("未开始活动"), num: "-" }
]);
const promotionStatusList = reactive([
    { value: 0, label: t("全部") },
    { value: 1, label: t("秒杀") },
    { value: 2, label: t("优惠券") },
    { value: 6, label: t("限时折扣") },
    { value: 3, label: t("满减") },
    { value: 5, label: t("满赠") },
    { value: 7, label: t("拼团") }
]);
const filterParams = reactive<PromotionFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: "",
    timeType: 1,
    types: 0
});
const filterState = ref<PromotionFilterState[]>([]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    let type: any[] = [];
    if (filterParams.types === 0) {
        type = [];
    } else if (filterParams.types === 3) {
        type = [3, 4];
    } else {
        type = [filterParams.types];
    }
    try {
        const count = await getPromotionCount();
        (promotionTypeList[0].num as any) = count.timeType1Count;
        (promotionTypeList[1].num as any) = count.timeType2Count;
        (promotionTypeList[2].num as any) = count.timeType3Count;
        const result = await getPromotionList({ ...filterParams, type: type.join(",") });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
const changeTimeType = (e: number) => {
    filterParams.timeType = e;
    loadFilter();
};
const onTabChange = (e: number) => {
    filterParams.types = promotionStatusList[e].value;
    loadFilter();
};
const parsePromotionTime = (value: string | number | undefined): number | null => {
    if (value === undefined || value === null || value === "" || value === 0 || value === "0") {
        return null;
    }
    if (typeof value === "number") {
        return value < 1e12 ? value * 1000 : value;
    }
    const ts = new Date(value).getTime();
    return Number.isNaN(ts) ? null : ts;
};

const isPromotionExpired = (row: PromotionFilterState) => {
    const end = parsePromotionTime(row.endTime);
    if (end == null) return false;
    return end < Date.now();
};

/** 列表状态文案：进行中 / 未开始 / 过期（与优惠券列表口径一致） */
const activityStatusText = (row: PromotionFilterState) => {
    const now = Date.now();
    const start = parsePromotionTime(row.startTime);
    const end = parsePromotionTime(row.endTime);
    if (start != null && start > now) return t("未开始");
    if (end != null && now > end) return t("过期");
    return t("进行中");
};

const toPage = (type: number) => {
    switch (type) {
        case 1:
            // 秒杀
            router.push({ path: "/promotion/seckill/list" });
            break;
        case 2:
            // 优惠券
            router.push({ path: "/promotion/coupon/list" });
            break;
        case 6:
            // 限时折扣
            router.push({ path: "/promotion/limitdiscount-gift/list" });
            break;
        case 3:
            // 满减
            router.push({ path: "/promotion/full-reduction/list" });
            break;
        case 5:
            // 满赠
            router.push({ path: "/promotion/reward-gift/list" });
            break;
        case 9:
            // 签到
            router.push({ path: "/promotion/sign-in-setting/list" });
            break;
        case 7:
            // 拼团
            router.push({ path: "/promotion/groupon/list" });
            break;
        // case 7:
        //     // 充值
        //     router.push({ path: "/promotion/recharge-setting/list" });
        //     break;
        case 8:
            // 抽奖
            router.push({ path: "/promotion/lottery/index" });
            break;
        case 10:
            // 赠品
            router.push({ path: "/promotion/product-gift/list" });
            break;
    }
};
</script>
<style lang="less" scoped>
.container {
    .content_wrapper {
        .row {
            .tit-box {
                display: flex;
                align-items: center;
                justify-content: space-between;
                background-color: #f5f6fa;
                padding: 10px;
                margin-bottom: 20px;

                .tit {
                    border-left: 3px solid #155bd4;
                    padding-left: 10px;
                    font-size: 14px;
                }
            }

            .activity {
                h2 {
                    margin-bottom: 10px;
                }

                .tip {
                    font-size: 14px;
                    color: #999;
                }

                .tabs {
                    margin-top: 20px;

                    .item {
                        padding: 20px;
                        width: 240px;
                        background-color: #f7f7f7;
                        border-bottom: 3px solid #f7f7f7;
                        /* 设置边框 */
                        display: flex;
                        flex-direction: column;
                        justify-content: flex-end;
                        margin-right: 15px;
                        cursor: pointer;

                        .num {
                            font-weight: 900;
                            font-size: 28px;
                            line-height: 28px;
                            font-family: Aviner;
                        }

                        .txt {
                            font-weight: 400;
                            font-size: 14px;
                            line-height: 20px;
                            margin-top: 10px;
                        }
                    }

                    .active {
                        background: var(--tig-item-active-bg);
                        border-bottom: 3px solid var(--tig-primary);
                        /* 设置边框 */
                    }
                }
            }

            .col {
                h2 {
                    margin: 20px 0;
                }
            }

            .app-wrapper {
                display: flex;
                flex-wrap: wrap;
                margin-top: 20px;
                row-gap: 16px;

                .item {
                    display: flex;
                    align-items: center;
                    padding: 16px 20px;
                    background-color: #f7f8fa;
                    border-radius: 2px;
                    transition: background-color 0.2s ease-in-out;

                    &:hover {
                        background: #f2f2f2;
                    }
                }

                .icon-col {
                    background-color: #df4544;
                    color: #fff;
                    width: 40px;
                    height: 40px;
                    border-radius: 5px;
                    text-align: center;
                    margin-right: 10px;
                    line-height: 40px;

                    .iconfont-admin {
                        font-size: 25px;
                    }

                    p {
                        font-size: 20px;
                    }
                }

                .name {
                    font-size: 14px;
                }

                .tip {
                    color: #999;
                    font-size: 12px;
                    line-height: 16px;
                    margin-top: 4px;
                }
            }
        }
    }
}

:deep(.el-tabs--card) {
    .el-tabs__header .el-tabs__item {
        background-color: #f5f6fa;
    }

    .el-tabs__header .el-tabs__item.is-active {
        background-color: #fff;
        color: #333;
    }

    .el-tabs__header .el-tabs__nav {
        overflow: hidden;
    }
}
</style>
