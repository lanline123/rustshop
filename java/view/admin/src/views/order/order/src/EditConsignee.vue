/**
 * @file 修改收货人信息
 * @description 订单收货人信息编辑表单
 * @features
 * - 修改收货人姓名、手机、电话
 * - 修改收货地区（省市区三级联动）
 * - 修改详细地址、邮编、邮箱
 */
<template>
  <div class="container">
    <div class="content_wrapper">
      <div class="lyecs-form-table">
        <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
          <el-form-item :rules="[{ required: true, message: '收货人不能为空!' }]" :label="$t('收货人')" prop="consignee">
            <TigInput classType="tig-form-input" v-model="formState.consignee" />
          </el-form-item>
          <el-form-item :label="$t('所在地区')" prop="regionIds">
              <div v-if="isOverseas()" class="cross-border-fields">
                <el-select v-model="formState.countryCode" :placeholder="$t('选择国家')" @change="onCountryChange">
                  <el-option v-for="country in countries" :key="country.standardCode" :label="country.regionName" :value="country.standardCode" />
                </el-select>
                <TigInput classType="tig-form-input" v-model="formState.mobileAreaCode" :placeholder="$t('区号')" />
              </div>
              <SelectRegion v-if="!loading" :key="formState.countryCode || 'domestic'" v-model:selectedIds="formState.regionIds" :lazy="isOverseas()" :root-region-id="selectedCountryRegionId" @change="onRegionChange"></SelectRegion>
              <TigInput v-if="isOverseas()" classType="tig-form-input" v-model="regionNamesText" :placeholder="$t('地区名称，多个层级用逗号分隔')" @blur="syncRegionNames" />
          </el-form-item>
          <el-form-item :rules="[{ required: true, message: '手机不能为空!' }]" :label="$t('手机')" prop="mobile">
            <TigInput classType="tig-form-input" v-model="formState.mobile" />
          </el-form-item>
          <el-form-item :rules="[{ required: true, message: '地址不能为空!' }]" :label="$t('地址')" prop="address">
            <TigInput classType="tig-form-input" v-model="formState.address" />
          </el-form-item>
          <el-form-item :label="$t('邮编')" prop="postcode">
            <TigInput classType="tig-form-input" v-model="formState.postcode" />
          </el-form-item>
          <el-form-item :label="$t('电子邮件')" prop="email">
            <TigInput classType="tig-form-input" v-model="formState.email" />
          </el-form-item>
          <el-form-item :label="$t('电话')" prop="telephone">
            <TigInput classType="tig-form-input" v-model="formState.telephone" />
          </el-form-item>
          <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
            <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }} </el-button>
          </el-form-item>
        </el-form>
        <a-spin :spinning="loading" style="width:100%;margin-top:100px" />
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue"
import { useRouter } from 'vue-router'
import { message } from "ant-design-vue";
import { OrderFormState } from '@/types/order/order.d';
import { getOrder, modifyOrderConsignee } from "@/api/order/order";
import { getCountryList } from "@/api/setting/region";
import { priceFormat } from "@/utils/format";
import { Checkbox } from "@/components/radio";
import { SelectRegion } from "@/components/select";
import { isOverseas } from "@/utils/version";
import { buildConsigneePayload } from "./consigneePayload";
import type { CountryRegionState } from "@/types/setting/region";

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
    default: 0
  },
  form: {
    type: Object as () => OrderFormState,
    default: () => ({})
  },
  isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formRef = shallowRef();
const formState = ref<OrderFormState>(props.form);
const countries = ref<CountryRegionState[]>([]);
const regionNamesText = ref("");
const selectedCountryRegionId = computed(() => countries.value.find((item) => item.standardCode === formState.value.countryCode)?.regionId);

onMounted(() => {
  // 获取详情数据
  fetchOrder();
  if (isOverseas()) loadCountries();
});
const loadCountries = async () => {
  try {
    countries.value = await getCountryList();
  } catch (error: any) {
    message.error(error?.message || t('国家列表加载失败'));
  }
};
const onCountryChange = (countryCode: string) => {
  const country = countries.value.find((item) => item.standardCode === countryCode);
  formState.value.mobileAreaCode = country?.mobileAreaCode || "";
  formState.value.regionIds = [];
  formState.value.regionNames = [];
  regionNamesText.value = "";
  formState.value.addressData = {};
};
const onRegionChange = (regions: any[]) => {
  formState.value.regionNames = (regions || []).map((item) => item.regionName).filter(Boolean);
  regionNamesText.value = formState.value.regionNames.join(", ");
};
const syncRegionNames = () => {
  formState.value.regionNames = regionNamesText.value.split(/[,，]/).map((name) => name.trim()).filter(Boolean);
};
const fetchOrder = async () => {
  try {
    loading.value = true;
    const result = await getOrder(action.value, { id: id.value });
    Object.assign(
      formState.value,
      result
    )
    const snapshot = result.addressData as any;
    if (snapshot) {
      formState.value.countryCode ||= snapshot.countryCode;
      formState.value.mobileAreaCode ||= snapshot.mobileAreaCode;
      formState.value.regionNames ||= snapshot.regionNames;
      formState.value.regionIds ||= snapshot.regionIds;
      formState.value.address ||= snapshot.address;
      formState.value.postcode ||= snapshot.postcode;
    }
    const names = formState.value.regionNames || formState.value.regionName || [];
    regionNamesText.value = (Array.isArray(names) ? names : [names]).filter(Boolean).join(", ");
  } catch (error:any) {
    message.error(error.message);
    emit('close');
  } finally {
    loading.value = false;
  }
};


// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
  try {
    emit('update:confirmLoading', true);
    const addressData = { ...(formState.value.addressData || {}) };
    syncRegionNames();
    const names = formState.value.regionNames || [];
    if (formState.value.countryCode === "MY") {
      if (names[0]) addressData.province = names[0];
      if (names[1]) addressData.city = names[1];
    } else if (formState.value.countryCode === "SG") {
      if (names[0]) addressData.district = names[0];
    } else if (["TH", "KH", "VN", "HK", "TW"].includes(formState.value.countryCode || "")) {
      if (names[0]) addressData.province = names[0];
      if (names[1]) addressData.district = names[1];
      if (names[2]) addressData.subdistrict = names[2];
    }
    if (formState.value.address) {
      addressData.address = formState.value.address;
      if (["MY", "SG"].includes(formState.value.countryCode || "")) {
        addressData.addressLineOne = formState.value.address;
      }
    }
    if (formState.value.postcode) addressData.postcode = formState.value.postcode;
    const result = await modifyOrderConsignee(buildConsigneePayload(id.value, { ...formState.value, addressData }) as Partial<OrderFormState> & { id: number });
    emit('submitCallback', result);
    message.success(t('操作成功'));
  } catch (error:any) {
    message.error(error.message);
  } finally {
    emit('update:confirmLoading', false);
  }
};
// 表单提交
const onFormSubmit = () => {
  onSubmit()
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped></style>
