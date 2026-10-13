/**
 * @file 售后详情页
 * @description 展示售后申请的详细信息和处理进度
 * @features
 * - 展示售后商品信息和退款明细
 * - 展示退款进度（线上/线下/余额退款状态）
 * - 协商记录时间线展示
 * - 支持发表留言和上传凭证
 * - 支持确认收货 / 拒收操作（审核未通过为终态，无操作入口）
 */
<template>
    <div class="container" v-loading="loading">
        <div class="content_wrapper" v-if="!loading">
            <div class="lyecs-form-table">
                <!-- 顶部状态卡：状态标题 → 副文案 → 操作按钮 → 标记订单 -->
                <div class="status-card">
                    <div class="status-card__main">
                        <div class="status-card__title">{{ formState.statusName || "--" }}</div>
                        <div v-if="showAutoAgreeCountdown" class="status-card__tip">
                            {{ $t('订单将在') }}<span class="status-card__countdown">{{ autoAgreeCountdownText }}</span>{{ $t('后自动同意买家退款申请，请及时处理。') }}
                        </div>
                        <div v-else-if="statusTipText" class="status-card__tip">{{ statusTipText }}</div>
                    </div>
                    <div v-if="type != 2" class="status-card__actions">
                        <InfoBtn :formState="formState" :type="type" @callback="fetchBrand" @confirmReceipt="confirmReceipt">
                        </InfoBtn>
                    </div>
                    <div v-if="type != 2 && formState.orderId" class="status-card__footer">
                        <EditSign :item="(formState as any)" :title="$t('标记订单')" @call-back="fetchBrand">
                            <el-button size="small" text type="primary">{{ $t('标记订单') }}</el-button>
                        </EditSign>
                    </div>
                </div>

                <!-- 售后明细 -->
                <div class="container-card">
                    <div class="title">
                        <p>{{ $t('售后明细') }}</p>
                    </div>
                    <div class="info-card">
                        <div class="card-title">
                            <ul>
                                <li class="card-li">
                                    <div>{{ $t('售后方式：') }}</div>
                                    <div class="li-info red">{{ formState.aftersalesTypeName }}</div>
                                </li>
                                <li class="card-li">
                                    <div>{{ $t('售后状态：') }}</div>
                                    <div class="li-info">{{ formState.statusName }}</div>
                                </li>
                                <li class="card-li">
                                    <div>{{ $t('申请时间：') }}</div>
                                    <div class="li-info">{{ formState.addTime ? formState.addTime : "--" }}</div>
                                </li>
                                <!-- <li class="card-li">
                                    联系方式：<span class="li-info">{{ formState.logisticsName ? formState.logisticsName : "--" }}</span>
                                </li> -->
                                <li class="card-li">
                                    <div>{{ $t('退款原因：') }}</div>
                                    <div class="li-info">{{ formState.aftersaleReason }}</div>
                                </li>
                                <li class="card-li">
                                    <div>{{ $t('退款说明：') }}</div>
                                    <div class="li-info">
                                        {{ formState.description ? formState.description : "--" }}
                                    </div>
                                </li>
                            </ul>
                        </div>
                        <div class="card-title">
                            <ul>
                                <li class="card-li" v-if="formState.refund">
                                    <div>{{ $t('退款进度：') }}</div>
                                    <div class="li-info">
                                        <el-popover
                                            :width="220"
                                            popper-style="box-shadow: rgb(14 18 22 / 35%) 0px 10px 38px -10px, rgb(14 18 22 / 20%) 0px 10px 20px -15px; padding: 20px;"
                                        >
                                            <template #reference>
                                                <el-button type="primary" link v-if="formState.refund.refundStatus == 2">{{ $t('退款完成') }}</el-button>
                                                <el-button type="danger" link v-else>{{ $t('退款中') }}</el-button>
                                            </template>
                                            <template #default>
                                                <div class="refund-info flex">
                                                    <div style="color: #666">{{ $t('线上退款：') }}</div>
                                                    <div class="li-info">{{ priceFormat(formState.refund.onlineBalance) || "0" }}</div>
                                                    <div style="margin-left: 10px">
                                                        (
                                                        {{ formState.refund.isOnline == 2 ? $t('退款成功') : formState.refund.isOnline == 1 ? $t('退款中') : $t('无需退款') }}
                                                        )
                                                    </div>
                                                </div>
                                                <div class="refund-info flex">
                                                    <div style="color: #666">{{ $t('线下退款：') }}</div>
                                                    <div class="li-info">{{ priceFormat(formState.refund.offlineBalance) || "0" }}</div>
                                                    <div style="margin-left: 10px">
                                                        (
                                                        {{ formState.refund.isOffline == 2
                                                                ? $t('退款成功')
                                                                : formState.refund.isOffline == 1
                                                                  ? $t('退款中')
                                                                  : $t('无需退款') }}
                                                        )
                                                    </div>
                                                </div>
                                                <div class="flex">
                                                    <div style="color: #666">{{ $t('余额退款：') }}</div>
                                                    <div class="li-info">{{ priceFormat(formState.refund.refundBalance) || "0" }}</div>
                                                    <div style="margin-left: 10px">
                                                        (
                                                        {{ formState.refund.isReceive == 2
                                                                ? $t('退款成功')
                                                                : formState.refund.isReceive == 1
                                                                  ? $t('退款中')
                                                                  : $t('无需退款') }}
                                                        )
                                                    </div>
                                                </div>
                                            </template>
                                        </el-popover>
                                    </div>
                                </li>
                                <li class="card-li">
                                    <div>{{ adminType === "vendor" ? $t('退货供货金额：') : $t('售后金额：') }}</div>
                                    <div class="li-info">
                                        {{ formatRefoundAmount }}
                                    </div>
                                </li>
                                <li class="card-li">
                                    <div>{{ $t('物流公司：') }}</div>
                                    <div class="li-info">{{ formState.logisticsName || "--" }}</div>
                                </li>
                                <li class="card-li">
                                    <div>{{ $t('物流单号：') }}</div>
                                    <div class="li-info">{{ formState.trackingNo || "--" }}</div>
                                </li>
                                <li class="card-li">
                                    <div>{{ $t('退货地址：') }}</div>
                                    <div class="li-info">{{ formState.returnAddress || "--" }}</div>
                                </li>
                                <li class="card-li" v-if="formState.aftersalesItems"> {{ $t('订单编号：') }}<span class="li-info">{{ formState.aftersalesItems[0].orderSn }}</span>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
                <!-- 商品信息 -->
                <el-table :data="formState.aftersalesItems" style="width: 100%; margin-bottom: 20px" border>
                    <el-table-column :label="$t('商品信息')" width="400">
                        <template #default="{ row }">
                            <div class="flex">
                                <ProductCard :picThumb="row.picThumb" :productId="row.productId" :productName="row.productName"> </ProductCard>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="adminType === 'vendor' ? $t('供货价') : $t('商品价格')" align="center">
                        <template #default="{ row }">
                            <div>{{ priceFormat(adminType === "vendor" ? getSupplierOrderItemPrice(row) : row.price) }}</div>
                        </template>
                    </el-table-column>
                    <el-table-column prop="quantity" :label="$t('商品数量')" align="center" />
                    <el-table-column prop="number" :label="$t('退货数量')" align="center" />
                </el-table>
                <div class="negotiation-box">
                    <div class="negotiation-title flex flex-justify-between">
                        <div>{{ $t('协商记录') }}</div>
                        <div class="btn" v-if="formState.status == 1 && type != 2">
                            <el-button bg size="small" text type="primary" :disabled="isMsg" @click="isMsg = true"> {{ $t('点击发表留言') }} </el-button>
                        </div>
                    </div>
                    <el-form :model="messageForm" label-width="auto" style="margin-top: 20px" v-if="isMsg">
                        <el-form-item :label="$t('留言内容：')" prop="logInfo">
                            <TigInput
                                width="100%"
                                v-model="messageForm.logInfo"
                                :rows="4"
                                type="textarea"
                                :maxlength="300"
                                show-word-limit
                                :placeholder="$t('你可以在这里给买家留言, 为了保证你的权益, 请尽可能详细的上传留言资料')"
                            />
                        </el-form-item>
                        <el-form-item :label="$t('上传凭证：')" prop="returnPic">
                            <FormAddGallery v-if="!loading" v-model:photos="messageForm.returnPic" :isMultiple="true"> </FormAddGallery>
                        </el-form-item>
                        <el-form-item>
                            <div class="btn-box">
                                <el-button type="primary" @click="postMessage(formState.aftersaleId)">{{ $t('发表留言') }}</el-button>
                                <el-button @click="isMsg = false">{{ $t('取消') }}</el-button>
                            </div>
                        </el-form-item>
                    </el-form>
                    <div class="negotiation-content">
                        <Timeline>
                            <TimelineItem v-for="(item, index) in formState.aftersalesLog">
                                <template #dot>
                                    <div class="logo" v-if="item.userName && !item.adminName">{{ $t('买') }}</div>
                                    <div class="logo-b" v-if="item.adminName">
                                        <span v-if="item.shopId > 0">{{ $t('商') }}</span>
                                        <span v-else-if="item.vendorId > 0">{{ $t('供') }}</span>
                                        <p v-else class="platform">{{ $t('平台') }}</p>
                                    </div>
                                </template>
                                <div class="info-box">
                                    <div class="tit">
                                        <div class="txt">
                                            <span v-if="item.userName && !item.adminName">{{ $t('买家') }}</span>
                                            <span v-if="item.adminName">
                                                <span v-if="item.shopId > 0">{{ $t('商家') }}</span>
                                                <span v-else-if="item.vendorId > 0">{{ $t('供应商') }}</span>
                                                <span v-else>{{ item.adminName }}：</span>
                                            </span> {{ $t('留言') }} </div>
                                        <div class="time">
                                            {{ item.addTime }}
                                        </div>
                                    </div>
                                    <div class="detail">
                                        <div class="content">
                                            {{ item.logInfo }}
                                        </div>
                                        <div class="card-li" v-if="index + 1 == formState.aftersalesLog?.length">
                                            <span>{{ $t('售后方式：') }}</span>
                                            <span class="li-info">
                                                {{ formState.aftersalesTypeName }}
                                            </span>
                                        </div>
                                        <div class="card-li" v-if="index + 1 == formState.aftersalesLog?.length">
                                            <span>{{ $t('退款原因：') }}</span>
                                            <span class="li-info">
                                                {{ formState.aftersaleReason }}
                                            </span>
                                        </div>
                                        <div class="card-li" v-if="index + 1 == formState.aftersalesLog?.length">
                                            <span>{{ $t('退款说明：') }}</span>
                                            <span class="li-info">
                                                {{ formState.description }}
                                            </span>
                                        </div>
                                        <div class="card-li" v-if="item.returnPic?.length > 0">
                                            <span>{{ $t('上传凭证：') }}</span>
                                            <span class="li-info">
                                                <Image
                                                    v-for="pic in item.returnPic"
                                                    style="margin-right: 10px; border: 1px solid rgba(5, 5, 5, 0.06)"
                                                    width="60"
                                                    height="60"
                                                    :src="imageFormat(pic.picThumb)"
                                                    :preview-src-list="[imageFormat(pic.picUrl)]"
                                                    fit="cover"
                                                />
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </TimelineItem>
                        </Timeline>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { imageFormat } from "@/utils/format";
