<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <template v-if="!loading">
                    <div class="letf-wrap">
                        <el-form label-width="auto">
                            <template v-for="(item, index) in formState.template" :key="index">
                                <el-form-item :label="item.title">
                                    <template v-for="(op, i) in item.options" :key="i">
                                        <el-checkbox :label="op.chooseTitle" v-model="op.choose" :disabled="disabledList.includes(op.chooseTitle as string)" />
                                        <TigInput v-if="item.title == '底部公告'" classType="tig-form-input" v-model="op.value" :row="3"
                                        type="textarea" />
                                    </template>
                                </el-form-item>
                            </template>
                        </el-form>
                    </div>
                    <div class="right-wrap">
                        <div class="tabs">
                            <div
                                class="tab"
                                :class="{ active: formState.paperSize === 58 }"
                                @click="formState.paperSize = 58"
                            >58mm</div>
                            <div
                                class="tab"
                                :class="{ active: formState.paperSize === 80 }"
                                @click="formState.paperSize = 80"
                            >80mm</div>
                        </div>
                        <div class="receipt-preview" :style="receiptPreviewStyle">
                            <div class="line"></div>
                            <el-scrollbar height="100%" class="scrollbar" always>
                                <div class="receipt-content">
                                    <template v-for="(item, index) in formState.template" :key="index">
                                        <div class="wrap head-wrap" v-if="item.title == '小票头部'">
                                            <template v-for="(op, i) in item.options" :key="i">
                                                <div class="txt" :class="`op-${i}`" v-if="op.choose">
                                                    <div class="label">TigShop</div>
                                                </div>
                                            </template>
                                        </div>
                                        <div class="wrap delivery-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item.title == '配送信息'">
                                            <div class="txt op-1" v-if="item?.options?.[0].choose">
                                                <div class="label">{{ $t('配送方式:') }}</div>
                                                <div class="value">{{ $t('普通快递') }}</div>
                                            </div>
                                            <div class="txt op-1" v-if="item?.options?.[1].choose">
                                                <div class="label">{{ $t('收货信息:') }}</div>
                                                <div class="value">{{ $t('收货人姓名，{choose131000013100000000}，南昌市青山湖区世纪大道1888号B座202室', { choose131000013100000000: item?.options?.[2].choose ? '131****0000': '13100000000' }) }}</div>
                                            </div>
                                        </div>
                                        <div class="wrap remarks-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item.title == '买家备注'">
                                            <template v-for="(op, i) in item.options" :key="i">
                                                <div class="txt" :class="`op-${i}`" v-if="op.choose">
                                                    <div class="label">{{ $t('买家备注:') }}</div>
                                                    <div class="value">{{ $t('买家备注信息买家备注信息买家备注信息买家备注信息买家备注信息买家备注信息') }}</div>
                                                </div>
                                            </template>
                                        </div>
                                        <template v-if="item.title == '商品信息'">
                                            <div class="wrap goods-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item?.options?.[0].choose">
                                                <div class="title">{{ $t('商品') }}</div>
                                                <div class="table">
                                                    <div class="item">{{ $t('单价') }}</div>
                                                    <div class="item">{{ $t('数量') }}</div>
                                                    <div class="item">{{ $t('小计') }}</div>
                                                </div>
                                        </div>
                                        <div class="wrap goods-wrap":class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item?.options?.[0].choose">
                                                <div class="content">
                                                    <div class="good-name">{{ $t('商品名称商品名称商品名称商品名称商品名称商品名称（规格1/规格2）') }}</div>
                                                    <div class="sn"v-if="item?.options?.[1].choose">{{ $t('商品编码：123-456-787634566') }}</div>
                                                    <div class="table">
                                                        <div class="item">{{symbol}}100</div>
                                                        <div class="item">2</div>
                                                        <div class="item">{{symbol}}200</div>
                                                    </div>
                                                </div>
                                                <div class="content">
                                                    <div class="good-name">{{ $t('【会员价】商品名称商品名称商品名称商品名称商品名称商品名称') }}</div>
                                                    <div class="sn" v-if="item?.options?.[1].choose">{{ $t('商品编码：123-456-787634566') }}</div>
                                                    <div class="table">
                                                        <div class="item">￥100</div>
                                                        <div class="item">1</div>
                                                        <div class="item">￥100</div>
                                                    </div>
                                                </div>
                                        </div>
                                        <div class="wrap goods-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item?.options?.[0].choose">
                                            <div class="total">
                                                <div class="num">{{ $t('共3件') }}</div>
                                                <div class="price">{{ $t('合计：{symbol}300.00', { symbol }) }}</div>
                                            </div>
                                        </div>
                                        </template>
                                        <div class="wrap freight-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item.title == '运费信息'">
                                            <template v-for="(op, i) in item.options" :key="i">
                                                <div class="txt" v-if="op.choose">
                                                    <div class="label">{{ $t('运费') }}</div>
                                                    <div class="value">{{symbol}}0.00</div>
                                                </div>
                                            </template>
                                        </div>
                                        <div class="wrap discounts-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item.title == '优惠信息'">
                                            <div class="txt">
                                                <div class="label">{{ $t('优惠') }}</div>
                                                <div class="value">
                                                    <div class="detail" v-if="item?.options?.[0].choose">
                                                        <div class="item">{{ $t('优惠券：-{symbol}1.00', { symbol }) }}</div>
                                                        <div class="item">{{ $t('积分抵扣：-{symbol}3.00', { symbol }) }}</div>
                                                        <div class="item">{{ $t('活动优惠：-{symbol}20.00', { symbol }) }}</div>
                                                    </div>
                                                    <div class="total" v-if="item?.options?.[1].choose">{{ $t('总计：-{symbol}24.00', { symbol }) }}</div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="wrap pay-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item.title == '支付信息'">
                                            <div class="txt op-1" v-if="item?.options?.[1].choose">
                                                <div class="label">{{ $t('支付方式:') }}</div>
                                                <div class="value">{{ $t('余额支付') }}</div>
                                            </div>
                                            <div class="txt op-1" v-if="item?.options?.[0].choose">
                                                <div class="label">{{ $t('实收金额:') }}</div>
                                                <div class="value">{{symbol}}276.00</div>
                                            </div>
                                            <div class="txt op-1" v-if="item?.options?.[2].choose">
                                                <div class="label">{{ $t('第三方支付单号:') }}</div>
                                                <div class="value">123456789012345678</div>
                                            </div>
                                        </div>
                                        <div class="wrap customer-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item.title == '客户信息'">
                                            <div class="txt op-1" v-if="item?.options?.[0].choose">
                                                <div class="label">{{ $t('客户姓名:') }}</div>
                                                <div class="value">132*****123</div>
                                            </div>
                                            <div class="txt op-1" v-if="item?.options?.[0].choose">
                                                <div class="label">{{ $t('手机号:') }}</div>
                                                <div class="value">132*****123</div>
                                            </div>
                                            <!-- <div class="txt op-1" v-if="item?.options?.[0].choose">
                                                <div class="label">积分:</div>
                                                <div class="value">123456</div>
                                            </div>
                                            <div class="txt op-1" v-if="item?.options?.[0].choose">
                                                <div class="label">余额:</div>
                                                <div class="value">{{symbol}}888</div>
                                            </div> -->
                                        </div>
                                        <div class="wrap other-order-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? 'border' : 'none'" v-if="item.title == '其他订单信息'">
                                            <div class="txt op-1" v-if="item?.options?.[0].choose">
                                                <div class="label">{{ $t('下单时间:') }}</div>
                                                <div class="value">2025-07-01 13:00:01</div>
                                            </div>
                                            <div class="txt op-1" v-if="item?.options?.[1].choose">
                                                <div class="label">{{ $t('支付时间:') }}</div>
                                                <div class="value">2025-07-01 13:10:01</div>
                                            </div>
                                            <div class="txt op-1" v-if="item?.options?.[2].choose">
                                                <div class="label">{{ $t('渠道类型:') }}</div>
                                                <div class="value">PC</div>
                                            </div>
                                        </div>
                                        <div class="wrap barcode-wrap" v-if="item.title == '二维码'" :class="isAllCheck(item.options as PrintTemplateOption[]) ? '' : 'none'">
                                            <div class="txt op-1" v-if="item?.options?.[0].choose">
                                                <div class="label">{{ $t('订单编号:') }}</div>
                                                <div class="value">20250725183102646765</div>
                                            </div>
                                            <div class="code">
                                                <SvgIcon v-if="item?.options?.[0].choose" name="setting-barcode" width="100%" height="50px" />
                                                <div class="code-txt">20250725183102646765</div>
                                            </div>
                                        </div>
                                        <div class="wrap notice-wrap" :class="isAllCheck(item.options as PrintTemplateOption[]) ? '' : 'none'" v-if="item.title == '底部公告'">
                                            <template v-for="(op, i) in item.options" :key="i">
                                                <div class="txt" :class="`op-${i}`" v-if="op.choose">
                                                    {{ op.value }}
                                                </div>
                                            </template>
                                        </div>
                                    </template>
                                </div>
                            </el-scrollbar>
                            <div class="foot"></div>
                        </div>
                    </div>
                </template>
                <a-spin :spinning="loading" style="width:100%;margin-top:100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, computed } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { getPrintConfig, updatePrintConfig } from "@/api/setting/receiptPrint";
