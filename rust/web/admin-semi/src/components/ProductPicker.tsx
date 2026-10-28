import { useEffect, useState } from 'react';
import { Button, Input, SideSheet, Space, Table, Toast } from '@douyinfe/semi-ui';
import { apiGet } from '../api';

interface ProductItem {
  productId: number;
  productName: string;
  productSn?: string;
  productPrice?: number | string;
  picThumb?: string;
  productStock?: number;
}

interface ProductPickerProps {
  value?: number[];
  onChange: (ids: number[]) => void;
  max?: number;
  disabled?: boolean;
}

export default function ProductPicker({
  value = [],
  onChange,
  max = 10,
  disabled,
}: ProductPickerProps) {
  const [visible, setVisible] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [rows, setRows] = useState<ProductItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [selectedKeys, setSelectedKeys] = useState<number[]>(value);
  const [selectedRows, setSelectedRows] = useState<ProductItem[]>([]);

  const loadSelected = async (ids: number[]) => {
    if (!ids.length) {
      setSelectedRows([]);
      return;
    }
    try {
      const result = await apiGet<{ records: ProductItem[] }>('/product/product/list', {
        ids: ids.join(','),
        page: 1,
        pageSize: 100,
      });
      const records = result.records || [];
      setSelectedRows(ids.map((id) => records.find((item) => item.productId === id)).filter(Boolean) as ProductItem[]);
    } catch {
      setSelectedRows([]);
    }
  };

  useEffect(() => {
    void loadSelected(value);
  }, [JSON.stringify(value)]);

  const loadProducts = async (nextPage = page, nextKeyword = keyword) => {
    setLoading(true);
    try {
      const result = await apiGet<{ records: ProductItem[]; total: number }>(
        '/product/product/list',
        { keyword: nextKeyword, page: nextPage, pageSize: 10 },
      );
      setRows(result.records || []);
      setTotal(result.total || 0);
      setPage(nextPage);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '商品加载失败' });
    } finally {
      setLoading(false);
    }
  };

  const open = () => {
    setSelectedKeys(value);
    setKeyword('');
    setPage(1);
    void loadProducts(1, '');
    setVisible(true);
  };

  const confirm = () => {
    onChange(selectedKeys);
    void loadSelected(selectedKeys);
    setVisible(false);
  };

  const remove = (id: number) => {
    const next = value.filter((item) => item !== id);
    onChange(next);
    setSelectedRows((current) => current.filter((item) => item.productId !== id));
  };

  return (
    <div className="product-picker">
      <Space>
        <Button
          theme="solid"
          type="primary"
          disabled={disabled || (max > 0 && value.length >= max)}
          onClick={open}
        >
          选择商品
        </Button>
        {value.length > 0 && (
          <>
            <span className="product-picker-count">已选择 {value.length} 个商品</span>
            <Button
              theme="borderless"
              onClick={() => {
                onChange([]);
                setSelectedRows([]);
              }}
            >
              清空
            </Button>
          </>
        )}
      </Space>

      {selectedRows.length > 0 && (
        <Table
          dataSource={selectedRows}
          rowKey="productId"
          pagination={false}
          columns={[
            { title: '商品编号', dataIndex: 'productSn', width: 120 },
            { title: '商品名称', dataIndex: 'productName' },
            {
              title: '商品价格',
              dataIndex: 'productPrice',
              width: 110,
              render: (item: any) => `¥ ${item ?? 0}`,
            },
            { title: '商品库存', dataIndex: 'productStock', width: 100 },
            {
              title: '操作',
              width: 80,
              render: (_: unknown, record: ProductItem) => (
                <Button theme="borderless" type="danger" size="small" onClick={() => remove(record.productId)}>
                  删除
                </Button>
              ),
            },
          ]}
        />
      )}

      <SideSheet
        title="选择商品"
        visible={visible}
        onCancel={() => setVisible(false)}
        width={900}
        placement="right"
        bodyStyle={{ padding: '16px 20px', overflowY: 'auto' }}
        footer={
          <Space>
            <span className="product-picker-count">
              已选择 {selectedKeys.length} / {max} 个商品
            </span>
            <Button onClick={() => setVisible(false)}>取消</Button>
            <Button theme="solid" type="primary" onClick={confirm}>
              确定
            </Button>
          </Space>
        }
      >
        <Space style={{ marginBottom: 12 }}>
          <Input
            value={keyword}
            placeholder="搜索商品名称 / 编号"
            onChange={setKeyword}
            onEnterPress={() => loadProducts(1)}
            style={{ width: 300 }}
          />
          <Button onClick={() => loadProducts(1)}>搜索</Button>
        </Space>
        <Table
          loading={loading}
          dataSource={rows}
          rowKey="productId"
          rowSelection={{
            selectedRowKeys: selectedKeys,
            onChange: (keys) => {
              const next = (keys || []).map(Number);
              if (max > 0 && next.length > max) {
                Toast.warning({ content: `最多选择 ${max} 个商品` });
                return;
              }
              setSelectedKeys(next);
            },
          }}
          pagination={{
            currentPage: page,
            pageSize: 10,
            total,
            onChange: (next) => loadProducts(next),
          }}
          columns={[
            { title: '商品编号', dataIndex: 'productSn', width: 120 },
            { title: '商品名称', dataIndex: 'productName' },
            {
              title: '商品价格',
              dataIndex: 'productPrice',
              width: 110,
              render: (item: any) => `¥ ${item ?? 0}`,
            },
            { title: '商品库存', dataIndex: 'productStock', width: 100 },
          ]}
        />
      </SideSheet>
    </div>
  );
}
