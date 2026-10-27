import { useEffect, useState } from 'react';
import {
  Button,
  Col,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  SideSheet,
  Switch,
  Tabs,
  Toast,
} from '@douyinfe/semi-ui';
import { apiGet, apiPost, isDemo } from '../api';

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
  picUrl: '',
  galleryText: '',
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
  const [tab, setTab] = useState(
    () => new URLSearchParams(window.location.search).get('tab') || 'basic',
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
      return;
    }
    let alive = true;
    apiGet<Record<string, any>>('/product/product/detail', { id: productId })
      .then((detail) => {
        if (!alive) return;
        const galleryUrls = (detail.imgList || [])
          .map((item: any) => item.picUrl)
          .filter(Boolean);
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
            picUrl: detail.picUrl || '',
            galleryText: galleryUrls.join('\n'),
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
    setSaving(true);
    try {
      const galleryUrls = String(values.galleryText || '')
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean);
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
        picUrl: values.picUrl,
        productVideo: values.productVideo,
        productDesc: values.productDesc,
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
        skuType: 1,
        productList: [],
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
      title={productId ? '编辑商品' : '新增商品'}
      visible={visible}
      onCancel={onCancel}
      width={760}
      placement="right"
      bodyStyle={{ padding: '0 24px 16px', overflowY: 'auto' }}
      footer={
        <div className="modal-footer">
          <Button onClick={onCancel}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={submit}>
            保存
          </Button>
        </div>
      }
    >
      <Form
        getFormApi={setFormApi}
        initValues={emptyValues}
        labelPosition="top"
        style={{ width: '100%' }}
      >
        <Tabs
          type="button"
          activeKey={tab}
          onChange={setTab}
          keepDOM
          lazyRender={false}
          tabList={[
            { tab: '基本信息', itemKey: 'basic' },
            { tab: '销售信息', itemKey: 'sale' },
            { tab: '物流配送', itemKey: 'logistics' },
            { tab: '商品详情', itemKey: 'detail' },
            { tab: '高级信息', itemKey: 'advanced' },
          ]}
        >
          <Tabs.TabPane itemKey="basic">
            <Row gutter={16}>
              <Col span={16}>
                <Form.Input
                  field="productName"
                  label="商品名称"
                  placeholder="请输入商品名称"
                  rules={[{ required: true, message: '请输入商品名称' }]}
                />
              </Col>
              <Col span={8}>
                <Form.Select
                  field="productType"
                  label="商品类型"
                  disabled={Boolean(productId)}
                  optionList={[
                    { label: '普通商品', value: 1 },
                    { label: '虚拟商品', value: 2 },
                    { label: '电子卡券', value: 3 },
                    { label: '预约到店', value: 5 },
                    { label: '上门服务', value: 6 },
                  ]}
                />
              </Col>
              <Col span={12}>
                <Form.Select
                  field="categoryId"
                  label="商品类目"
                  placeholder="请选择商品类目"
                  filter
                  optionList={categories.map((item) => ({ label: item.name, value: item.id }))}
                  rules={[{ required: true, message: '请选择商品类目' }]}
                />
              </Col>
              <Col span={12}>
                <Form.Select
                  field="brandId"
                  label="商品品牌"
                  placeholder="请选择品牌"
                  filter
                  showClear
                  optionList={brands.map((item) => ({ label: item.name, value: item.id }))}
                />
              </Col>
              <Col span={12}>
                <Form.Input field="productSn" label="商品编码" placeholder="不填自动生成" />
              </Col>
              <Col span={12}>
                <Form.Input field="productTsn" label="商品条码" placeholder="如 69 开头的 13 位标准码" />
              </Col>
              <Col span={12}>
                <Form.InputNumber
                  field="productWeight"
                  label="商品重量（kg）"
                  min={0}
                  precision={3}
                  style={{ width: '100%' }}
                />
              </Col>
              <Col span={12}>
                <Form.RadioGroup
                  field="productStatus"
                  label="是否上架"
                  type="button"
                  options={[
                    { label: '上架', value: 1 },
                    { label: '下架', value: 0 },
                  ]}
                />
              </Col>
              <Col span={24}>
                <Form.Input field="picUrl" label="商品主图" placeholder="https://..." />
              </Col>
              <Col span={24}>
                <Form.TextArea
                  field="galleryText"
                  label="商品相册"
                  placeholder="每行一个图片地址，第一张作为主图"
                  autosize={{ minRows: 3, maxRows: 6 }}
                  extraText="支持多图，按行分隔；保存后会同步到商品相册。"
                />
              </Col>
              <Col span={24}>
                <Form.Input field="productVideo" label="商品视频" placeholder="视频地址，可留空" />
              </Col>
              <Col span={24}>
                <Form.Input
                  field="keywords"
                  label="搜索关键词"
                  placeholder="多个关键词用空格分隔，为空时按商品名称分词"
                />
              </Col>
              <Col span={24}>
                <Form.TextArea
                  field="productBrief"
                  label="商品描述"
                  placeholder="用于商品列表和搜索展示"
                  autosize={{ minRows: 2, maxRows: 4 }}
                />
              </Col>
            </Row>
          </Tabs.TabPane>

          <Tabs.TabPane itemKey="sale">
            <Row gutter={16}>
              <Col span={8}>
                <Form.InputNumber
                  field="productPrice"
                  label="销售价"
                  min={0}
                  precision={2}
                  style={{ width: '100%' }}
                  rules={[{ required: true, message: '请输入销售价' }]}
                />
              </Col>
              <Col span={8}>
                <Form.InputNumber
                  field="marketPrice"
                  label="市场价"
                  min={0}
                  precision={2}
                  style={{ width: '100%' }}
                />
              </Col>
              <Col span={8}>
                <Form.InputNumber
                  field="costPrice"
                  label="成本价"
                  min={0}
                  precision={2}
                  style={{ width: '100%' }}
                />
              </Col>
              <Col span={8}>
                <Form.InputNumber
                  field="productStock"
                  label="库存"
                  min={0}
                  precision={0}
                  style={{ width: '100%' }}
                />
              </Col>
              <Col span={8}>
                <Form.InputNumber
                  field="limitNumber"
                  label="限购数量（0 不限购）"
                  min={0}
                  precision={0}
                  style={{ width: '100%' }}
                />
              </Col>
              <Col span={8}>
                <Form.InputNumber
                  field="virtualSales"
                  label="虚拟销量"
                  min={0}
                  precision={0}
                  style={{ width: '100%' }}
                />
              </Col>
              <Col span={24}>
                <Form.RadioGroup
                  field="saleMode"
                  label="销售模式"
                  type="button"
                  options={[
                    { label: '现货销售', value: 0 },
                    { label: '预售模式', value: 1 },
                  ]}
                  extraText="预售模式需在商品详情中配置预售时间，当前先保存销售模式。"
                />
              </Col>
            </Row>
          </Tabs.TabPane>

          <Tabs.TabPane itemKey="logistics">
            <Row gutter={16}>
              <Col span={24}>
                <Form.Select
                  field="shippingTplId"
                  label="运费模板"
                  placeholder="请选择运费模板"
                  filter
                  optionList={shippingTpls.map((item) => ({
                    label: item.shippingTplName,
                    value: item.shippingTplId,
                  }))}
                />
              </Col>
              <Col span={8}>
                <Form.Switch field="freeShipping" label="包邮" />
              </Col>
              <Col span={8}>
                <Form.Switch field="isLogistics" label="快递发货" />
              </Col>
              <Col span={8}>
                <Form.Switch field="isShopPickup" label="到店自提" />
              </Col>
              <Col span={8}>
                <Form.Switch field="noShipping" label="无需配送" />
              </Col>
            </Row>
          </Tabs.TabPane>

          <Tabs.TabPane itemKey="detail">
            <Form.TextArea
              field="productDesc"
              label="商品详情"
              placeholder="支持 HTML 或纯文本"
              autosize={{ minRows: 10, maxRows: 18 }}
              extraText="商品详情会展示在 PC / H5 商品详情页。"
            />
            <Form.TextArea
              field="remark"
              label="备注"
              placeholder="内部备注，仅后台可见"
              autosize={{ minRows: 3, maxRows: 6 }}
            />
          </Tabs.TabPane>

          <Tabs.TabPane itemKey="advanced">
            <Row gutter={16}>
              <Col span={8}>
                <Form.Switch field="isBest" label="精品推荐" />
              </Col>
              <Col span={8}>
                <Form.Switch field="isNew" label="新品推荐" />
              </Col>
              <Col span={8}>
                <Form.Switch field="isHot" label="热销推荐" />
              </Col>
              <Col span={8}>
                <Form.Switch field="isSupportReturn" label="支持退货" />
              </Col>
              <Col span={8}>
                <Form.Switch field="isSupportCod" label="支持货到付款" />
              </Col>
              <Col span={24}>
                <Form.Input
                  field="seoIdentification"
                  label="SEO 关键词"
                  placeholder="用于商品详情页 SEO 关键词"
                />
              </Col>
            </Row>
          </Tabs.TabPane>
        </Tabs>
      </Form>
    </SideSheet>
  );
}
