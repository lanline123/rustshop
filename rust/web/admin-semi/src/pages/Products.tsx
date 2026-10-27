import { useEffect, useState } from 'react';
import {
  Button,
  Card,
  Input,
  Select,
  Space,
  Table,
  Tag,
  Toast,
  Typography,
  Avatar,
} from '@douyinfe/semi-ui';
import { IconPlus, IconRefresh, IconSearch } from '@douyinfe/semi-icons';
import { apiGet, isDemo } from '../api';
import { mockProductPage } from '../mock';
import type { PageResult, ProductRow } from '../types';

const { Text } = Typography;

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '在售', value: '1' },
  { label: '下架', value: '0' },
];

export default function Products() {
  const [rows, setRows] = useState<ProductRow[]>(isDemo() ? mockProductPage.records : []);
  const [total, setTotal] = useState(isDemo() ? mockProductPage.total : 0);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(!isDemo());
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (isDemo()) return;
    let alive = true;
    setLoading(true);
    apiGet<PageResult<ProductRow>>('/product/product/list', {
      page,
      pageSize: 10,
      keyword: keyword || undefined,
      productStatus: status || undefined,
    })
      .then((result) => {
        if (!alive) return;
        setRows(result.records || []);
        setTotal(result.total || 0);
      })
      .catch((error) => alive && Toast.error({ content: error.message }))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [page, status, reloadKey]);

  const search = () => {
    setPage(1);
    setReloadKey((key) => key + 1);
  };

  return (
    <Card className="table-card" shadows="hover">
      <div className="table-toolbar">
        <Space>
          <Input
            prefix={<IconSearch />}
            placeholder="搜索商品名称 / 编号"
            value={keyword}
            onChange={setKeyword}
            onEnterPress={search}
            style={{ width: 280 }}
          />
          <Select
            value={status}
            onChange={(value) => {
              setStatus(String(value || ''));
              setPage(1);
            }}
            optionList={statusOptions}
            style={{ width: 140 }}
          />
          <Button theme="solid" type="primary" onClick={search}>
            查询
          </Button>
        </Space>
        <Space>
          <Button icon={<IconRefresh />} onClick={() => setReloadKey((key) => key + 1)}>
            刷新
          </Button>
          <Button theme="solid" type="primary" icon={<IconPlus />}>
            新增商品
          </Button>
        </Space>
      </div>

      <Table
        loading={loading}
        dataSource={rows}
        rowKey="productId"
        pagination={{
          currentPage: page,
          pageSize: 10,
          total,
          onChange: (nextPage: number) => setPage(nextPage),
        }}
        columns={[
          {
            title: '商品',
            dataIndex: 'productName',
            render: (value: string, record: ProductRow) => (
              <div className="product-cell">
                {record.picThumb ? (
                  <Avatar shape="square" src={record.picThumb} size="small" />
                ) : (
                  <div className="product-thumb">W</div>
                )}
                <div>
                  <div className="product-name">{value}</div>
                  <Text type="tertiary" size="small">
                    {record.productSn || '-'}
                  </Text>
                </div>
              </div>
            ),
          },
          {
            title: '销售价',
            dataIndex: 'productPrice',
            width: 140,
            render: (value: string) => <span className="money">¥ {Number(value || 0).toLocaleString()}</span>,
          },
          { title: '库存', dataIndex: 'productStock', width: 90 },
          {
            title: '状态',
            dataIndex: 'productStatus',
            width: 100,
            render: (value: number) =>
              value === 1 ? <Tag color="green">在售</Tag> : <Tag color="grey">下架</Tag>,
          },
          {
            title: '审核',
            dataIndex: 'checkStatus',
            width: 100,
            render: (value: number) => {
              const map: Record<number, { color: string; text: string }> = {
                0: { color: 'orange', text: '待审核' },
                1: { color: 'green', text: '已通过' },
                2: { color: 'red', text: '未通过' },
              };
              const item = map[value] || map[0];
              return <Tag color={item.color as any}>{item.text}</Tag>;
            },
          },
          {
            title: '操作',
            width: 150,
            render: () => (
              <Space>
                <Button theme="borderless" size="small">
                  编辑
                </Button>
                <Button theme="borderless" size="small">
                  复制
                </Button>
              </Space>
            ),
          },
        ]}
      />
    </Card>
  );
}
