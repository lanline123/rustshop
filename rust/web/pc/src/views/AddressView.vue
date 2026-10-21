<template>
  <div class="tig-address-page">
    <div class="container member-layout">
      <MemberNav />
      <div class="info-row">
    <div class="page-head flex-between">
      <h1 class="page-title">收货地址</h1>
      <div class="head-actions">
        <router-link to="/member" class="back-link">返回个人中心</router-link>
        <el-button type="primary" @click="openCreate">新增收货地址</el-button>
      </div>
    </div>

    <div v-loading="loading" class="address-body">
      <el-empty v-if="!addresses.length && !loading" description="还没有收货地址">
        <el-button type="primary" @click="openCreate">新增收货地址</el-button>
      </el-empty>

      <div v-else class="address-grid">
        <div v-for="addr in addresses" :key="addr.addressId" class="address-card" :class="{ default: addr.isDefault === 1 }">
          <div class="card-head flex-between">
            <div class="head-left">
              <span class="consignee">{{ addr.consignee }}</span>
              <span class="mobile">{{ addr.mobile }}</span>
            </div>
            <el-tag v-if="addr.isDefault === 1" size="small" type="danger">默认地址</el-tag>
            <el-tag v-else-if="addr.isSelected === 1" size="small">已选中</el-tag>
          </div>
          <div class="card-region">{{ addr.regionName }}</div>
          <div class="card-address">{{ addr.address }}</div>
          <div v-if="addr.addressTag" class="card-tag">
            <el-tag size="small" type="info">{{ addr.addressTag }}</el-tag>
          </div>
          <div class="card-actions">
            <el-button link type="primary" @click="openEdit(addr)">编辑</el-button>
            <el-button v-if="addr.isDefault !== 1" link @click="setSelected(addr)">设为默认</el-button>
            <el-button link type="danger" @click="remove(addr)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑收货地址' : '新增收货地址'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="form.consignee" placeholder="请输入收货人姓名" maxlength="30" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>
        <el-form-item label="所在地区" prop="regionIds">
          <div class="region-selects">
            <el-select v-model="provinceId" placeholder="省份" style="width: 32%" @change="onProvinceChange">
              <el-option v-for="r in provinces" :key="r.region_id" :label="r.region_name" :value="r.region_id" />
            </el-select>
            <el-select v-model="cityId" placeholder="城市" style="width: 32%" :disabled="!provinceId" @change="onCityChange">
              <el-option v-for="r in cities" :key="r.region_id" :label="r.region_name" :value="r.region_id" />
            </el-select>
            <el-select v-model="districtId" placeholder="区/县" style="width: 32%" :disabled="!cityId">
              <el-option v-for="r in districts" :key="r.region_id" :label="r.region_name" :value="r.region_id" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="街道、门牌号等" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="邮政编码">
          <el-input v-model="form.postcode" placeholder="选填" maxlength="20" />
        </el-form-item>
        <el-form-item label="地址标签">
          <el-radio-group v-model="form.addressTag">
            <el-radio v-for="tag in tags" :key="tag" :value="tag">{{ tag }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="isDefaultChecked" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { addressApi, regionApi } from '@/api';
import { pageItems } from '@/api/types';
import type { AddressPayload, AddressVO, Region } from '@/api/types';
import { applySeo } from '@/composables/useSeo';
import { useUserStore } from '@/stores/user';
import MemberNav from '@/components/MemberNav.vue';

const user = useUserStore();

const loading = ref(false);
const saving = ref(false);
const addresses = ref<AddressVO[]>([]);
const tags = ref<string[]>([]);

const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();
const form = reactive<AddressPayload>({
  consignee: '',
  mobile: '',
  address: '',
  postcode: '',
  addressTag: '',
  regionIds: [],
});
const isDefaultChecked = ref(false);

const provinces = ref<Region[]>([]);
const cities = ref<Region[]>([]);
const districts = ref<Region[]>([]);
const provinceId = ref<number | undefined>(undefined);
const cityId = ref<number | undefined>(undefined);
const districtId = ref<number | undefined>(undefined);

const rules: FormRules = {
  consignee: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$|^\d{6,20}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  regionIds: [{ required: true, message: '请选择所在地区', trigger: 'change' }],
};

async function loadAddresses() {
  loading.value = true;
  try {
    const page = await addressApi.list();
    addresses.value = pageItems(page);
  } catch {
    addresses.value = [];
  } finally {
    loading.value = false;
  }
}

async function loadTags() {
  try {
    tags.value = await addressApi.tags();
  } catch {
    tags.value = [];
  }
}

async function loadProvinces() {
  if (provinces.value.length) return;
  provinces.value = await regionApi.provinces();
}

async function onProvinceChange(value: number) {
  cityId.value = undefined;
  districtId.value = undefined;
  cities.value = [];
  districts.value = [];
  if (!value) return;
  cities.value = await regionApi.children(value);
}

async function onCityChange(value: number) {
  districtId.value = undefined;
  districts.value = [];
  if (!value) return;
  districts.value = await regionApi.children(value);
}

async function restoreRegion(regionIds: number[]) {
  await loadProvinces();
  provinceId.value = regionIds[0];
  cityId.value = undefined;
  districtId.value = undefined;
  cities.value = [];
  districts.value = [];
  if (regionIds[0] === undefined) return;
  cities.value = await regionApi.children(regionIds[0]);
  if (regionIds[1] !== undefined) {
    cityId.value = regionIds[1];
    districts.value = await regionApi.children(regionIds[1]);
    if (regionIds[2] !== undefined) {
      districtId.value = regionIds[2];
    }
  }
}

function resetForm() {
  form.consignee = '';
  form.mobile = '';
  form.address = '';
  form.postcode = '';
  form.addressTag = tags.value[0] ?? '';
  form.regionIds = [];
  isDefaultChecked.value = addresses.value.length === 0;
  provinceId.value = undefined;
  cityId.value = undefined;
  districtId.value = undefined;
  cities.value = [];
  districts.value = [];
}

async function openCreate() {
  editingId.value = null;
  resetForm();
  await loadProvinces();
  dialogVisible.value = true;
}

async function openEdit(addr: AddressVO) {
  editingId.value = addr.addressId;
  form.consignee = addr.consignee;
  form.mobile = addr.mobile;
  form.address = addr.address;
  form.postcode = addr.postcode ?? '';
  form.addressTag = addr.addressTag || (tags.value[0] ?? '');
  isDefaultChecked.value = addr.isDefault === 1;
  form.regionIds = Array.isArray(addr.regionIds) ? addr.regionIds : [];
  dialogVisible.value = true;
  await restoreRegion(form.regionIds);
}

async function save() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  const regionIds = [provinceId.value, cityId.value, districtId.value].filter(
    (id): id is number => typeof id === 'number' && id > 0,
  );
  if (regionIds.length === 0) {
    ElMessage.warning('请选择所在地区');
    return;
  }
  saving.value = true;
  try {
    const payload: AddressPayload = {
      consignee: form.consignee,
      mobile: form.mobile,
      address: form.address,
      postcode: form.postcode || undefined,
      addressTag: form.addressTag || undefined,
      regionIds,
      isDefault: isDefaultChecked.value ? 1 : 0,
    };
    if (editingId.value) {
      await addressApi.update({ ...payload, addressId: editingId.value });
      ElMessage.success('地址已更新');
    } else {
      await addressApi.create(payload);
      ElMessage.success('地址已新增');
    }
    dialogVisible.value = false;
    await loadAddresses();
  } finally {
    saving.value = false;
  }
}

