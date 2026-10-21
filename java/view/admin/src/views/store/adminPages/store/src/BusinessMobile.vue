<template>
    <div class="time-cycle">
        <el-form-item :label="$t('联系电话')" prop="mobileData" :rules="[{ required: true, validator: validatePhoneType }]">
            <div>
                <div class="item" v-for="(item, index) in mobileData">
                    <el-checkbox v-model="item.checked" :label="item.label" :disabled="props.disabled" />
                    <div>
                        <div class="mb10" v-for="(mobile, index1) in item.mobiles">
                            <el-form-item
                                label=""
                                :prop="'mobileData.' + index + '.mobiles.' + index1"
                                :rules="[{ required: item.checked ? true : false, validator: validatePhone }]"
                            >
                                <div class="flex">
                                    <div v-if="item.type !== 3" class="flex">
                                        <TigInput width="60px" :placeholder="$t('区号')" v-model="mobile.code" :disabled="props.disabled || !item.checked" />
                                        <div class="ml10 mr10" style="font-size: 20px; font-weight: bold">-</div>
                                        <TigInput
                                            width="200px"
                                            :placeholder="$t('座机号')"
                                            v-model="mobile.value"
                                            :disabled="props.disabled || !item.checked"
                                        />
                                    </div>
                                    <div v-else>
                                        <TigInput
                                            width="290px"
                                            :placeholder="$t('请输入联系电话')"
                                            v-model="mobile.value"
                                            :disabled="props.disabled || !item.checked"
                                        />
                                    </div>
                                    <el-button
                                        v-if="!props.disabled && item.mobiles.length > 1"
                                        class="ml10"
                                        type="primary"
                                        link
                                        @click="deleteMobile(index, index1)"
                                        >{{ $t("删除") }}</el-button
                                    >
                                </div>
                            </el-form-item>
                        </div>
                        <el-button v-if="!props.disabled && item.mobiles.length < 2" link type="primary" @click="addMobile(item)" :disabled="!item.checked">
                            {{ $t("添加号码") }}
                        </el-button>
                    </div>
                </div>
                <div v-if="props.tip" class="extra">{{ $t(props.tip) }}</div>
            </div>
        </el-form-item>
    </div>
</template>
<script setup lang="ts">
import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const mobileData = ref<any[]>([
    {
        type: 1,
        checked: false,
        label: t("普通座机号"),
        mobiles: [
            {
                code: "",
                value: ""
            }
        ]
    },
    {
        type: 2,
        checked: false,
        label: t("企业座机号"),
        mobiles: [
            {
                code: "",
                value: ""
            }
        ]
    },
    {
        type: 3,
        checked: false,
        label: t("手机号码"),
        mobiles: [
            {
                code: "",
                value: ""
            }
        ]
    }
]);
const props = defineProps({
    formData: {
        type: Array,
        default: []
    },
    tip: {
        type: String,
        default: "请填写门店电话，该电话会显示在用户端店铺详情中 查看示例"
    },
    disabled: {
        type: Boolean,
        default: false
    }
});

watch(
    () => props.formData,
    (newVal: any) => {
        if (newVal.length > 0) {
            mobileData.value = mobileData.value.map((item: any) => {
                const filteredData = newVal.filter((data: any) => data.type === item.type);
                item.checked = filteredData.length > 0;
                item.mobiles = filteredData.flatMap((data: any) => {
                    if (item.type === 3) {
                        return [{ code: "", value: data.values || "" }];
                    }
                    const raw = data.values || "";
                    if (raw.includes("-")) {
                        const [code, ...rest] = raw.split("-");
                        return [{ code: code || "", value: rest.join("-") || "" }];
                    }
                    return [{ code: "", value: raw }];
                });
                return item;
            });
        }
    },
    { deep: true, immediate: true }
);

const addMobile = (item: any) => {
    item.mobiles.push({
        code: "",
        value: ""
    });
};

const deleteMobile = (index: number, indexChild: number) => {
    mobileData.value[index].mobiles.splice(indexChild, 1);
};

const validatePhoneType = (rule: any, value: any, callback: any) => {
    let isChecked = false;
    mobileData.value.forEach((item: any) => {
        if (item.checked) {
            isChecked = true;
            return;
        }
    });
    if (!isChecked) {
        callback(new Error(t("请选择联系电话")));
        return;
    }
    callback();
};
const validatePhone = (rule: any, value: any, callback: any) => {
    const regex = /mobileData\.(\d+)\.mobiles\.(\d+)/;
    const match: any = rule?.field.match(regex);
    const rowIndex = match[1];
    const rowIndexChild = match[2];
    let row = mobileData.value[rowIndex];
    let condition = mobileData.value[rowIndex].mobiles[rowIndexChild] as any;
    if (row.checked && row.type !== 3 && (!condition.code || !condition.value)) {
        callback(new Error(t("请输入号码")));
        return;
    }
    if (row.checked && row.type === 3 && !condition.value) {
        callback(new Error(t("请输入手机号码")));
        return;
    }
    if (row.checked && row.type === 3 && !condition.value.match(/^1[3-9]\d{9}$/)) {
        callback(new Error(t("请输入正确的手机号码")));
        return;
    }
    callback();
};

defineExpose({ formData: mobileData });
</script>
<style scoped lang="less">
.time-cycle {
    :deep(.el-form-item__content) {
        display: block;
    }
    :deep(.el-form-item__error) {
        position: relative;
        font-size: 14px;
        margin-top: 5px;
        top: 0;
    }
}
</style>
