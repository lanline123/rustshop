<template>
    <div class="content">
        <!-- 操作区域 -->
        <div class="no-print">
            <el-button type="primary" @click="print">{{ $t('打印') }}</el-button>
            <div class="select-row flex flex-align-center"> {{ $t('打印方式：') }} <el-select v-model="printMode" :placeholder="$t('打印模式')" style="min-width: 100px; width: 100px">
                    <el-option :label="$t('一页一单')" value="one"></el-option>
                    <el-option :label="$t('一页多单')" value="multiple"></el-option>
                </el-select>
            </div>
        </div>
        <div ref="printContent" class="print-container">
            <div>
                <div v-for="order in formState" :key="order.orderSn" :class="printMode">
                    <OrderTemplate :printMode="printMode" :order="order" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";
import { ref, nextTick } from "vue";
import OrderTemplate from "./src/OrderTemplate.vue";
import { OrderPrintFormResult } from "@/types/order/order.d";
import { getOrderPrint } from "@/api/order/order";
const query = useRouter().currentRoute.value.query;
// 基本参数定义
const loading = ref<boolean>(true);
const formState = ref<OrderPrintFormResult[]>([]);
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getOrderPrint({ ids: query.ids });
        formState.value = result;
    } catch (error: any) {
        // message.error(error.message);
    } finally {
        loading.value = false;
    }
};
loadFilter();
const props = defineProps({
    orders: Array // 订单数据 [{ id: 1, ... }, ...]
});

const printMode = ref("one");
const printContent = ref(null);

// 打印功能
const print = async () => {
    await nextTick(); // 确保 Vue 完成分页后的 DOM 更新
    // 动态注入打印样式
    const printStyle = document.createElement("style");
    printStyle.type = "text/css";
    printStyle.media = "print"; // 只在打印时生效
    document.head.appendChild(printStyle);

    // 在当前页面打印
    window.print();

    // 打印完成后移除样式（可选）
    setTimeout(() => {
        printStyle.remove();
    }, 500);
};
</script>
<style>
body {
    background-color: #e9ecf0;
}
</style>
<style scoped lang="less">
/* 隐藏非打印元素 */
@media print {
    .no-print {
        display: none !important;
    }
    /* 打印容器填满页面 */
    .content {
        width: 100vw !important;
        margin: auto !important;
    }
}
body {
    .content {
        width: 65vw;
        margin: auto;
        .no-print {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 30px 0;
        }

        .print-container {
            .one {
                margin-bottom: 10px;
                page-break-after: always;
                &:last-child {
                    page-break-after: avoid !important;
                }
            }

            .multiple {
                border-bottom: 10px solid #ffffff;
            }

            /* 订单内容样式 */
            :deep(.order-template) {
                font-family: "Microsoft YaHei", sans-serif;
                font-size: 12pt;
                background-color: #fff;
                padding: 20px;
                font-size: 12px;

                .tips {
                    padding-top: 40px;
                    text-align: center;
                }

                .title-row {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 10px 0;
                    font-size: 14px;

                    .order-sn {
                        font-weight: bold;
                    }
                }

                .user-info {
                    border: 1px dashed #000;
                    padding: 20px;
                    margin-top: 10px;
                    margin-bottom: 10px;
                    display: flex;
                    flex-wrap: wrap;
                    gap: 15px;

                    .item {
                        display: flex;
                        width: 45%;
                    }
                }

                table {
                    width: 100%;
                    border-color: #000 !important;
                    border-collapse: collapse;
                    text-align: center;

                    .order-info {
                        display: flex;
                        flex-wrap: wrap;
                        padding: 10px;

                        .item {
                            display: flex;
                            line-height: 25px;
                            width: 28%;
                        }
                    }

                    tr,
                    td {
                        height: 40px;
                        line-height: 40px;
                        border-color: #000 !important;
                        padding: 5px;
                    }

                    td {
                        &:nth-child(2) {
                            width: 15%;
                        }

                        &:nth-child(3) {
                            width: 15%;
                        }
                    }
                }
            }
        }
    }
}
@media (max-width: 768px) {
    .content {
        width: 90vw !important;
    }
}
</style>
