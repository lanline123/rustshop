import { useEffect, useState } from 'react';
import { Button, Input, InputNumber, RadioGroup, Select, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost, isDemo } from '../api';
import FormDrawer from '../framework/FormDrawer';
import ImageUploader from './ImageUploader';
import RichTextEditor from './RichTextEditor';

interface Option {
  id: number;
  name: string;
}

interface ProductFormDrawerProps {
  visible: boolean;
  productId?: number;
  categories: Option[];
  brands: Option[];
  onCancel: () => void;
  onSaved: () => void;
}

interface ShippingTpl {
  shippingTplId: number;
  shippingTplName: string;
}

const emptyValues: Record<string, any> = {
  productType: 1,
  productName: '',
  productSn: '',
  productTsn: '',
  categoryId: undefined,
  brandId: undefined,
  productPrice: 0,
  marketPrice: 0,
  costPrice: 0,
  productStock: 0,
  productStatus: 1,
  productBrief: '',
  productVideo: '',
  productDesc: '',
  remark: '',
  keywords: '',
  productWeight: 0,
  saleMode: 0,
  limitNumber: 0,
  shippingTplId: undefined,
  freeShipping: true,
  isLogistics: true,
  isShopPickup: false,
  noShipping: false,
  isBest: false,
  isNew: false,
  isHot: false,
  virtualSales: 0,
  seoIdentification: '',
  isSupportReturn: true,
  isSupportCod: true,
  gallery: [],
  skuType: 1,
  skuRows: [],
};

function SkuEditor({ formApi, values }: { formApi: any; values: Record<string, any> }) {
  const skuType = Number(values.skuType || 1);
  const rows: Array<Record<string, any>> = values.skuRows || [];

  const setRows = (next: Array<Record<string, any>>) => formApi?.setValue('skuRows', next);

  const updateRow = (index: number, key: string, value: any) => {
    const next = rows.map((row, i) => (i === index ? { ...row, [key]: value } : row));
    setRows(next);
  };

  const addRow = () => {
    setRows([
      ...rows,
      { skuValue: '', skuSn: '', skuPrice: 0, marketPrice: 0, costPrice: 0, skuStock: 0 },
    ]);
  };

  return (
    <div className="product-sku-editor">
      <RadioGroup
        value={skuType}
        onChange={(value) => {
          const next = Number(value);
          formApi?.setValue('skuType', next);
          if (next === 2 && !rows.length) addRow();
        }}
        options={[
          { label: '单规格', value: 1 },
          { label: '多规格', value: 2 },
        ]}
      />
      {skuType === 2 && (
        <div className="product-sku-table">
          <div className="product-sku-row product-sku-head">
            <span>规格值</span>
            <span>SKU编码</span>
            <span>售价</span>
            <span>市场价</span>
            <span>成本价</span>
            <span>库存</span>
            <span />
          </div>
          {rows.map((row, index) => (
            <div className="product-sku-row" key={index}>
              <Input
                value={row.skuValue || ''}
                placeholder="如 红色 / 大号"
                onChange={(value) => updateRow(index, 'skuValue', value)}
              />
              <Input
                value={row.skuSn || ''}
                placeholder="SKU编码"
                onChange={(value) => updateRow(index, 'skuSn', value)}
              />
              <InputNumber
                value={row.skuPrice}
                min={0}
                precision={2}
                onChange={(value) => updateRow(index, 'skuPrice', value)}
              />
              <InputNumber
                value={row.marketPrice}
                min={0}
                precision={2}
                onChange={(value) => updateRow(index, 'marketPrice', value)}
              />
              <InputNumber
                value={row.costPrice}
                min={0}
                precision={2}
                onChange={(value) => updateRow(index, 'costPrice', value)}
              />
              <InputNumber
                value={row.skuStock}
                min={0}
                precision={0}
                onChange={(value) => updateRow(index, 'skuStock', value)}
              />
              <Button
                theme="borderless"
                type="danger"
                size="small"
                onClick={() => setRows(rows.filter((_, i) => i !== index))}
              >
                删除
              </Button>
            </div>
          ))}
          <Button theme="light" onClick={addRow}>
            + 添加规格
          </Button>
        </div>
      )}
    </div>
  );
}

