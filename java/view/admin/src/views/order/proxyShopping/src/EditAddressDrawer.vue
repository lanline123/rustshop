<template>
    <div class="address-drawer">
        <el-form v-if="!loading" ref="formRef" label-width="auto" label-suffix="：" :model="formState" :rules="rules" autocomplete="off">
            <el-form-item :label="$t('收货人')" prop="consignee">
                <el-input v-model="formState.consignee" :maxlength="20" :placeholder="$t('请输入收货人')" />
            </el-form-item>

            <AddressMap
                v-model:regionIds="formState.regionIds"
                v-model:address="formState.address"
                v-model:positionAddress="formState.positionAddress"
                v-model:longitude="formState.longitude"
                v-model:latitude="formState.latitude"
            />

            <el-form-item :label="$t('邮政编码')">
                <el-input v-model="formState.postcode" :maxlength="60" :placeholder="$t('请输入邮政编码')" />
            </el-form-item>

            <el-form-item :label="$t('手机号码')" prop="mobile">
                <el-input v-model="formState.mobile" :maxlength="11" :placeholder="$t('请输入手机号码')" />
            </el-form-item>
        </el-form>
        <a-spin :spinning="loading" style="width: 100%; margin-top: 40px" />
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import AddressMap from "./AddressMap.vue";
import { createProxyShoppingAddress, updateProxyShoppingAddress } from "@/api/proxyShopping/address";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps<{
    userId: number;
    isDialog?: boolean;
    // 传入则为编辑模式（isSystem=1 的系统地址会从父级把当前项传进来）
    address?: any;
}>();

const loading = ref(false);
const formRef = shallowRef();

const formState = reactive<any>({
    userId: 0,
    addressId: 0,
    consignee: "",
    regionIds: [] as number[],
    address: "",
    postcode: "",
    positionAddress: "",
    longitude: 0,
    latitude: 0,
    mobileAreaCode: "86",
    mobile: ""
});

const isEdit = computed(() => Number(props.address?.addressId || 0) > 0);

onMounted(() => {
    formState.userId = Number(props.userId || 0);

    // 编辑模式：回填数据
    if (props.address) {
        formState.addressId = Number(props.address?.addressId || 0);
        formState.consignee = String(props.address?.consignee || "");
        formState.regionIds = Array.isArray(props.address?.regionIds) ? props.address.regionIds : [];
        formState.address = String(props.address?.address || "");
        formState.postcode = String(props.address?.postcode || "");
        formState.positionAddress = String(props.address?.positionAddress || "");
        formState.longitude = Number(props.address?.longitude || 0);
        formState.latitude = Number(props.address?.latitude || 0);
        formState.mobileAreaCode = String(props.address?.mobileAreaCode || "86");
        formState.mobile = String(props.address?.mobile || props.address?.telephone || "");
    }
});

const validateMobile = (_rule: any, value: any, callback: any) => {
    if (!value) return callback(new Error(t('手机号不能为空')));
    callback();
};

const rules = reactive<any>({
    consignee: [{ required: true, message: t('请填写收货人姓名'), trigger: "blur" }],
    regionIds: [{ required: true, message: t('请选择所在地区'), trigger: "change" }],
    address: [
        { required: true, message: t('请填写详细地址'), trigger: "blur" },
        { min: 3, message: t('收货地址过短'), trigger: "blur" }
    ],
    mobile: [{ validator: validateMobile, trigger: "blur", required: true }]
});

const onFormSubmit = async () => {
    await (formRef.value as any)?.validate?.();
    try {
        emit("update:confirmLoading", true);
        loading.value = true;
        const payload = {
            userId: Number(formState.userId || 0),
            addressId: Number(formState.addressId || 0) || undefined,
            address: String(formState.address || "").trim(),
            postcode: String(formState.postcode || "").trim(),
            positionAddress: String(formState.positionAddress || "").trim(),
            latitude: Number(formState.latitude || 0),
            longitude: Number(formState.longitude || 0),
            consignee: String(formState.consignee || "").trim(),
            mobile: String(formState.mobile || "").trim(),
            mobileAreaCode: String(formState.mobileAreaCode || "").trim(),
            regionIds: Array.isArray(formState.regionIds) ? formState.regionIds : []
        };

        // 新增/编辑：先落库，再通知父页面刷新
        if (isEdit.value) {
            const id = Number(formState.addressId || 0);
            await updateProxyShoppingAddress({ ...(payload as any), addressId: id } as any);
            message.success(t('操作成功'));
            emit("submitCallback", { ...payload, addressId: id || undefined });
        } else {
            const addressId = await createProxyShoppingAddress(payload);
            message.success(t('操作成功'));
            emit("submitCallback", { ...payload, addressId: addressId || undefined });
        }
    } catch (e: any) {
        message.error(e?.message || t('保存失败，请检查必填项'));
    } finally {
        loading.value = false;
        emit("update:confirmLoading", false);
    }
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less"></style>
