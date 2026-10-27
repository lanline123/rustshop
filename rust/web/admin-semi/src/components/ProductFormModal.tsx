import { useEffect, useState } from 'react';
import {
  Button,
  Col,
  Form,
  Input,
  InputNumber,
  SideSheet,
  Row,
  Select,
  Toast,
} from '@douyinfe/semi-ui';
import { apiGet, apiPost, isDemo } from '../api';
import type { ProductRow } from '../types';

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

const emptyValues = {
  productName: '',
  productSn: '',
  categoryId: undefined,
  brandId: undefined,
  productPrice: 0,
  marketPrice: 0,
  productStock: 0,
  productStatus: 1,
  productBrief: '',
  picUrl: '',
  productDesc: '',
  keywords: '',
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

  useEffect(() => {
    if (!visible || !formApi) return;
    if (!productId) {
      formApi.reset();
      formApi.setValues(emptyValues, { isOverride: true });
      return;
    }
    let alive = true;
    apiGet<ProductRow & Record<string, unknown>>('/product/product/detail', { id: productId })
      .then((detail) => {
        if (!alive) return;
        formApi.setValues(
          {
            productName: detail.productName || '',
            productSn: detail.productSn || '',
            categoryId: detail.categoryId || undefined,
            brandId: detail.brandId || undefined,
            productPrice: Number(detail.productPrice || 0),
            marketPrice: Number(detail.marketPrice || 0),
            productStock: Number(detail.productStock || 0),
            productStatus: Number(detail.productStatus ?? 1),
            productBrief: detail.productBrief || '',
            picUrl: detail.picUrl || '',
            productDesc: detail.productDesc || '',
            keywords: detail.keywords || '',
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
      const payload = {
        productId: productId || undefined,
        productName: values.productName,
        productSn: values.productSn,
        categoryId: values.categoryId,
        brandId: values.brandId,
        productPrice: values.productPrice,
        marketPrice: values.marketPrice,
        productStock: values.productStock,
        productStatus: values.productStatus,
        productBrief: values.productBrief,
        picUrl: values.picUrl,
        productDesc: values.productDesc,
        keywords: values.keywords,
        productType: 1,
        skuType: 1,
        productList: [],
        isLogistics: 1,
        freeShipping: 1,
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
      width={680}
      placement="right"
      bodyStyle={{ padding: '8px 24px 16px', overflowY: 'auto' }}
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
        <Form.Section text="基础信息">
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
              <Form.Input field="productSn" label="商品编号" placeholder="不填自动生成" />
            </Col>
            <Col span={12}>
              <Form.Select
                field="categoryId"
                label="商品分类"
                placeholder="请选择商品分类"
                filter
                optionList={categories.map((item) => ({ label: item.name, value: item.id }))}
              />
            </Col>
            <Col span={12}>
              <Form.Select
                field="brandId"
                label="品牌"
                placeholder="请选择品牌"
                filter
                showClear
                optionList={brands.map((item) => ({ label: item.name, value: item.id }))}
              />
            </Col>
          </Row>
        </Form.Section>

        <Form.Section text="价格与库存">
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
                field="productStock"
                label="库存"
                min={0}
                precision={0}
                style={{ width: '100%' }}
              />
            </Col>
            <Col span={12}>
              <Form.RadioGroup
                field="productStatus"
                label="上架状态"
                type="button"
                options={[
                  { label: '上架', value: 1 },
                  { label: '下架', value: 0 },
                ]}
              />
            </Col>
          </Row>
        </Form.Section>

        <Form.Section text="展示信息">
          <Form.Input field="picUrl" label="主图地址" placeholder="https://..." />
          <Form.Input field="keywords" label="关键词" placeholder="多个关键词用逗号分隔" />
          <Form.TextArea
            field="productBrief"
            label="商品简介"
            placeholder="用于商品列表和搜索展示"
            autosize={{ minRows: 2, maxRows: 4 }}
          />
          <Form.TextArea
            field="productDesc"
            label="商品描述"
            placeholder="支持 HTML 或纯文本"
            autosize={{ minRows: 4, maxRows: 8 }}
          />
        </Form.Section>
      </Form>
    </SideSheet>
  );
}