export default function ProductFormDrawer({
  visible,
  productId,
  categories,
  brands,
  onCancel,
  onSaved,
}: ProductFormDrawerProps) {
  const [initialValues, setInitialValues] = useState<Record<string, any>>(emptyValues);
  const [shippingTpls, setShippingTpls] = useState<ShippingTpl[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!visible) return;
    let alive = true;
    if (!productId) {
      setInitialValues(emptyValues);
      return () => {
        alive = false;
      };
    }
    setLoading(true);
    apiGet<Record<string, any>>('/product/product/detail', { id: productId })
      .then((detail) => {
        if (!alive) return;
        const images = (detail.imgList || []).map((item: any) => item.picUrl).filter(Boolean);
        if (!images.length && detail.picUrl) images.push(detail.picUrl);
        setInitialValues({
          ...emptyValues,
          productType: Number(detail.productType || 1),
          productName: detail.productName || '',
          productSn: detail.productSn || '',
          productTsn: detail.productTsn || '',
          categoryId: detail.categoryId || undefined,
          brandId: detail.brandId || undefined,
          productPrice: Number(detail.productPrice || 0),
          marketPrice: Number(detail.marketPrice || 0),
          costPrice: Number(detail.costPrice || 0),
          productStock: Number(detail.productStock || 0),
          productStatus: Number(detail.productStatus ?? 1),
          productBrief: detail.productBrief || '',
          productVideo: detail.productVideo || '',
          productDesc: detail.productDesc || '',
          remark: detail.remark || '',
          keywords: detail.keywords || '',
          productWeight: Number(detail.productWeight || 0),
          saleMode: Number(detail.saleMode || 0),
          limitNumber: Number(detail.limitNumber || 0),
          shippingTplId: detail.shippingTplId || undefined,
          freeShipping: Number(detail.freeShipping || 0) === 1,
          isLogistics: Number(detail.isLogistics ?? 1) === 1,
          isShopPickup: Number(detail.isShopPickup || 0) === 1,
          noShipping: Number(detail.noShipping || 0) === 1,
          isBest: Number(detail.isBest || 0) === 1,
          isNew: Number(detail.isNew || 0) === 1,
          isHot: Number(detail.isHot || 0) === 1,
          virtualSales: Number(detail.virtualSales || 0),
          seoIdentification: detail.seoIdentification || '',
          isSupportReturn: Number(detail.isSupportReturn ?? 1) === 1,
          isSupportCod: Number(detail.isSupportCod ?? 1) === 1,
          gallery: images,
          skuType: Number(detail.skuType || 1),
          skuRows: (detail.productList || []).map((sku: any) => ({
            skuId: sku.skuId,
            skuValue: sku.skuValue || '',
            skuSn: sku.skuSn || '',
            skuPrice: Number(sku.skuPrice || 0),
            marketPrice: Number(sku.marketPrice || 0),
            costPrice: Number(sku.costPrice || 0),
            skuStock: Number(sku.skuStock || 0),
          })),
        });
      })
      .catch((error) => Toast.error({ content: error.message }))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [visible, productId]);

  useEffect(() => {
    if (!visible) return;
    apiGet<ShippingTpl[]>('/product/product/shippingTplList')
      .then((result) => setShippingTpls(result || []))
      .catch(() => undefined);
  }, [visible]);

  const submit = async (payload: Record<string, any>) => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持保存，请登录后操作' });
      return;
    }
    if (payload.skuType === 2) {
      if (!payload.productList.length) throw new Error('请至少添加一个规格');
      if (payload.productList.some((row: any) => !String(row.skuValue || '').trim())) {
        throw new Error('请填写完整的规格值');
      }
    }
    await apiPost(productId ? '/product/product/update' : '/product/product/create', payload);
    Toast.success({ content: productId ? '商品已更新' : '商品已创建' });
    onSaved();
    onCancel();
  };

  return (
    <FormDrawer
      visible={visible}
      mode={productId ? 'edit' : 'create'}
      initialValues={initialValues}
      onCancel={onCancel}
      onSubmit={submit}
      saving={loading}
      config={{
        title: '商品',
        addText: '新增商品',
        width: 950,
        labelWidth: 'auto',
        inputWidth: 420,
        createApi: '/product/product/create',
        updateApi: '/product/product/update',
        idField: 'productId',
        sections: [
          {
            key: 'basic',
            title: '基本信息',
            fields: [
              {
                field: 'productType',
                label: '商品类型',
                type: 'select',
                disabled: Boolean(productId),
                options: [
                  { label: '普通商品', value: 1 },
                  { label: '虚拟商品', value: 2 },
                  { label: '电子卡券', value: 3 },
                  { label: '预约到店', value: 5 },
                  { label: '上门服务', value: 6 },
                ],
                extraText: '商品类型发布后不可修改。',
              },
              {
                field: 'productName',
                label: '商品名称',
                required: true,
                placeholder: '请输入商品名称',
              },
              {
                field: 'categoryId',
                label: '商品类目',
                type: 'select',
                required: true,
                placeholder: '请选择商品类目',
                options: categories.map((item) => ({ label: item.name, value: item.id })),
              },
              {
                field: 'brandId',
                label: '商品品牌',
                type: 'select',
                placeholder: '请选择商品品牌',
                options: brands.map((item) => ({ label: item.name, value: item.id })),
              },
              { field: 'productSn', label: '商品编码', placeholder: '不填自动生成' },
              { field: 'productTsn', label: '商品条码', placeholder: '如 69 开头的 13 位标准码' },
              { field: 'productWeight', label: '商品重量', type: 'number' },
              {
                field: 'productStatus',
                label: '是否上架',
                type: 'radio',
                options: [
                  { label: '上架', value: 1 },
                  { label: '下架', value: 0 },
                ],
              },
              {
                field: 'gallery',
                label: '商品图片',
                type: 'custom',
                render: ({ formApi, values }) => (
                  <ImageUploader
                    value={values.gallery || []}
                    onChange={(urls) => formApi?.setValue('gallery', urls)}
                  />
                ),
              },
              { field: 'productVideo', label: '商品视频', placeholder: '视频地址，可留空' },
              { field: 'keywords', label: '搜索关键词', placeholder: '多个关键词用空格分隔' },
              { field: 'productBrief', label: '商品描述', type: 'textarea' },
            ],
          },
          {
            key: 'sale',
            title: '销售信息',
            fields: [
              {
                field: 'saleMode',
                label: '销售模式',
                type: 'radio',
                options: [
                  { label: '现货销售', value: 0 },
                  { label: '预售模式', value: 1 },
                ],
              },
              { field: 'productPrice', label: '销售价', type: 'number' },
              { field: 'marketPrice', label: '市场价', type: 'number' },
              { field: 'costPrice', label: '成本价', type: 'number' },
              { field: 'productStock', label: '库存', type: 'number' },
              { field: 'limitNumber', label: '限购数量', type: 'number' },
              { field: 'virtualSales', label: '虚拟销量', type: 'number' },
              {
                field: 'skuType',
                label: '规格类型',
                type: 'custom',
                render: ({ formApi, values }) => <SkuEditor formApi={formApi} values={values} />,
              },
            ],
          },
          {
            key: 'logistics',
            title: '物流及配送',
            fields: [
              {
                field: 'shippingTplId',
                label: '运费模板',
                type: 'select',
                options: shippingTpls.map((item) => ({
                  label: item.shippingTplName,
                  value: item.shippingTplId,
                })),
              },
              {
                field: 'freeShipping',
                label: '是否包邮',
                type: 'switch',
                checkedText: '包邮',
                uncheckedText: '自定义运费',
              },
              {
                field: 'isLogistics',
                label: '支持物流',
                type: 'switch',
                checkedText: '支持',
                uncheckedText: '不支持',
              },
              {
                field: 'isShopPickup',
                label: '支持自提',
                type: 'switch',
                checkedText: '支持',
                uncheckedText: '不支持',
              },
              {
                field: 'noShipping',
                label: '无需配送',
                type: 'switch',
                checkedText: '无需',
                uncheckedText: '需要',
              },
              {
                field: 'isSupportReturn',
                label: '支持退货',
                type: 'switch',
                checkedText: '支持',
                uncheckedText: '不支持',
              },
              {
                field: 'isSupportCod',
                label: '支持货到付款',
                type: 'switch',
                checkedText: '支持',
                uncheckedText: '不支持',
              },
            ],
          },
          {
            key: 'detail',
            title: '商品详情',
            fields: [
              {
                field: 'productDesc',
                label: '商品详情',
                type: 'custom',
                render: ({ formApi, values }) => (
                  <RichTextEditor
                    value={values.productDesc || ''}
                    onChange={(html) => formApi?.setValue('productDesc', html)}
                    height={420}
                  />
                ),
              },
            ],
          },
          {
            key: 'advanced',
            title: '高级信息',
            fields: [
              {
                field: 'isBest',
                label: '精品',
                type: 'switch',
                checkedText: '是',
                uncheckedText: '否',
              },
              {
                field: 'isNew',
                label: '新品',
                type: 'switch',
                checkedText: '是',
                uncheckedText: '否',
              },
              {
                field: 'isHot',
                label: '热销',
                type: 'switch',
                checkedText: '是',
                uncheckedText: '否',
              },
              { field: 'seoIdentification', label: 'SEO标识' },
              { field: 'remark', label: '备注', type: 'textarea' },
            ],
          },
        ],
        toPayload: (values) => ({
          productId: productId || undefined,
          ...values,
          picUrl: (values.gallery || [])[0] || '',
          imgList: (values.gallery || []).map((picUrl: string) => ({ picUrl })),
          freeShipping: values.freeShipping ? 1 : 0,
          isLogistics: values.isLogistics ? 1 : 0,
          isShopPickup: values.isShopPickup ? 1 : 0,
          noShipping: values.noShipping ? 1 : 0,
          isBest: values.isBest ? 1 : 0,
          isNew: values.isNew ? 1 : 0,
          isHot: values.isHot ? 1 : 0,
          isSupportReturn: values.isSupportReturn ? 1 : 0,
          isSupportCod: values.isSupportCod ? 1 : 0,
          skuType: Number(values.skuType || 1),
          productList:
            Number(values.skuType) === 2
              ? (values.skuRows || []).map((row: any) => ({
                  skuId: row.skuId,
                  skuValue: row.skuValue,
                  skuSn: row.skuSn,
                  skuPrice: row.skuPrice,
                  marketPrice: row.marketPrice,
                  costPrice: row.costPrice,
                  skuStock: row.skuStock,
                  isEnabled: 1,
                  skuData: [{ name: '规格', value: row.skuValue }],
                  attrValues: [row.skuValue],
                }))
              : [],
        }),
      }}
    />
  );
}