import { DialogForm } from "@/components/dialog";
import { Image } from "@/components/image";
import { onMounted, onUnmounted, ref, shallowRef, computed } from "vue";
import { useRouter } from "vue-router";
import { message, Timeline, TimelineItem } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { FormState } from "@/types/order/aftersales";
import { getAftersales, addMessage, updataConfirmReceipt } from "@/api/order/aftersales";
import { ProductCard } from "@/components/list";
import { priceFormat } from "@/utils/format";
import { getSupplierAftersalesAmount, getSupplierOrderItemPrice } from "@/utils/orderSupplierPrice";
import { isS2b2c } from "@/utils/version";
import InfoBtn from "./src/InfoBtn.vue";
import EditSign from "@/views/order/order/src/EditSign.vue";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const adminType = ref(localStorage.getItem("adminType"));
// 父组件回调
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
    type: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "insert" : "update";
const formRef = shallowRef();
const formState = ref<FormState>({
    picThumb: "",
    number: 1
});
const isMsg = ref<boolean>(false);
const messageForm = ref<any>({
    logInfo: "",
    returnPic: []
});
/**
 * 顶部状态卡副文案
 * 优先后端返回的 statusDescription / statusDesc；缺失时按售后 status 数字本地兜底。
 * value 为 i18n 词条 key，在 statusTipText 中按当前语言取值。
 */
