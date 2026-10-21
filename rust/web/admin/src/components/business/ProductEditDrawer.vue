<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '编辑商品' : '新增商品'"
    size="88%"
    destroy-on-close
    :close-on-click-modal="false"
  >
    <div v-loading="loading" class="product-edit-drawer">
      <el-tabs v-model="activeTab">
        <!-- 基础信息 -->
        <el-tab-pane label="基础信息" name="base">
          <el-form label-width="120px" class="edit-form">
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="商品名称" required>
                  <el-input v-model="form.productName" placeholder="请输入商品名称" maxlength="120" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="商品货号">
                  <el-input v-model="form.productSn" placeholder="不填则系统自动生成" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="商品分类" required>
                  <el-cascader
                    v-model="form.categoryId"
                    :options="catOptions"
                    :props="{ emitPath: false, checkStrictly: true }"
                    placeholder="请选择商品分类"
                    clearable
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="品牌">
                  <el-select v-model="form.brandId" placeholder="请选择品牌" clearable filterable style="width: 100%">
                    <el-option v-for="b in brandOptions" :key="b.value" :label="b.label" :value="b.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="销售价" required>
                  <el-input-number v-model="form.productPrice" :min="0" :precision="2" :step="0.01" />
                  <span class="unit">元</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="市场价">
                  <el-input-number v-model="form.marketPrice" :min="0" :precision="2" :step="0.01" />
                  <span class="unit">元</span>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="库存">
                  <el-input-number v-model="form.productStock" :min="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="排序权重">
                  <el-input-number v-model="form.sortOrder" :min="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="商品主图">
                  <ImageUpload v-model="form.picUrl" tip="支持 jpg/png，建议 800x800" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="上架销售">
                  <el-switch v-model="form.productStatus" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="推荐商品">
                  <el-switch v-model="form.isBest" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="新品">
                  <el-switch v-model="form.isNew" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="热销">
                  <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="商品卖点">
                  <el-input v-model="form.productBrief" type="textarea" :rows="3" placeholder="列表/详情展示的商品卖点" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 相册 / 描述 / 视频 -->
        <el-tab-pane label="相册与详情" name="gallery">
          <div class="section-block">
            <div class="section-head">
              <span class="section-title">商品相册（多图）</span>
              <el-button type="primary" size="small" @click="addImage">添加图片</el-button>
            </div>
            <div class="image-list-grid">
              <div v-for="(img, idx) in images" :key="idx" class="image-row">
                <ImageUpload v-model="img.picUrl" />
                <div class="image-actions">
                  <span class="image-index">第 {{ idx + 1 }} 张</span>
                  <el-button type="danger" link size="small" @click="images.splice(idx, 1)">删除</el-button>
                </div>
              </div>
              <el-empty v-if="images.length === 0" description="暂无相册图片，点击右上角添加" :image-size="60" />
            </div>
          </div>

          <div class="section-block">
            <div class="section-head">
              <span class="section-title">商品描述</span>
            </div>
            <el-input
              v-model="form.productDesc"
              type="textarea"
              :rows="6"
              placeholder="支持纯文本描述；保存后写入 product_desc"
            />
          </div>

          <div class="section-block">
            <div class="section-head">
              <span class="section-title">商品视频（可选）</span>
            </div>
            <div class="video-row">
              <el-input v-model="form.videoUrl" placeholder="视频 URL（素材库上传后回填）" style="max-width: 520px" />
              <el-button type="primary" link @click="form.videoUrl = ''">清空</el-button>
            </div>
            <div class="video-row">
              <ImageUpload v-model="form.videoCover" tip="视频封面（可选）" />
            </div>
          </div>
        </el-tab-pane>

        <!-- 规格与属性 -->
        <el-tab-pane label="规格与属性" name="attrs">
          <div class="section-block">
            <div class="section-head">
              <span class="section-title">多规格商品</span>
              <el-switch v-model="multiSpec" @change="handleMultiSpecChange" />
              <span class="section-tip">开启后按规格值生成 SKU 列表；关闭则按基础价格/库存单规格销售</span>
            </div>
          </div>

          <template v-if="multiSpec">
            <div class="section-block">
              <div class="section-head">
                <span class="section-title">规格属性（如 颜色 / 尺码）</span>
                <el-button type="primary" size="small" @click="addSpecGroup">添加规格</el-button>
              </div>
              <div v-for="(group, gi) in specGroups" :key="gi" class="spec-group">
                <div class="spec-group-head">
                  <el-input v-model="group.attrName" placeholder="规格名，如 颜色" style="width: 220px" />
                  <el-button type="danger" link size="small" @click="specGroups.splice(gi, 1)">删除规格</el-button>
                </div>
                <div class="spec-values">
                  <div v-for="(val, vi) in group.values" :key="vi" class="spec-value-row">
                    <el-input v-model="val.attrValue" placeholder="规格值，如 红色" style="width: 200px" size="small" />
                    <el-input-number
                      v-model="val.attrPrice"
                      :min="0"
                      :precision="2"
                      :step="0.01"
                      size="small"
                      placeholder="加价"
                      style="width: 130px"
                    />
                    <el-button type="danger" link size="small" @click="group.values.splice(vi, 1)">删除</el-button>
                  </div>
                  <el-button type="primary" link size="small" @click="group.values.push({ attrValue: '' })">
                    + 添加规格值
                  </el-button>
                </div>
              </div>
              <el-empty v-if="specGroups.length === 0" description="请添加规格（如颜色、尺码）" :image-size="60" />
            </div>

            <div class="section-block">
              <div class="section-head">
                <span class="section-title">SKU 列表</span>
                <el-button type="primary" size="small" @click="buildSkus">按规格生成 / 刷新 SKU</el-button>
              </div>
              <el-table :data="skuRows" border size="small">
                <el-table-column prop="skuValue" label="规格" min-width="180" />
                <el-table-column label="SKU 货号" width="180">
                  <template #default="{ row }">
                    <el-input v-model="row.skuSn" size="small" placeholder="选填" />
                  </template>
                </el-table-column>
                <el-table-column label="销售价" width="150">
                  <template #default="{ row }">
                    <el-input-number v-model="row.skuPrice" :min="0" :precision="2" :step="0.01" size="small" />
                  </template>
                </el-table-column>
                <el-table-column label="库存" width="130">
                  <template #default="{ row }">
                    <el-input-number v-model="row.skuStock" :min="0" size="small" />
                  </template>
                </el-table-column>
                <el-table-column label="启用" width="90" align="center">
                  <template #default="{ row }">
                    <el-switch v-model="row.isEnabled" :active-value="1" :inactive-value="0" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                  <template #default="{ $index }">
                    <el-button type="danger" link size="small" @click="skuRows.splice($index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>

          <div class="section-block">
            <div class="section-head">
              <span class="section-title">商品参数（普通属性）</span>
              <el-button type="primary" size="small" @click="normalAttrs.push({ name: '', value: '' })">添加参数</el-button>
            </div>
            <div v-for="(item, idx) in normalAttrs" :key="idx" class="normal-row">
              <el-input v-model="item.name" placeholder="参数名，如 产地" style="width: 220px" size="small" />
              <el-input v-model="item.value" placeholder="参数值，如 中国" style="width: 320px" size="small" />
              <el-button type="danger" link size="small" @click="normalAttrs.splice(idx, 1)">删除</el-button>
            </div>
            <el-empty v-if="normalAttrs.length === 0" description="暂无商品参数" :image-size="60" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSave">
        {{ isEdit ? '保存修改' : '创建商品' }}
      </el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
