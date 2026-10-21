/** * @file 询价详情/回复 * @description 查看询价详情并回复客户询价 * @features * - 展示询价内容和联系方式 * - 未回复状态可填写回复内容 * -
已回复状态显示回复记录 */
<template>
    <div class="container">
        <div class="lyecs-form-table">
            <h2>
                <span>{{ $t("类型:{typeName}", { typeName: $t("商品") }) }} </span>
                <span v-if="formState.product?.productName">【 {{ formState.product.productName }} 】</span>
            </h2>
        </div>
        <el-divider />
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" :model="formState" label-width="auto">
                    <el-form-item :label="$t('联系电话')" prop="mobile">
                        <div>{{ formState.mobile }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('邮箱')" prop="email">
                        <div>{{ formState.email || "--" }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('询价内容')" prop="content">
                        <div>{{ formState.content }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('创建时间')" prop="createTime">
                        <div>{{ formState.createTime }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('回复内容')" v-if="canReplyCurrent">
                        <TigInput classType="tig-form-input" v-model="replyRemark" :rows="6" type="textarea" :placeholder="$t('请输入回复内容')" />
                    </el-form-item>
                    <el-form-item label=" " v-if="canReplyCurrent">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t("提交") }}</el-button>
                    </el-form-item>
                </el-form>
                <div class="negotiation-box" v-if="!loading && formState.replyRecords?.length">
                    <div class="negotiation-title">{{ $t("回复记录") }}</div>
                    <div class="negotiation-content">
                        <Timeline>
                            <TimelineItem v-for="(item, index) in formState.replyRecords" :key="`${item.createTime}-${index}`">
                                <template #dot>
                                    <div class="logo-b" v-if="isMerchantReply(item.replyName)">{{ $t("商") }}</div>
                                    <div class="logo" v-else>{{ $t("买") }}</div>
                                </template>
                                <div class="info-box">
                                    <div class="tit">
                                        <div class="txt">
                                            {{ $t("{replyName}：回复", { replyName: item.replyName }) }}
                                        </div>
                                        <div class="time">{{ item.createTime }}</div>
                                    </div>
                                    <div class="detail">
                                        <div class="content">{{ item.content }}</div>
                                    </div>
                                </div>
                            </TimelineItem>
                        </Timeline>
                    </div>
                </div>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { message, Timeline, TimelineItem } from "ant-design-vue";
import { getPriceInquiry, updatePriceInquiryReply } from "@/api/product/enquiry";
// 父组件回调
import { useI18n } from "vue-i18n";
const { t } = useI18n();
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
    isDialog: Boolean,
    closeOnSubmit: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formState = ref<any>({
    id: 0,
    userId: 0,
    content: "",
    remark: ""
});
const replyRemark = ref("");
const canReplyCurrent = computed(() => Number(formState.value?.userId) > 0);
const fetchBrand = async () => {
    try {
        const result = await getPriceInquiry(action.value, { id: id.value });
        Object.assign(formState.value, result);
        formState.value.replyRecords = result.replyRecords || [];
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

const isMerchantReply = (replyName = "") => {
    return /admin|商家|供应商|平台/i.test(replyName);
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchBrand();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    if (!canReplyCurrent.value) {
        message.warning(t("该记录无需回复"));
        return;
    }
    if (!replyRemark.value.trim()) {
        message.error(t("请输入回复内容"));
        return;
    }
    try {
        emit("update:confirmLoading", true);
        const result = await updatePriceInquiryReply({ id: id.value, remark: replyRemark.value });
        emit("submitCallback", result, props.closeOnSubmit);
        message.success(t("操作成功"));
        replyRemark.value = "";
        fetchBrand();
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
    h2 {
        line-height: 30px;
    }
}

.negotiation-box {
    margin-top: 16px;
    .negotiation-title {
        font-size: 16px;
        font-weight: 700;
        padding-bottom: 30px;
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
                    color: #333;
                }
            }
        }
    }
}
</style>
