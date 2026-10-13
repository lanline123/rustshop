<template>
    <div class="order-area" :style="{ height: preview ? 'calc(100vh - 153px)' : 'calc(100vh - 50px)' }">
        <div class="top-bar">
            <div @click="checkTabs(1)" :class="{ tab__actived: currentTabs == 1 }" class="card">{{ $t('全部') }}</div>
            <div @click="checkTabs(2)" :class="{ tab__actived: currentTabs == 2 }" class="card">{{ $t('未完成') }}</div>
            <div @click="checkTabs(3)" :class="{ tab__actived: currentTabs == 3 }" class="card">{{ $t('已完成') }}</div>
            <div @click="checkTabs(4)" :class="{ tab__actived: currentTabs == 4 }" class="card">{{ $t('已关闭') }}</div>
        </div>
        <div class="info-bar">
            <!--            <div >-->
            <template v-if="!loading">
                <template v-if="total > 0">
                    <div v-for="item in filterState" class="order-card">
                        <div class="order-history-m_header_2AO48">
                            <div class="order-history-m_header-line_3zlQJ">
                                <label>{{ $t('订单编号：') }}</label>
                                <div class="order-history-m_content_2mSu8">{{ item.orderSn }}</div>
                            </div>
                            <div class="order-history-m_header-line_3zlQJ">
                                <label>{{ $t('下单时间：') }}</label>
                                <div class="order-history-m_content_2mSu8 order-history-m_time_1Pp-t">{{ item.addTime }}</div>
                                <div class="order-history-m_status_fLMU8">{{ item.orderStatusName }}</div>
                            </div>
                        </div>
                        <DialogForm
                            :params="{ act: 'detail', id: item.orderId }"
                            :showClose="false"
                            :showOnOk="false"
                            :title="$t('订单详情 ') + item.orderSn"
                            isDrawer
                            path="order/order/Info"
                            width="880px"
                        >
                            <a class="order-history-m_goods-item_216A0" @click="toPage(item)" target="_blank">
                                <div style="border: 1px solid #f5f5f5" class="order-history-m_goods-pic_3wo1h">
                                    <img width="100%" :src="imageFormat(item.items?.[0]?.picThumb)" />
                                </div>
                                <div class="order-history-m_goods-detail_1XBpg">
                                    <div class="order-history-m_goods-name_2i4Hj">{{ item.items[0].productName }}</div>
                                </div>
                            </a>
                        </DialogForm>
                        <div class="order-history-m_summary_2gJrv">{{ $t('共{length}件商品，实付{priceFormat}', { length: item.items.length, priceFormat: priceFormat(item.totalAmount) }) }}</div>
                        <div class="order-history-m_bottom_2g40I">
                            <div class="order-history-m_orderOperationArea_o0OOz">
                                <div class="zent-popover-wrapper zent-pop-wrapper" style="display: inline-block">
                                    <DialogForm
                                        :params="{ act: 'detail', id: item.orderId, valueName: 'adminNote' }"
                                        isDrawer
                                        path="order/order/src/EditRemark"
                                        :title="$t('编辑备注')"
                                        width="600px"
                                    >
                                        <a>{{ $t('备注') }}</a>
                                    </DialogForm>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
                <template v-else>
                    <el-empty :description="$t('暂无订单～')" />
                </template>
            </template>
            <!--            </div>-->
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
        </div>
    </div>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { getOrderList } from "@/api/order/order";
import { message } from "ant-design-vue";
import { OrderFilterParams, OrderFilterState } from "@/types/order/order";
import { imageFormat, priceFormat } from "@/utils/format";
import { DialogForm } from "@/components/dialog";