const statusTipMap: Record<number, string> = {
    0: "买家已提交售后申请，请及时处理",
    1: "买家已提交售后申请，请及时处理",
    21: "供应商待处理，请及时处理",
    2: "已同意售后，等待买家寄回商品",
    4: "等待买家寄回商品",
    5: "买家已寄回商品，请及时确认收货",
    22: "买家已寄回商品，请供应商确认收货",
    6: "售后已完成，退款将按原支付路径退回",
    3: "售后申请已拒绝",
    7: "售后已取消"
};
const statusTipText = computed<string>(() => {
    const backendTip = formState.value.statusDescription || formState.value.statusDesc;
    if (backendTip) {
        return backendTip;
    }
    const tip = statusTipMap[Number(formState.value.status)];
    return tip ? t(tip) : "";
});

// ============ 顶部状态卡「自动同意退款」倒计时 ============
/** 每秒更新的当前时间戳，仅倒计时场景触发 */
const nowTs = ref(Date.now());
/** 服务器与本地时钟偏移（拉取详情时算一次），用于校准本地时钟漂移 */
const serverOffset = ref(0);
/** 校准后的当前时间戳 */
const calibratedNow = computed(() => nowTs.value + serverOffset.value);
/** 自动同意退款截止毫秒时间戳；未返回或非法返回 0（兼容 iOS 的 - 分隔符解析） */
const autoAgreeEndTimeMs = computed(() => {
    const raw = String(formState.value.autoAgreeEndTime || "");
    if (!raw) return 0;
    const ms = Date.parse(raw.replace(/-/g, "/"));
    return Number.isFinite(ms) ? ms : 0;
});
/** 处理中：售后 status ∈ {0,1,21}（待处理/待商家/待供应商审核） */
const isProcessing = computed(() => [0, 1, 21].includes(Number(formState.value.status ?? -1)));
/** 是否展示「自动同意退款」倒计时（处理中 + 有合法的未来截止时间） */
const showAutoAgreeCountdown = computed(() => isProcessing.value && autoAgreeEndTimeMs.value > calibratedNow.value);
/** 倒计时文案：N天N小时N分（天/小时为 0 时省略），剩余不足 1 分钟按 1 分显示 */
const autoAgreeCountdownText = computed(() => {
    const diff = autoAgreeEndTimeMs.value - calibratedNow.value;
    if (diff <= 0) return "";
    const totalSec = Math.floor(diff / 1000);
    const days = Math.floor(totalSec / 86400);
    const hours = Math.floor((totalSec % 86400) / 3600);
    let minutes = Math.floor((totalSec % 3600) / 60);
    if (days === 0 && hours === 0 && minutes === 0) minutes = 1;
    let text = "";
    if (days > 0) text += t("{days}天", { days });
    if (days > 0 || hours > 0) text += t("{hours}小时", { hours });
    text += t("{minutes}分", { minutes });
    return text;
});

