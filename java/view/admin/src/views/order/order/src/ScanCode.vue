/** * @file 扫码核销组件 * @description 订单核销功能，支持扫码枪或手动输入核销码 * @features * - 支持扫码枪自动核销（带回车） * - 支持手动输入核销码/订单号 * -
展示已核销订单列表 * - 自动聚焦输入框 */
<template>
    <div class="container">
        <div class="form-row">
            <div class="input-row">
                <TigInput ref="pickupSnInputRef" v-model="pickupSn" width="400px" :placeholder="$t('请输入核销码/订单号或扫码核销')" @keyup.enter="onSubmit" clearable>
                    <template #prefix>
                        <SvgIcon name="product-QRcode" width="20" height="20" />
                    </template>
                </TigInput>
                <div class="btn" @click="onSubmit">{{ $t('核销(Enter)') }}</div>
            </div>
            <div class="tips">{{ $t('提示：支持扫码枪设置带回车自动核销') }}</div>
            <div class="order-list">
                <div class="order-item" v-for="item in orderList">
                    <div class="order-product" v-for="product in item.items">
                        <div class="info">
                            <div class="image">
                                <el-image style="width: 50px; height: 50px" :src="imageFormat(product.picThumb)" fit="cover" />
                            </div>
                            <div class="info-text">
                                <div class="name">{{ product.productName }}</div>
                                <div class="sn">{{ $t('订单编号：{orderSn}', { orderSn: product.orderSn }) }}</div>
                            </div>
                        </div>
                        <div class="num">x{{ product.quantity }}</div>
                        <div class="price">{{ priceFormat(product.price) }}</div>
                        <div class="status">{{ $t('已核销') }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, reactive } from "vue";
import { message } from "ant-design-vue";
import { writeOffOrder } from "@/api/order/order";
import { OrderFormState } from "@/types/order/order.d";
import { imageFormat } from "@/utils/format";
import { priceFormat } from "@/utils/format";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const pickupSnInputRef = ref();
const pickupSn = ref("");

const orderList = ref<OrderFormState[]>([]);
// 表单通过验证后提交
const onSubmit = async () => {
    try {
        emit("update:confirmLoading", true);
        if (pickupSn.value) {
            const result = await writeOffOrder({ pickupSn: pickupSn.value });
            if (result) {
                orderList.value.unshift(result);
            }
        } else {
            return message.error(t('请输入核销码/订单号'));
        }
        message.success(t('核销成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        pickupSn.value = "";
        emit("update:confirmLoading", false);
    }
};

onMounted(() => {
    setTimeout(() => {
        if (pickupSnInputRef.value && pickupSnInputRef.value.$refs.inputRef) {
            pickupSnInputRef.value.$refs.inputRef.focus();
        }
    }, 100);
});

// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.container {
    display: flex;
    justify-content: center;
    padding: 30px 20px 30px 20px;
}

.input-row {
    border: 1px solid #dcdfe6;
    border-radius: 100px;
    display: flex;
    justify-content: center;
    overflow: hidden;

    :deep(.el-input__wrapper) {
        box-shadow: none;
    }

    .btn {
        background-color: var(--tig-primary-color);
        color: #fff;
        padding: 0 10px;
        line-height: 30px;
        cursor: pointer;
    }
}

.tips {
    color: #a8abb2;
    margin-top: 5px;
}

.order-list {
    margin-top: 20px;
    max-height: 280px;
    cursor: pointer;
    overflow-y: auto;
    scrollbar-width: none;
    /* Firefox */
    -ms-overflow-style: none;

    /* IE 10+ */
    .order-item {
        background-color: #f5f7fa;
        padding: 0px 15px 0 15px;
        border-radius: 5px;
        margin-bottom: 5px;

        .order-product {
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1px solid rgba(5, 5, 5, 0.06);
            padding-bottom: 15px;
            padding-top: 15px;

            &:last-child {
                border-bottom: none;
            }

            .info {
                display: flex;
                align-items: center;

                .image {
                    margin-right: 10px;
                }

                .info-text {
                    .name {
                        color: #333;
                        font-size: 14px;
                        margin-bottom: 10px;
                    }

                    .sn {
                        color: #a8abb2;
                        font-size: 14px;
                    }
                }
            }

            .num,
            .price {
                color: #333;
                font-size: 14px;
            }

            .status {
                color: #a8abb2;
                white-space: nowrap;
            }
        }
    }
}
</style>