const props = defineProps({
    userId: {
        type: Number,
        default: 0
    },
    preview: {
        type: Boolean,
        default: false
    }
});
const currentTabs = ref(1);
const checkTabs = (val: number) => {
    currentTabs.value = val;
    if (val == 1) {
        filterParams.orderStatus = -1;
    } else if (val == 2) {
        filterParams.orderStatus = [0, 1, 2];
    } else if (val == 3) {
        filterParams.orderStatus = 5;
    } else if (val == 4) {
        filterParams.orderStatus = [3, 4, -2];
    }
    loadFilter();
};
const loading = ref(false);
const filterParams = reactive<OrderFilterParams>({
    page: 1,
    size: 9999,
    orderStatus: -1,
    sortField: "addTime",
    sortOrder: "desc",
    userId: props.userId
});
const total = ref<number>(0);
const filterState = ref<OrderFilterState[]>([]);
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getOrderList({ ...filterParams });
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const toPage = (value: any) => {
    console.log(value);
};
onMounted(() => {
    loadFilter();
});
</script>
<style scoped lang="less">
.order-area {
    display: flex;
    width: 100%;
    flex-direction: column;

    .top-bar {
        background-color: #f5f5f5;
        height: 35px;
        display: flex;
        flex-direction: row;
        color: #999;
        line-height: 35px;
        font-size: 12px;

        .card {
            flex: 1;
            text-align: center;
            cursor: pointer;
        }

        .tab__actived {
            color: #3773da;
        }
    }

    .info-bar {
        display: flex;
        flex-direction: column;
        flex: 1 1 100%;
        overflow-y: auto;
        overflow-x: hidden;
        align-items: stretch;

        .order-card {
            border-bottom: 5px solid #f9f9f9;
            overflow-x: hidden;
            flex: 0 0 auto;

            .order-history-m_header_2AO48 {
                padding: 6px 10px 0;
                color: #646566;
                font-size: 12px;

                .order-history-m_header-line_3zlQJ {
                    display: flex;
                    align-items: stretch;
                    margin: 6px 0;
                    line-height: 20px;

                    label {
                        flex: 0 0 auto;
                        color: #969799;
                    }

                    .order-history-m_content_2mSu8 {
                        flex: 1 1 100%;
                        word-break: break-all;
                        white-space: pre-wrap;
                        overflow: hidden;
                        color: #969799;
                    }

                    .order-history-m_time_1Pp-t {
                        word-break: keep-all;
                    }

                    .order-history-m_status_fLMU8 {
                        font-size: 14px;
                        color: #ed6a0c;
                        margin-left: auto;
                        flex: 0 0 auto;
                    }
                }
            }

            .order-history-m_goods-item_216A0 {
                min-height: 60px;
                display: flex;
                margin: 10px;
                cursor: pointer;

                .order-history-m_goods-pic_3wo1h {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    flex: 0 0 60px;
                    width: 60px;
                    height: 60px;
                    background-color: #f5f5f5;
                    margin-right: 8px;
                    overflow: hidden;
                }

                .order-history-m_goods-detail_1XBpg {
                    flex: 1 1 100%;
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;

                    .order-history-m_goods-name_2i4Hj {
                        font-size: 12px;

                        word-break: normal;
                        color: #999;
                        overflow-wrap: anywhere;
                        line-height: 1.2; /* 调整行高以适应三行的高度 */
                        display: -webkit-box; /* 使用弹性盒子布局 */
                        -webkit-line-clamp: 3; /* 设置显示的行数为3 */
                        -webkit-box-orient: vertical; /* 设置垂直方向的弹性盒子 */
                        overflow: hidden;
                        text-overflow: ellipsis;
                    }
                }
            }

            .order-history-m_summary_2gJrv {
                text-align: right;
                margin: 5px 14px;
                color: #323233;
            }

            .order-history-m_bottom_2g40I {
                border-top: 1px solid #e5e5e5;
                margin: 0 14px;
                font-size: 12px;

                .order-history-m_orderOperationArea_o0OOz {
                    height: 45px;
                    display: flex;
                    align-items: center;
                    justify-content: flex-end;

                    .zent-popover-wrapper {
                        margin-left: 10px;
                    }

                    .zent-pop-wrapper {
                    }
                }
            }
        }
    }
}
</style>