import type { PrintConfigResponse, PrintTemplateOption } from "@/types/setting/receiptPrint";
import { useConfigStore } from "@/store/config";

// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const configStore = useConfigStore();
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const updateId = ref<number>();
const formState = ref<PrintConfigResponse>({} as PrintConfigResponse);
const disabledList = ref([t('配送方式'),t('商配&同城-收货信息'),t('买家备注'),t('商品基础信息'),t('运费'),t('优惠总计'),t('实收金额'),t('支付方式'),t('第三方支付单号'),t('支付时间')])
const fetchGetPrintConfig = async () => {
    try {
        const result = await getPrintConfig({ printId: id.value });
        Object.assign(
            formState.value,
            result
        );
        if (formState.value.paperSize !== 58 && formState.value.paperSize !== 80) {
            formState.value.paperSize = 58;
        }
        updateId.value = result.id; 
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

const symbol = computed(() => {
    return configStore.config.dollarSign
});

const receiptPreviewStyle = computed<Record<string, string>>(() => {
    const isWidePaper = formState.value.paperSize === 80;

    return {
        "--receipt-content-width": isWidePaper ? "356px" : "258px",
        "--receipt-line-width": isWidePaper ? "366px" : "268px"
    };
});

const isAllCheck = (options: PrintTemplateOption[]) => {
    return options?.some(item => item.choose);
}

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchGetPrintConfig();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    try {
        emit("update:confirmLoading", true);
        let { template, paperSize } = formState.value;
        const result = await updatePrintConfig({ id: updateId.value, template: template, paperSize: paperSize });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.lyecs-form-table {
    display: flex;
    justify-content: space-between;
    max-width: 1200px;
    .letf-wrap {
        width: 598px;
        min-width: 598px;
    }
    .right-wrap {
        width: 384px;
        height: 756px;
        .tabs {
            display: flex;
            justify-content: center;
            gap: -1px;
            width: fit-content;
            margin: 0 auto 24px;
            .tab {
                height: 32px;
                padding: 0 15px;
                color: #666;
                font-size: 14px;
                line-height: 30px;
                border-radius: 2px;
                border: 1px solid #dcdfe6;
                cursor: pointer;
                user-select: none;
                &+ .tab {
                    margin-left: -1px;
                }
                &.active {
                    position: relative;
                    z-index: 1;
                    color: var(--tig-primary);
                    border-color: var(--tig-primary);
                }
            }
        }
        .receipt-preview {
            box-sizing: border-box;
            position: relative;
            margin: 0 auto;
            height: calc(100vh - 408px);
            max-height: 792px;
            .line {
                position: absolute;
                top: -4px;
                left: 50%;
                width: var(--receipt-line-width);
                height: 8px;
                background: #edeef2;
                border-radius: 9px;
                transform: translateX(-50%);
            }
            .scrollbar {
                width: var(--receipt-content-width);
                margin: 0 auto;
                margin-top: -1px;
                box-shadow: 0 0 16px 0 rgba(0, 0, 0, .06);
            }
            .receipt-content {
                box-sizing: border-box;
                width: 100%;     
                padding: 10px 16px;
                background-color: #fff;
                height: fit-content;
                .wrap {
                    padding: 10px 0; 
                    &.border {
                        border-bottom: 1px dashed #e1e2e6;
                    }
                    &.none {
                        display: none;
                    }
                    .txt {
                        display: flex;
                       .label {
                          flex-shrink: 0;
                       } 
                       .value {
                          margin-left: 5px;
                       }
                    }
                }
                .head-wrap {
                    display: flex;
                    flex-direction: column;
                    justify-content: center;
                    align-items: center;
                    .txt {
                        &.op-0 {
                            font-size: 12px;
                            line-height: 22px;
                        }
                        &.op-1 {
                            margin-top: 5px;
                            font-weight: 700;
                            font-size: 18px;
                            line-height: 24px;
                        }
                    }
                }
                .delivery-wrap {
                    .txt {
                        &.op-0 {
                            font-size: 12px;
                            line-height: 22px;
                        }
                        &.op-1 {
                            font-size: 12px;
                            line-height: 22px;
                        }
                    }
                }
                .remarks-wrap {
                    .txt {
                        &.op-0 {
                            font-size: 12px;
                            line-height: 22px;
                            font-weight: 600;
                        }
                    }
                    .value {
                        font-weight: 600;
                    }
                }
                .goods-wrap {
                    font-size: 12px;
                    line-height: 16px;
                    .title {
                        padding-bottom: 4px;
                    }
                    .table {
                        display: flex;
                        padding-top: 4px;
                        .item {
                            flex: 1;
                            &:nth-child(2) {
                                text-align: center;
                            }
                            &:nth-child(3) {
                                text-align: right;
                            }
                        }
                    }
                    .content {
                        .table {
                            display: flex;
                            padding: 4px 0;
                            .item {
                                flex: 1;
                                &:nth-child(2) {
                                    text-align: center;
                                }
                                &:nth-child(3) {
                                    text-align: right;
                                }
                            }
                        }
                        .good-name {
                            padding: 4px 0;
                        }
                        .sn {
                            padding: 4px 0;
                        }
                    }
                    .total {
                        display: flex;
                        justify-content: space-between;
                        .price {
                            font-weight: 600;
                        }
                    }
                }
                .freight-wrap {
                    .txt {
                        display: flex;
                        justify-content: space-between;
                        font-size: 12px;
                        line-height: 16px;
                    }
                }
                .discounts-wrap {
                    .txt {
                        display: flex;
                        justify-content: space-between;
                        font-size: 12px;
                        line-height: 16px;
                        .value {
                            .detail {
                                .item{
                                    font-size: 12px;
                                    line-height: 16px;
                                    padding: 4px 0;
                                    text-align: right;
                                }
                            }
                            .total {
                                font-size: 12px;
                                line-height: 16px;
                                font-weight: 600;
                                text-align: right;
                            }
                        }
                    }
                }
                .pay-wrap {
                    .txt {
                        padding: 4px 0;
                        font-size: 12px;
                        line-height: 16px;
                    }
                }
                .customer-wrap {
                    .txt {
                        padding: 4px 0;
                        font-size: 12px;
                        line-height: 16px;
                    }
                }
                .other-order-wrap {
                    .txt {
                        padding: 4px 0;
                        font-size: 12px;
                        line-height: 16px;
                    }
                }
                .barcode-wrap {
                    padding-bottom: 0px;
                    .txt {
                        padding: 4px 0;
                        font-size: 12px;
                        line-height: 16px;
                    }
                    .code {
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        align-items: center;
                        width: 100%;
                        margin-top: 10px;
                        .code-txt {
                            margin-top: 4px;
                            font-size: 12px;
                            line-height: 16px;
                        }
                    }
                }
                .notice-wrap {
                    padding-top: 0;
                    .txt {
                        height: 56px;
                        justify-content: center;
                        align-items: center;
                        font-size: 12px;
                    }
                }
            }
            .foot {
                position: absolute;
                left: 50%;
                bottom: -4px;
                transform: translateX(-50%);
                width: var(--receipt-content-width);
                height: 13px;
                background-image: url('@/assets/setting/foot.png');
                background-size: 100% 100%;
                background-repeat: no-repeat;
            }
        }
    }
}

:deep(.el-scrollbar__bar).is-vertical {
    width: 4px;
}

</style>