/**
 * 商品新增/编辑抽屉（多规格 SKU + 规格属性 + 相册 + 描述 + 视频）。
 *
 * 字段与 Rust 侧 `ProductSaveDTO` / `ProductSkuSaveDTO` / `ProductAttrsVO` /
 * `GalleryPicDTO` / `ProductVideoVO` 对齐（camelCase）。
 *
 * 接口：
 * - `GET /adminapi/product/product/detail?id=`（编辑回填）
 * - `GET /adminapi/product/product/catAndBrandList`（分类树/品牌）
 * - `POST /adminapi/product/product/create` / `update`
 */
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import ImageUpload from '@/components/widgets/ImageUpload.vue';
import request from '@/utils/request';
import type { FormOption } from '@/components/schema/types';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    productId?: number | null;
  }>(),
  { productId: null }
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'saved'): void;
}>();

const visible = computed({
  get: () => props.modelValue,
  set: (v: boolean) => emit('update:modelValue', v)
});

const isEdit = computed(() => props.productId != null && props.productId > 0);

// ————————————————————— 数据类型 —————————————————————
interface ImageItem {
  picUrl: string;
  picThumb?: string;
}

interface SpecValue {
  attributesId?: number;
  attrValue: string;
  attrPrice?: number;
  attrPicThumb?: string;
}

interface SpecGroup {
  attrName: string;
  values: SpecValue[];
}

