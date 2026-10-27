import { useEffect, useState } from 'react';
import {
  Avatar,
  Button,
  Card,
  Input,
  Space,
  Table,
  Tag,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { IconRefresh, IconSearch } from '@douyinfe/semi-icons';
import { apiGet, isDemo } from '../api';
import { mockMemberPage } from '../mock';
import type { MemberRow, PageResult } from '../types';

const { Text } = Typography;

export default function Members() {
  const [rows, setRows] = useState<MemberRow[]>(isDemo() ? mockMemberPage.records : []);
  const [total, setTotal] = useState(isDemo() ? mockMemberPage.total : 0);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [loading, setLoading] = useState(!isDemo());
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (isDemo()) return;
    let alive = true;
    setLoading(true);
    apiGet<PageResult<MemberRow>>('/user/user/list', {
      page,
      size: 10,
      keyword: keyword || undefined,
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
  }, [page, reloadKey]);

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
            placeholder="搜索用户名 / 手机号 / 昵称"
            value={keyword}
            onChange={setKeyword}
            onEnterPress={search}
            style={{ width: 300 }}
          />
          <Button theme="solid" type="primary" onClick={search}>
            查询
          </Button>
        </Space>
        <Button icon={<IconRefresh />} onClick={() => setReloadKey((key) => key + 1)}>
          刷新
        </Button>
      </div>

      <Table
        loading={loading}
        dataSource={rows}
        rowKey="userId"
        pagination={{
          currentPage: page,
          pageSize: 10,
          total,
          onChange: (nextPage: number) => setPage(nextPage),
        }}
        columns={[
          {
            title: '会员',
            dataIndex: 'username',
            render: (value: string, record: MemberRow) => (
              <div className="member-cell">
                {record.avatar ? (
                  <Avatar src={record.avatar} size="small" />
                ) : (
                  <Avatar color="blue" size="small">
                    {(value || 'U').slice(0, 1).toUpperCase()}
                  </Avatar>
                )}
                <div>
                  <div>{record.nickname || value}</div>
                  <Text type="tertiary" size="small">
                    ID {record.userId}
                  </Text>
                </div>
              </div>
            ),
          },
          { title: '手机号', dataIndex: 'mobile', render: (value: string) => value || '-' },
          { title: '会员等级', dataIndex: 'rankName', render: (value: string) => <Tag color="blue">{value || '普通会员'}</Tag> },
          {
            title: '余额',
            dataIndex: 'balance',
            width: 120,
            render: (value: string) => <span className="money">¥ {Number(value || 0).toLocaleString()}</span>,
          },
          { title: '积分', dataIndex: 'points', width: 90 },
          { title: '订单数', dataIndex: 'orderCount', width: 90 },
          {
            title: '累计消费',
            dataIndex: 'orderAmount',
            width: 140,
            render: (value: string) => `¥ ${Number(value || 0).toLocaleString()}`,
          },
          { title: '注册时间', dataIndex: 'regTime', width: 170 },
          {
            title: '操作',
            width: 100,
            render: () => (
              <Button theme="borderless" size="small">
                查看
              </Button>
            ),
          },
        ]}
      />
    </Card>
  );
}