// 倒计时逐秒推进；归零后自动重拉一次详情
let countdownTimer: ReturnType<typeof setInterval> | null = null;
let refetchedOnZero = false;

const formatRefoundAmount = computed(() => {
    if (adminType.value === "vendor") {
        return priceFormat(getSupplierAftersalesAmount(formState.value.aftersalesItems));
    }
    //rerefundAmount是字符串需要做处理，也可能是小数
    let refundAmount = 0;

    if (formState.value.actualRefundAmount) {
        return priceFormat(formState.value.actualRefundAmount);
    }
    if (typeof formState.value.refundAmount === "string") {
        refundAmount = Number(formState.value.refundAmount);
        return priceFormat(refundAmount);
    }
    return "0";
});
const fetchBrand = async () => {
    try {
        const result = await getAftersales(action.value, { id: id.value });
        Object.assign(formState.value, result);
        // 用服务器时间校准本地时钟漂移（缺失则不校准）
        const serverRaw = String(formState.value.serverTime || "");
        if (serverRaw) {
            const serverMs = Date.parse(serverRaw.replace(/-/g, "/"));
            serverOffset.value = Number.isFinite(serverMs) ? serverMs - Date.now() : 0;
        } else {
            serverOffset.value = 0;
        }
        formState.value.aftersalesTypeConfig = toArray(formState.value.aftersalesTypeConfig);
        formState.value.statusConfig = toArray(formState.value.statusConfig);
        formState.value.modifyNumber = formState.value.number;
        emit("submitCallback", "", false);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const toArray = (arr: any) => {
    if (typeof arr == "object") {
        var newArr = [];

        for (let i in arr) {
            newArr.push(arr[i]);
        }
        return newArr;
    } else {
        return arr;
    }
};
const onInput = (e: number) => {
    if (e > formState.value.number) {
        formState.value.modifyNumber = formState.value.number;
    }
    if (e < 1) {
        formState.value.modifyNumber = 1;
    }
};

const postMessage = async (id: any) => {
    if (messageForm.value.logInfo == "") {
        message.error(t('请输入留言内容'));
        return false;
    }
    try {
        const result = await addMessage({ aftersaleId: id, ...messageForm.value });
        isMsg.value = false;
        let data = {
            logInfo: "",
            returnPic: []
        };
        messageForm.value = data;
        fetchBrand();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    // 获取详情数据
    fetchBrand();
    // 倒计时逐秒推进；归零后自动重拉一次详情
    countdownTimer = setInterval(() => {
        if (!isProcessing.value || autoAgreeEndTimeMs.value <= 0) return;
        nowTs.value = Date.now();
        if (autoAgreeEndTimeMs.value - calibratedNow.value <= 0 && !refetchedOnZero) {
            refetchedOnZero = true;
            fetchBrand();
        }
    }, 1000);
});
onUnmounted(() => {
    if (countdownTimer) {
        clearInterval(countdownTimer);
        countdownTimer = null;
    }
});

// 确认收货
const confirmReceipt = async () => {
    try {
        const result = await updataConfirmReceipt({ id: id.value });
        message.success(t('操作成功'));
        // fetchBrand();
        emit("submitCallback");
    } catch (error: any) {
        message.error(error.message);
    }
};
// 表单提交
const onFormSubmit = () => {
    // onSubmit()
    emit("submitCallback");
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.status-card {
    border: 1px solid #ececec;
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;

    .status-card__title {
        font-size: 16px;
        line-height: 25px;
        color: #323233;
        font-weight: 700;
    }

    .status-card__tip {
        margin-top: 6px;
        font-size: 13px;
        line-height: 20px;
        color: #86909c;

        .status-card__countdown {
            margin: 0 4px;
            color: #cc0c1c;
            font-weight: 600;
        }
    }

    .status-card__actions {
        margin-top: 16px;
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 8px;
    }

    .status-card__footer {
        margin-top: 12px;
        display: flex;
        align-items: center;
    }
}
.container-card {
    border: 1px solid #ececec;
    /* 添加淡淡的边框 */
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;

    .border-bottom {
        border-bottom: 1px solid #ececec;
        padding-bottom: 16px;
        margin-bottom: 16px;
    }

    .button-card {
        margin-bottom: 12px;
        margin-top: 20px;
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        /* 允许换行 */
        gap: 8px;
    }

    .babyBlue {
        background-color: rgba(61, 127, 255, 0.06);
        border-radius: 6px;
    }

    .pd20 {
        padding: 20px;
    }

    .orange {
        color: #ed6a0c;
    }

    .title {
        font-size: 16px;
        line-height: 25px;
        color: #323233;
        margin-bottom: 15px;
        font-weight: 700;
    }

    .info-card {
        display: flex;
        flex-direction: row;
        gap: 20px;
        white-space: nowrap;

        .card-title {
            flex: 1;
            display: flex;
            flex-direction: column;
            font-size: 14px;
            line-height: 30px;
            font-weight: 500;
            color: #646566;
        }

        .min-title {
            margin-bottom: 10px;
        }

        .card-li {
            display: flex;
            font-weight: normal;
            font-size: 12px;
            line-height: 28px;
        }

        .li-info {
            max-width: 300px;
            white-space: wrap;
            color: black;
            :deep(.el-button.is-link) {
                margin-bottom: 2px;
            }
        }
        .red {
            color: var(--tig-red-text-color);
        }
    }

    .total {
        margin: 24px 0;
        width: 100%;
        text-align: right;
    }
}
.negotiation-box {
    .negotiation-title {
        font-size: 16px;
        font-weight: 700;
        padding-bottom: 25px;
    }
    .btn-box {
        display: flex;
        justify-content: flex-end;
        width: 100%;
    }
    .negotiation-content {
        .logo {
            width: 24px;
            height: 24px;
            line-height: 24px;
            display: inline-block;
            border-radius: 50%;
            background-color: #74829d;
            color: #fff;
            text-align: center;
        }
        .logo-b {
            width: 24px;
            height: 24px;
            line-height: 24px;
            display: inline-block;
            border-radius: 50%;
            background-color: #155bd4;
            color: #fff;
            text-align: center;
            .platform {
                font-size: 10px;
            }
        }
        .info-box {
            background-color: #f7f8fa;
            padding: 16px;
            border-radius: 4px;
            margin-bottom: 16px;
            .tit {
                display: flex;
                align-items: center;
                justify-content: space-between;
                .txt {
                    color: #323233;
                    font-weight: 500;
                }
                .time {
                    color: #999;
                }
            }
            .detail {
                border-top: 1px solid #ebedf0;
                margin-top: 15px;
                padding-top: 5px;
                font-size: 12px;
                .content {
                    padding: 5px 0;
                }
                .card-li {
                    padding: 5px 0;
                    color: #646566;
                    display: flex;
                    .li-info {
                        color: #333;
                    }
                }
            }
        }
    }
}
.refund-info {
    margin-bottom: 10px;
}
</style>