interface SkuRow {
  skuId?: number;
  skuValue: string;
  skuData: Array<{ name: string; value: string; attrPicThumb?: string }>;
  skuSn: string;
  skuPrice: number;
  skuStock: number;
  isEnabled: number;
}

interface NormalAttr {
  name: string;
  value: string;
}

// ————————————————————— 状态 —————————————————————
const activeTab = ref<'base' | 'gallery' | 'attrs'>('base');
const loading = ref(false);
const submitting = ref(false);
const catOptions = ref<FormOption[]>([]);
const brandOptions = ref<FormOption[]>([]);

const form = reactive({
  productId: null as number | null,
  productName: '',
  productSn: '',
  categoryId: null as number | null,
  brandId: null as number | null,
  productPrice: 0,
  marketPrice: 0,
  productStock: 0,
  sortOrder: 50,
  productBrief: '',
  picUrl: '',
  productStatus: 1,
  isBest: 0,
  isNew: 0,
  isHot: 0,
  productDesc: '',
  videoUrl: '',
  videoCover: ''
});

const images = ref<ImageItem[]>([]);
const multiSpec = ref(false);
const specGroups = ref<SpecGroup[]>([]);
const skuRows = ref<SkuRow[]>([]);
const normalAttrs = ref<NormalAttr[]>([]);

function addImage() {
  images.value.push({ picUrl: '', picThumb: '' });
}

// ————————————————————— 分类/品牌 —————————————————————
async function loadCatAndBrand() {
  if (catOptions.value.length > 0) return;
  const catRes = await request<any>({
    url: '/product/category/list',
    method: 'get',
    params: { page: 1, size: 100 }
  });
  const cats: any[] = catRes?.list ?? [];
  const nodes = new Map<number, FormOption>();
  for (const c of cats) {
    nodes.set(c.categoryId, { label: c.categoryName, value: c.categoryId, children: [] });
  }
  const roots: FormOption[] = [];
  for (const c of cats) {
    const node = nodes.get(c.categoryId);
    if (!node) continue;
    const parent = c.parentId ? nodes.get(c.parentId) : undefined;
    if (parent) {
      parent.children = parent.children || [];
      parent.children.push(node);
    } else {
      roots.push(node);
    }
  }
  catOptions.value = roots;

  const brandRes = await request<any>({
    url: '/product/brand/list',
    method: 'get',
    params: { page: 1, size: 100 }
  });
  brandOptions.value = (brandRes?.list ?? []).map((b: any) => ({
    label: b.brandName,
    value: b.brandId
  }));
}

// ————————————————————— 打开 / 回填 —————————————————————
function resetForm() {
  activeTab.value = 'base';
  Object.assign(form, {
    productId: null,
    productName: '',
    productSn: '',
    categoryId: null,
    brandId: null,
    productPrice: 0,
    marketPrice: 0,
    productStock: 0,
    sortOrder: 50,
    productBrief: '',
    picUrl: '',
    productStatus: 1,
    isBest: 0,
    isNew: 0,
    isHot: 0,
    productDesc: '',
    videoUrl: '',
    videoCover: ''
  });
  images.value = [];
  multiSpec.value = false;
  specGroups.value = [];
  skuRows.value = [];
  normalAttrs.value = [];
}

