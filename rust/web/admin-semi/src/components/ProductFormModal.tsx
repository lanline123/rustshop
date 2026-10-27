import { useEffect, useState } from 'react';
import {
  Button,
  Col,
  Form,
  Input,
  InputNumber,
  RadioGroup,
  Row,
  Select,
  SideSheet,
  Switch,
  Toast,
} from '@douyinfe/semi-ui';
import { IconPlus } from '@douyinfe/semi-icons';
import { apiGet, apiPost, isDemo } from '../api';
import ImageUploader from './ImageUploader';
import RichTextEditor from './RichTextEditor';

interface Option {
  id: number;
  name: string;
}

interface ProductFormModalProps {
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

const emptyValues = {
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
};

const sections = [
  { key: 'basic', label: '基本信息' },
  { key: 'sale', label: '销售信息' },
  { key: 'logistics', label: '物流及配送' },
  { key: 'detail', label: '商品详情' },
  { key: 'advanced', label: '高级信息' },
];

export default function ProductFormModal({
  visible,
  productId,
  categories,
  brands,
  onCancel,
  onSaved,
}: ProductFormModalProps) {
  const [formApi, setFormApi] = useState<any>(null);
  const [saving, setSaving] = useState(false);
  const [activeSection, setActiveSection] = useState('basic');
  const [galleryUrls, setGalleryUrls] = useState<string[]>([]);
  const [productDesc, setProductDesc] = useState('');
  const [skuType, setSkuType] = useState(
    () => (new URLSearchParams(window.location.search).get('sku') === '2' ? 2 : 1),
  );
  const [skuRows, setSkuRows] = useState<Array<Record<string, any>>>(() =>
    new URLSearchParams(window.location.search).get('sku') === '2'
      ? [{ skuValue: '', skuSn: '', skuPrice: 0, marketPrice: 0, costPrice: 0, skuStock: 0 }]
      : [],
  );
  const [shippingTpls, setShippingTpls] = useState<ShippingTpl[]>([
    { shippingTplId: 1, shippingTplName: '默认模板' },
  ]);

  useEffect(() => {
    if (!visible || isDemo()) return;
    let alive = true;
    apiGet<ShippingTpl[]>('/product/product/shippingTplList')
      .then((result) => {
        if (!alive) return;
        const list = (result || []).map((item) => ({
          shippingTplId: item.shippingTplId,
          shippingTplName: item.shippingTplName,
        }));
        if (list.length) setShippingTpls(list);
      })
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [visible]);

  useEffect(() => {
    if (!visible || !formApi) return;
    if (!productId) {
      formApi.reset();
      formApi.setValues(emptyValues, { isOverride: true });
      setGalleryUrls([]);
      setProductDesc('');
      if (!isDemo()) {
        setSkuType(1);
        setSkuRows([]);
      }
      return;
    }
    let alive = true;
    apiGet<Record<string, any>>('/product/product/detail', { id: productId })
      .then((detail) => {
        if (!alive) return;
        const images = (detail.imgList || [])
          .map((item: any) => item.picUrl)
          .filter(Boolean);
        if (!images.length && detail.picUrl) images.push(detail.picUrl);
        setGalleryUrls(images);
        setProductDesc(detail.productDesc || '');
        setSkuType(Number(detail.skuType || 1));
        setSkuRows(
          (detail.productList || []).map((sku: any) => ({
            skuId: sku.skuId,
            skuValue: sku.skuValue || '',
            skuSn: sku.skuSn || '',
            skuPrice: Number(sku.skuPrice || 0),
            marketPrice: Number(sku.marketPrice || 0),
            costPrice: Number(sku.costPrice || 0),
            skuStock: Number(sku.skuStock || 0),
          })),
        );
        formApi.setValues(
          {
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
          },
          { isOverride: true },
        );
      })
      .catch((error) => Toast.error({ content: error.message }));
    return () => {
      alive = false;
    };
  }, [visible, productId, formApi]);

  const scrollTo = (key: string) => {
    setActiveSection(key);
    document.getElementById(`product-section-${key}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  useEffect(() => {
    if (!visible) return;
    const section = new URLSearchParams(window.location.search).get('section');
    if (!section) return;
    const timer = window.setTimeout(() => scrollTo(section), 300);
    return () => window.clearTimeout(timer);
  }, [visible]);

  const submit = async () => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持保存，请登录后操作' });
      return;
    }
    if (!formApi) return;
    let values: Record<string, any>;
    try {
      values = await formApi.validate();
    } catch {
      return;
    }
    if (skuType === 2) {
      if (!skuRows.length) {
        Toast.warning({ content: '请至少添加一个规格' });
        return;
      }
      if (skuRows.some((row) => !String(row.skuValue || '').trim())) {
        Toast.warning({ content: '请填写完整的规格值' });
        return;
      }
      if (skuRows.some((row) => row.skuPrice === undefined || Number(row.skuPrice) < 0)) {
        Toast.warning({ content: '请填写规格价格' });
        return;
      }
    }
    setSaving(true);
    try {
      const payload = {
        productId: productId || undefined,
        productType: values.productType,
        productName: values.productName,
        productSn: values.productSn,
        productTsn: values.productTsn,
        categoryId: values.categoryId,
        brandId: values.brandId,
        productPrice: values.productPrice,
        marketPrice: values.marketPrice,
        costPrice: values.costPrice,
        productStock: values.productStock,
        productStatus: values.productStatus,
        productBrief: values.productBrief,
        picUrl: galleryUrls[0] || '',
        productVideo: values.productVideo,
        productDesc,
        remark: values.remark,
        keywords: values.keywords,
        productWeight: values.productWeight,
        saleMode: values.saleMode,
        limitNumber: values.limitNumber,
        shippingTplId: values.shippingTplId,
        freeShipping: values.freeShipping ? 1 : 0,
        isLogistics: values.isLogistics ? 1 : 0,
        isShopPickup: values.isShopPickup ? 1 : 0,
        noShipping: values.noShipping ? 1 : 0,
        isBest: values.isBest ? 1 : 0,
        isNew: values.isNew ? 1 : 0,
        isHot: values.isHot ? 1 : 0,
        virtualSales: values.virtualSales,
        seoIdentification: values.seoIdentification,
        isSupportReturn: values.isSupportReturn ? 1 : 0,
        isSupportCod: values.isSupportCod ? 1 : 0,
        imgList: galleryUrls.map((picUrl) => ({ picUrl })),
        skuType,
        productList:
          skuType === 2
            ? skuRows.map((row) => ({
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
      };
      await apiPost(productId ? '/product/product/update' : '/product/product/create', payload);
      Toast.success({ content: productId ? '商品已更新' : '商品已创建' });
      onSaved();
      onCancel();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <SideSheet
      className="product-form-sheet"
      title={productId ? '编辑商品' : '新增商品'}
      visible={visible}
      onCancel={onCancel}
      width={950}
      placement="right"
      bodyStyle={{ padding: 0, overflowY: 'auto' }}
      footer={
        <div className="modal-footer">
          <Button onClick={onCancel}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            保存
          </Button>
        </div>
      }
    >
      <div className="product-form-nav">
        {sections.map((section) => (
          <button
            type="button"
            key={section.key}
            className={activeSection === section.key ? 'is-active' : ''}
            onClick={() => scrollTo(section.key)}
          >
            {section.label}
          </button>
        ))}
      </div>

      <Form
        getFormApi={setFormApi}
        initValues={emptyValues}
        labelPosition="left"
        labelWidth={110}
        labelAlign="right"
        className="product-form"
      >
        <section id="product-section-basic" className="product-section">
          <div className="product-section-title">基本信息</div>
          <Form.Select
            field="productType"
            label="商品类型"
            disabled={Boolean(productId)}
            style={{ width: 360 }}
            optionList={[
              { label: '普通商品', value: 1 },
              { label: '虚拟商品', value: 2 },
              { label: '电子卡券', value: 3 },
              { label: '预约到店', value: 5 },
              { label: '上门服务', value: 6 },
            ]}
            extraText="商品类型发布后不可修改。"
          />
          <Form.Input
            field="productName"
            label="商品名称"
            placeholder="请输入商品名称"
            style={{ width: 420 }}
            rules={[{ required: true, message: '请输入商品名称' }]}
          />
          <Form.Select
            field="categoryId"
            label="商品类目"
            placeholder="请选择商品类目"
            filter
            style={{ width: 360 }}
            optionList={categories.map((item) => ({ label: item.name, value: item.id }))}
            rules={[{ required: true, message: '请选择商品类目' }]}
          />
          <Form.Select
            field="brandId"
            label="商品品牌"
            placeholder="请选择商品品牌"
            filter
            showClear
            style={{ width: 360 }}
            optionList={brands.map((item) => ({ label: item.name, value: item.id }))}
          />
          <Form.Input field="productSn" label="商品编码" placeholder="不填自动生成" style={{ width: 360 }} />
          <Form.Input
            field="productTsn"
            label="商品条码"
            placeholder="如 69 开头的 13 位标准码"
            style={{ width: 360 }}
            extraText="用于快速识别商品所标记的编码。"
          />
          <Form.InputNumber
            field="productWeight"
            label="商品重量"
            min={0}
            precision={3}
            style={{ width: 360 }}
            suffix="kg"
          />
          <Form.RadioGroup
            field="productStatus"
            label="是否上架"
            type="button"
            options={[
              { label: '上架', value: 1 },
              { label: '下架', value: 0 },
            ]}
            extraText="上架则允许销售，未审核商品无法手动上架。"
          />
          <div className="product-image-field">
            <label className="product-image-label">商品图片</label>
            <div className="product-image-content">
              <ImageUploader value={galleryUrls} onChange={setGalleryUrls} />
            </div>
          </div>
          <Form.Input
            field="productVideo"
            label="商品视频"
            placeholder="视频地址，可留空"
            style={{ width: 520 }}
          />
          <Form.Input
            field="keywords"
            label="搜索关键词"
            placeholder="多个关键词用空格分隔"
            style={{ width: 520 }}
            extraText="为空时按商品名称自动分词。"
          />
          <Form.TextArea
            field="productBrief"
            label="商品描述"
            placeholder="用于商品列表和搜索展示"
            style={{ width: 620 }}
            autosize={{ minRows: 2, maxRows: 4 }}
          />
        </section>

        <section id="product-section-sale" className="product-section">
          <div className="product-section-title">销售信息</div>
          <Form.RadioGroup
            field="saleMode"
            label="销售模式"
            type="button"
            options={[
              { label: '现货销售', value: 0 },
              { label: '预售模式', value: 1 },
            ]}
          />
          <div className="sku-type-row">
            <label className="product-image-label">规格类型</label>
            <RadioGroup
              value={skuType}
              onChange={(value) => {
                const next = Number(value);
                setSkuType(next);
                if (next === 2 && !skuRows.length) {
                  setSkuRows([
                    {
                      skuValue: '',
                      skuSn: '',
                      skuPrice: 0,
                      marketPrice: 0,
                      costPrice: 0,
                      skuStock: 0,
                    },
                  ]);
                }
              }}
              options={[
                { label: '单规格', value: 1 },
                { label: '多规格', value: 2 },
              ]}
            />
          </div>
          {skuType === 2 && (
            <div className="sku-editor">
              <div className="sku-editor-head">
                <span>规格值</span>
                <span>规格编码</span>
                <span>销售价</span>
                <span>市场价</span>
                <span>成本价</span>
                <span>库存</span>
                <span>操作</span>
              </div>
              {skuRows.map((row, index) => (
                <div className="sku-editor-row" key={index}>
                  <Input
                    value={row.skuValue}
                    placeholder="如：红色 / L"
                    onChange={(value) =>
                      setSkuRows((rows) =>
                        rows.map((item, i) => (i === index ? { ...item, skuValue: value } : item)),
                      )
                    }
                  />
                  <Input
                    value={row.skuSn}
                    placeholder="不填自动生成"
                    onChange={(value) =>
                      setSkuRows((rows) =>
                        rows.map((item, i) => (i === index ? { ...item, skuSn: value } : item)),
                      )
                    }
                  />
                  <InputNumber
                    value={row.skuPrice}
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                    onChange={(value) =>
                      setSkuRows((rows) =>
                        rows.map((item, i) => (i === index ? { ...item, skuPrice: value } : item)),
                      )
                    }
                  />
                  <InputNumber
                    value={row.marketPrice}
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                    onChange={(value) =>
                      setSkuRows((rows) =>
                        rows.map((item, i) => (i === index ? { ...item, marketPrice: value } : item)),
                      )
                    }
                  />
                  <InputNumber
                    value={row.costPrice}
                    min={0}
                    precision={2}
                    style={{ width: '100%' }}
                    onChange={(value) =>
                      setSkuRows((rows) =>
                        rows.map((item, i) => (i === index ? { ...item, costPrice: value } : item)),
                      )
                    }
                  />
                  <InputNumber
                    value={row.skuStock}
                    min={0}
                    precision={0}
                    style={{ width: '100%' }}
                    onChange={(value) =>
                      setSkuRows((rows) =>
                        rows.map((item, i) => (i === index ? { ...item, skuStock: value } : item)),
                      )
                    }
                  />
                  <Button
                    type="danger"
                    theme="borderless"
                    onClick={() => setSkuRows((rows) => rows.filter((_, i) => i !== index))}
                  >
                    删除
                  </Button>
                </div>
              ))}
              <Button
                icon={<IconPlus />}
                onClick={() =>
                  setSkuRows((rows) => [
                    ...rows,
                    { skuValue: '', skuSn: '', skuPrice: 0, marketPrice: 0, costPrice: 0, skuStock: 0 },
                  ])
                }
              >
                添加规格
              </Button>
            </div>
          )}
          <Row gutter={0}>
            <Col span={12}>
              <Form.InputNumber
                field="productPrice"
                label="销售价"
                min={0}
                precision={2}
                style={{ width: 260 }}
                rules={[{ required: true, message: '请输入销售价' }]}
              />
            </Col>
            <Col span={12}>
              <Form.InputNumber
                field="marketPrice"
                label="市场价"
                min={0}
                precision={2}
                style={{ width: 260 }}
              />
            </Col>
            <Col span={12}>
              <Form.InputNumber
                field="costPrice"
                label="成本价"
                min={0}
                precision={2}
                style={{ width: 260 }}
              />
            </Col>
            <Col span={12}>
              <Form.InputNumber
                field="productStock"
                label="商品库存"
                min={0}
                precision={0}
                style={{ width: 260 }}
              />
            </Col>
            <Col span={12}>
              <Form.InputNumber
                field="limitNumber"
                label="限购数量"
                min={0}
                precision={0}
                style={{ width: 260 }}
                extraText="0 表示不限购"
              />
            </Col>
            <Col span={12}>
              <Form.InputNumber
                field="virtualSales"
                label="虚拟销量"
                min={0}
                precision={0}
                style={{ width: 260 }}
                extraText="仅展示，不是真实销量"
              />
            </Col>
          </Row>
        </section>

        <section id="product-section-logistics" className="product-section">
          <div className="product-section-title">物流及配送</div>
          <Form.Select
            field="shippingTplId"
            label="运费模板"
            placeholder="请选择运费模板"
            filter
            style={{ width: 360 }}
            optionList={shippingTpls.map((item) => ({
              label: item.shippingTplName,
              value: item.shippingTplId,
            }))}
          />
          <Form.Switch field="freeShipping" label="包邮" extraText="开启后前台展示包邮。" />
          <Form.Switch field="isLogistics" label="快递发货" />
          <Form.Switch field="isShopPickup" label="到店自提" />
          <Form.Switch field="noShipping" label="无需配送" />
        </section>

        <section id="product-section-detail" className="product-section">
          <div className="product-section-title">商品详情</div>
          <div className="product-image-field">
            <label className="product-image-label">商品详情</label>
            <div className="product-image-content">
              <RichTextEditor value={productDesc} onChange={setProductDesc} height={380} />
            </div>
          </div>
          <Form.TextArea
            field="remark"
            label="备注"
            placeholder="内部备注，仅后台可见"
            style={{ width: 720 }}
            autosize={{ minRows: 3, maxRows: 6 }}
          />
        </section>

        <section id="product-section-advanced" className="product-section">
          <div className="product-section-title">高级信息</div>
          <Form.Switch field="isBest" label="精品推荐" />
          <Form.Switch field="isNew" label="新品推荐" />
          <Form.Switch field="isHot" label="热销推荐" />
          <Form.Switch field="isSupportReturn" label="支持退货" />
          <Form.Switch field="isSupportCod" label="支持货到付款" />
          <Form.Input
            field="seoIdentification"
            label="SEO 关键词"
            placeholder="用于商品详情页 SEO 关键词"
            style={{ width: 520 }}
          />
        </section>
      </Form>
    </SideSheet>
  );
}
