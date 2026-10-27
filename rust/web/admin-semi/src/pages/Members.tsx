import { Avatar, Tag, Typography } from '@douyinfe/semi-ui';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';
import type { MemberRow } from '../types';

const { Text } = Typography;

const columns: ColumnConfig<MemberRow>[] = [
  {
    title: '会员',
    dataIndex: 'username',
    render: (value: string, record) => (
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
  {
    title: '会员等级',
    dataIndex: 'rankName',
    render: (value: string) => <Tag color="blue">{value || '普通会员'}</Tag>,
  },
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
];

export default function Members() {
  return (
    <ListPage<MemberRow>
      api="/user/user/list"
      rowKey="userId"
      pageSize={10}
      filters={[
        {
          field: 'keyword',
          label: '搜索会员',
          type: 'input',
          placeholder: '搜索用户名 / 手机号 / 昵称',
          width: 300,
        },
      ]}
      columns={columns}
      drawer={{
        title: '会员',
        addText: '新增会员',
        width: 560,
        createApi: '/user/user/create',
        updateApi: '/user/user/update',
        deleteApi: '/user/user/del',
        idField: 'userId',
        sections: [
          {
            key: 'basic',
            title: '基本信息',
            fields: [
              { field: 'username', label: '用户名', required: true, placeholder: '请输入用户名' },
              { field: 'mobile', label: '手机号', placeholder: '请输入手机号' },
              { field: 'email', label: '邮箱', placeholder: '请输入邮箱' },
              { field: 'password', label: '密码', placeholder: '新增时必填，编辑留空不修改' },
              {
                field: 'status',
                label: '启用状态',
                type: 'switch',
                initValue: true,
              },
              {
                field: 'adminNote',
                label: '管理员备注',
                type: 'textarea',
                width: 520,
              },
            ],
          },
        ],
        toPayload: (values) => ({
          ...values,
          status: values.status ? 1 : 0,
        }),
      }}
    />
  );
}