async function loadDetail(productId: number) {
  const d = await request<any>({ url: '/product/product/detail', method: 'get', params: { id: productId } });
  if (!d) return;
  form.productId = d.productId ?? productId;
  form.productName = d.productName ?? '';
  form.productSn = d.productSn ?? '';
  form.categoryId = d.categoryId > 0 ? d.categoryId : null;
  form.brandId = d.brandId > 0 ? d.brandId : null;
  form.productPrice = Number(d.productPrice ?? 0);
  form.marketPrice = Number(d.marketPrice ?? 0);
  form.productStock = Number(d.productStock ?? 0);
  form.sortOrder = Number(d.sortOrder ?? 50);
  form.productBrief = d.productBrief ?? '';
  form.picUrl = d.picUrl ?? '';
  form.productStatus = Number(d.productStatus ?? 1);
  form.isBest = Number(d.isBest ?? 0);
  form.isNew = Number(d.isNew ?? 0);
  form.isHot = Number(d.isHot ?? 0);
  form.productDesc = d.productDesc ?? '';
  images.value = (d.imgList ?? []).map((p: any) => ({
    picUrl: p.picUrl ?? '',
    picThumb: p.picThumb ?? p.picUrl ?? ''
  }));

  const attrs = d.attrList ?? {};
  specGroups.value = (attrs.spe ?? []).map((g: any) => ({
    attrName: g.attrName ?? '',
    values: (g.attrList ?? []).map((a: any) => ({
      attributesId: a.attributesId ?? undefined,
      attrValue: a.attrValue ?? '',
      attrPrice: a.attrPrice != null ? Number(a.attrPrice) : undefined,
      attrPicThumb: a.attrPicThumb ?? undefined
    }))
  }));
  normalAttrs.value = (attrs.normal ?? []).flatMap((g: any) =>
    (g.attrList ?? []).map((a: any) => ({
      name: g.attrName ?? a.attrName ?? '',
      value: a.attrValue ?? ''
    }))
  );
  const skus: any[] = d.productList ?? [];
  skuRows.value = skus.map((s: any) => ({
    skuId: s.skuId ?? undefined,
    skuValue: s.skuValue ?? '',
    skuData: Array.isArray(s.skuData)
      ? s.skuData.map((x: any) => ({ name: x.name ?? '', value: x.value ?? '', attrPicThumb: x.attrPicThumb }))
      : [],
    skuSn: s.skuSn ?? '',
    skuPrice: Number(s.skuPrice ?? 0),
    skuStock: Number(s.skuStock ?? 0),
    isEnabled: Number(s.isEnabled ?? 1)
  }));
  multiSpec.value = Number(d.skuType ?? 0) === 2 || skuRows.value.length > 1;
  if (multiSpec.value && skuRows.value.length === 0) buildSkus();

  const videos: any[] = d.productVideoInfo ?? [];
  if (videos.length > 0) {
    form.videoUrl = videos[0].videoUrl ?? '';
    form.videoCover = videos[0].videoCover ?? '';
  }
}

async function open() {
  activeTab.value = 'base';
  if (isEdit.value) {
    loading.value = true;
    try {
      await loadCatAndBrand();
      await loadDetail(props.productId as number);
    } finally {
      loading.value = false;
    }
  } else {
    resetForm();
    await loadCatAndBrand();
  }
}

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      resetForm();
      void open();
    }
  },
  { immediate: false }
);

// ————————————————————— 多规格 / SKU —————————————————————
function addSpecGroup() {
  specGroups.value.push({ attrName: '', values: [{ attrValue: '' }] });
}

function handleMultiSpecChange(val: string | number | boolean) {
  if (val && skuRows.value.length === 0) {
    if (specGroups.value.length === 0) {
      specGroups.value.push({ attrName: '', values: [{ attrValue: '' }] });
    }
  }
}

/** 按规格值做笛卡尔积生成 SKU，保留同规格值已有的货号/价格/库存/启用状态。 */
function buildSkus() {
  const groups = specGroups.value
    .map((g) => ({
      name: g.attrName.trim(),
      values: g.values.filter((v) => v.attrValue.trim() !== '')
    }))
    .filter((g) => g.name !== '' && g.values.length > 0);

  if (groups.length === 0) {
    ElMessage.warning('请先填写规格名与规格值');
    return;
  }

  const oldMap = new Map<string, SkuRow>();
  for (const row of skuRows.value) oldMap.set(row.skuValue, row);

  const combos: Array<Array<{ name: string; value: string; attrPicThumb?: string }>> = [];
  const walk = (idx: number, acc: Array<{ name: string; value: string; attrPicThumb?: string }>) => {
    if (idx === groups.length) {
      combos.push([...acc]);
      return;
    }
    const group = groups[idx];
    for (const v of group.values) {
      acc.push({ name: group.name, value: v.attrValue, attrPicThumb: v.attrPicThumb });
      walk(idx + 1, acc);
      acc.pop();
    }
  };
  walk(0, []);

  skuRows.value = combos.map((items) => {
    const skuValue = items.map((i) => i.value).join(' ');
    const old = oldMap.get(skuValue);
    return {
      skuId: old?.skuId,
      skuValue,
      skuData: items,
      skuSn: old?.skuSn ?? '',
      skuPrice: old?.skuPrice ?? Number(form.productPrice ?? 0),
      skuStock: old?.skuStock ?? 0,
      isEnabled: old?.isEnabled ?? 1
    };
  });
}

// ————————————————————— 保存 —————————————————————
function buildNormalAttrList() {
  const map = new Map<string, NormalAttr[]>();
  for (const item of normalAttrs.value) {
    const name = item.name.trim();
    if (!name || !item.value.trim()) continue;
    const list = map.get(name) ?? [];
    list.push({ name, value: item.value.trim() });
    map.set(name, list);
  }
  return Array.from(map.entries()).map(([attrName, list]) => ({
    attrName,
    attrList: list.map((item, idx) => ({
      attrType: 0,
      attrName,
      attrValue: item.value,
      sortOrder: idx
    }))
  }));
}