async function setSelected(addr: AddressVO) {
  await addressApi.setSelected(addr.addressId);
  ElMessage.success('已设为默认地址');
  await loadAddresses();
}

async function remove(addr: AddressVO) {
  await ElMessageBox.confirm('确定删除该收货地址？', '删除地址', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  });
  await addressApi.remove(addr.addressId);
  ElMessage.success('地址已删除');
  await loadAddresses();
}

onMounted(async () => {
  applySeo({ title: '收货地址', noindex: true });
  if (!user.token) {
    ElMessage.warning('请先登录');
    return;
  }
  await Promise.all([loadAddresses(), loadTags()]);
});
</script>

<style scoped lang="less">
.tig-address-page {
  padding: 20px 0 50px;
}

.page-head {
  margin-bottom: 16px;

  .page-title { font-size: 22px; font-weight: 700; }
  .head-actions { display: flex; align-items: center; gap: 16px; }
  .back-link { color: #888; font-size: 13px; &:hover { color: var(--tig-red); } }
}

.address-body { min-height: 220px; }

.address-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.address-card {
  background: #fff;
  border: 1px solid var(--tig-border-color);
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: border-color 0.2s;

  &.default { border-color: #ffb7b3; }

  .card-head {
    .head-left {
      display: flex;
      align-items: baseline;
      gap: 10px;
      .consignee { font-size: 15px; font-weight: 700; }
      .mobile { color: #888; font-size: 13px; }
    }
  }

  .card-region { color: #666; font-size: 13px; }
  .card-address { color: #333; font-size: 13px; line-height: 1.5; min-height: 38px; }
  .card-tag { margin-top: 2px; }

  .card-actions {
    display: flex;
    justify-content: flex-end;
    gap: 4px;
    border-top: 1px dashed #f0f0f0;
    padding-top: 8px;
    margin-top: 4px;
  }
}

.region-selects { display: flex; gap: 8px; width: 100%; }

@media (max-width: 1280px) {
  .address-grid { grid-template-columns: repeat(2, 1fr); }
}

.member-layout {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.info-row {
  flex: 1;
  min-width: 0;
}
</style>
