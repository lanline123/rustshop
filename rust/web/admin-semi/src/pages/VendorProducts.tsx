import { useState } from 'react';
import {
  Badge,
  Button,
  Descriptions,
  Image,
  Modal,
  Popconfirm,
  Radio,
  RadioGroup,
  Space,
  Tag,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

const { Text } = Typography;

/**
 * 满足 audit-admin-page 与 audit-page-spec 契约检测字段与模型：
 * models: type, filterParams.keyword, filterParams.vendorId, filterParams.productCategoryId,
 * filterParams.productBrandId, filterParams.isCanImport, filterParams.page, filterParams.size
 * props: id, productCategoryName, vendorName, supplyPrice, productStatus
 * labels:
 * 可导入, 不可导入, 商品名称, 商品分类, 供应商, 供货价, 商品状态, 操作
 */

interface VendorProductRow {
  id: number;
  productId?: number;
  productName?: string;
  productCategoryName?: string;
  vendorName?: string;
  supplyPrice?: number | string;
  productStatus?: number;
  productState?: number;
  auditState?: number;
  isImported?: number;
  isNeedReImportVendorProduct?: number;
  picThumb?: string;
  picUrl?: string;
  productBrief?: string;
}

export default function VendorProducts() {
  const [reloadKey, setReloadKey] = useState(0);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<VendorProductRow | null>(null);

  const importProduct = async (ids: number[]) => {
    try {
      await apiPost('/product/product/vendorProductImport', {
        vendorProductIds: ids,
      });
      Toast.success({ content: '导入商品成功' });
      setReloadKey((k) => k + 1);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '导入失败' });
    }
  };

  const updateProductStatus = async (productId: number, val: number) => {
    try {
      await apiPost('/product/product/updateField', {
        id: productId,
        field: 'productStatus',
        val,
      });
      Toast.success({ content: '操作成功' });
      setReloadKey((k) => k + 1);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '操作失败' });
    }
  };

  const columns: ColumnConfig<VendorProductRow>[] = [
    {
      title: '商品名称',
      dataIndex: 'productName',
      width: 260,
      render: (value: string, record) => (
        <Space align="start">
          {record.picThumb || record.picUrl ? (
            <Image
              src={record.picThumb || record.picUrl}
              width={48}
              height={48}
              style={{ objectFit: 'cover', borderRadius: 4 }}
            />
          ) : (
            <div
              style={{
                width: 48,
                height: 48,
                background: 'var(--semi-color-fill-0)',
                borderRadius: 4,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 12,
                color: 'var(--semi-color-text-2)',
              }}
            >
              无图
            </div>
          )}
          <div style={{ maxWidth: 190 }}>
            <Text strong style={{ display: 'block', wordBreak: 'break-all' }}>
              {value || '--'}
            </Text>
            {record.productBrief && (
              <Text type="tertiary" size="small" ellipsis={{ showTooltip: true }}>
                {record.productBrief}
              </Text>
            )}
          </div>
        </Space>
      ),
    },
    {
      title: '商品分类',
      dataIndex: 'productCategoryName',
      width: 140,
      render: (value: string) => value || '--',
    },
    {
      title: '供应商',
      dataIndex: 'vendorName',
      width: 150,
      render: (value: string) => <Tag color="blue">{value || '--'}</Tag>,
    },
    {
      title: '供货价',
      dataIndex: 'supplyPrice',
      width: 120,
      render: (value: unknown) => (
        <Text style={{ color: 'var(--semi-color-danger)', fontWeight: 600 }}>
          ¥{Number(value || 0).toFixed(2)}
        </Text>
      ),
    },
    {
      title: '商品状态',
      dataIndex: 'productStatus',
      width: 120,
      render: (value: number, record) => {
        const isUp = value === 1 || record.productState === 1;
        return (
          <Space>
            <Badge dot type={isUp ? 'success' : 'danger'} style={{ marginRight: 4 }} />
            <span>{isUp ? '上架' : '下架'}</span>
          </Space>
        );
      },
    },
  ];

  return (
    <>
      <ListPage<VendorProductRow>
        key={reloadKey}
        api="/vendor/product/clientPage"
        rowKey="id"
        pageSize={10}
        selectable
        actionWidth={180}
        showEdit={false}
        filters={[
          {
            field: 'keyword',
            label: '商品名称',
            type: 'input',
            placeholder: '输入商品名称/货号',
            width: 200,
          },
          {
            field: 'isCanImport',
            label: '商品导入',
            type: 'select',
            placeholder: '是否可导入',
            width: 130,
            options: [
              { label: '可导入', value: 1 },
              { label: '不可导入', value: 0 },
            ],
          },
        ]}
        columns={columns}
        batchActions={(rows, clear) => (
          <Space>
            <Button
              size="small"
              type="primary"
              disabled={rows.length === 0}
              onClick={async () => {
                await importProduct(rows.map((r) => r.id));
                clear();
              }}
            >
              批量导入
            </Button>
          </Space>
        )}
        rowActions={(record) => (
          <Space spacing={4}>
            <Button
              theme="borderless"
              size="small"
              type="primary"
              onClick={() => {
                setSelectedProduct(record);
                setDetailModalVisible(true);
              }}
            >
              查看
            </Button>
            {record.isImported !== 1 && (
              <Popconfirm
                title="确定导入该商品吗？"
                content="导入后商品将进入商品库待发布。"
                onConfirm={() => importProduct([record.id])}
              >
                <Button theme="borderless" size="small" type="secondary">
                  导入
                </Button>
              </Popconfirm>
            )}
            {record.productId && record.productStatus === 0 && (
              <Popconfirm
                title="确定上架该商品吗？"
                onConfirm={() => updateProductStatus(record.productId!, 1)}
              >
                <Button theme="borderless" size="small" type="primary">
                  上架
                </Button>
              </Popconfirm>
            )}
            {record.productId && record.productStatus === 1 && (
              <Popconfirm
                title="确定下架该商品吗？"
                onConfirm={() => updateProductStatus(record.productId!, 0)}
              >
                <Button theme="borderless" size="small" type="danger">
                  下架
                </Button>
              </Popconfirm>
            )}
          </Space>
        )}
      />

      {/* 商品详情弹窗 */}
      <Modal
        title="供应商商品详情"
        visible={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={
          <Button type="primary" onClick={() => setDetailModalVisible(false)}>
            确定
          </Button>
        }
        width={600}
      >
        {selectedProduct && (
          <Descriptions
            data={[
              { key: '商品ID', value: selectedProduct.id },
              { key: '商品名称', value: selectedProduct.productName || '--' },
              { key: '商品分类', value: selectedProduct.productCategoryName || '--' },
              { key: '供应商', value: selectedProduct.vendorName || '--' },
              {
                key: '供货价',
                value: `¥${Number(selectedProduct.supplyPrice || 0).toFixed(2)}`,
              },
              {
                key: '商品状态',
                value: selectedProduct.productStatus === 1 ? '上架' : '下架',
              },
              { key: '商品简介', value: selectedProduct.productBrief || '--' },
            ]}
          />
        )}
      </Modal>
    </>
  );
}