function buildSpecAttrList() {
  return specGroups.value
    .filter((g) => g.attrName.trim() !== '')
    .map((g) => ({
      attrName: g.attrName.trim(),
      attrList: g.values
        .filter((v) => v.attrValue.trim() !== '')
        .map((v, idx) => ({
          attributesId: v.attributesId,
          attrType: 1,
          attrName: g.attrName.trim(),
          attrValue: v.attrValue.trim(),
          attrPrice: v.attrPrice,
          attrPicThumb: v.attrPicThumb,
          sortOrder: idx
        }))
    }));
}

function buildPayload() {
  const payload: Record<string, any> = {
    productType: 1,
    saleMode: 0,
    deliveryMode: 1,
    freeShipping: 0,
    noShipping: 0,
    productName: form.productName.trim(),
    productSn: form.productSn.trim(),
    categoryId: form.categoryId ?? 0,
    brandId: form.brandId ?? 0,
    productPrice: Number(form.productPrice) || 0,
    marketPrice: Number(form.marketPrice) || 0,
    productStock: Number(form.productStock) || 0,
    sortOrder: Number(form.sortOrder) || 0,
    productBrief: form.productBrief,
    picUrl: form.picUrl,
    picThumb: form.picUrl,
    productStatus: form.productStatus,
    isBest: form.isBest,
    isNew: form.isNew,
    isHot: form.isHot,
    productDesc: form.productDesc,
    productDescArr: form.productDesc ? [{ type: 'text', html: form.productDesc }] : [],
    imgList: images.value
      .filter((i) => i.picUrl)
      .map((i, idx) => ({ picUrl: i.picUrl, picThumb: i.picThumb || i.picUrl, picSort: idx })),
    productVideoInfo: form.videoUrl
      ? [{ videoUrl: form.videoUrl, videoCover: form.videoCover }]
      : [],
    skuType: multiSpec.value ? 2 : 1,
    attrList: {
      normal: buildNormalAttrList(),
      spe: multiSpec.value ? buildSpecAttrList() : [],
      extra: []
    },
    productList: multiSpec.value
      ? skuRows.value.map((row) => ({
          skuId: row.skuId,
          skuValue: row.skuValue,
          skuData: row.skuData,
          skuSn: row.skuSn,
          skuPrice: Number(row.skuPrice) || 0,
          skuStock: Number(row.skuStock) || 0,
          isEnabled: row.isEnabled
        }))
      : []
  };
  if (isEdit.value) payload.productId = props.productId;
  return payload;
}

async function handleSave() {
  if (!form.productName.trim()) {
    activeTab.value = 'base';
    ElMessage.warning('请输入商品名称');
    return;
  }
  if (!form.categoryId) {
    activeTab.value = 'base';
    ElMessage.warning('请选择商品分类');
    return;
  }
  if (Number(form.productPrice) <= 0 && !multiSpec.value) {
    activeTab.value = 'base';
    ElMessage.warning('请输入销售价');
    return;
  }
  if (multiSpec.value) {
    const attrList = buildSpecAttrList();
    if (attrList.length === 0 || attrList.every((g) => g.attrList.length === 0)) {
      activeTab.value = 'attrs';
      ElMessage.warning('多规格商品请先填写规格名与规格值');
      return;
    }
    if (skuRows.value.length === 0) {
      buildSkus();
      if (skuRows.value.length === 0) {
        activeTab.value = 'attrs';
        ElMessage.warning('请先生成 SKU 列表');
        return;
      }
    }
  }

  submitting.value = true;
  try {
    const payload = buildPayload();
    await request({
      url: isEdit.value ? '/product/product/update' : '/product/product/create',
      method: 'post',
      data: payload
    });
    ElMessage.success(isEdit.value ? '商品保存成功' : '商品创建成功');
    visible.value = false;
    emit('saved');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped lang="less">
.product-edit-drawer {
  --edit-gap: 18px;
  padding: 0 8px 8px;
}

.edit-form {
  max-width: 100%;
}

.unit {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
}

.section-block {
  margin-bottom: var(--edit-gap);
  padding: 14px 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: #fff;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.section-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.image-list-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.image-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.image-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.image-index {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.video-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.spec-group {
  padding: 10px 12px;
  margin-bottom: 10px;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
}

.spec-group-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.spec-values {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.spec-value-row,
.normal-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
