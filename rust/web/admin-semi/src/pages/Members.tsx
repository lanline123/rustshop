import { useState } from 'react';
import { Avatar, Switch, Tag, Toast, Typography } from '@douyinfe/semi-ui';
import { apiPost, isDemo } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';
import type { MemberRow } from '../types';

const { Text } = Typography;

function MemberStatusSwitch({ record }: { record: MemberRow }) {
  const [checked, setChecked] = useState(record.status === 1);

  const toggle = async (next: boolean) => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持操作' });
      return;
    }
    try {
      await apiPost('/user/user/updateField', {
        id: record.userId,
        field: 'status',
        val: next ? 1 : 0,
      });
      setChecked(next);
      Toast.success({ content: next ? '已启用' : '已禁用' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '操作失败' });
    }
  };

  return <Switch checked={checked} onChange={toggle} size="small" />;
}

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
    width: 110,
    render: (value: string) => <span className="money">¥ {Number(value || 0).toLocaleString()}</span>,
  },
  { title: '积分', dataIndex: 'points', width: 80 },
  { title: '订单数', dataIndex: 'orderCount', width: 80 },
  {
    title: '累计消费',
    dataIndex: 'orderAmount',
    width: 130,
    render: (value: string) => `¥ ${Number(value || 0).toLocaleString()}`,
  },
  { title: '注册时间', dataIndex: 'regTime', width: 160 },
  {
    title: '是否启用',
    dataIndex: 'status',
    width: 90,
    render: (_: unknown, record: MemberRow) => <MemberStatusSwitch record={record} />,
  },
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
        title: '客户',
        addText: '添加客户',
        width: 560,
        labelWidth: 'auto',
        inputWidth: 420,
        createApi: '/user/user/create',
        updateApi: '/user/user/update',
        deleteApi: '/user/user/del',
        idField: 'userId',
        sections: [
          {
            key: 'basic',
            title: '客户信息',
            fields: [
              {
                field: 'username',
                label: '客户名称',
                required: true,
                placeholder: '请输入客户名称',
                visibleWhen: (mode) => mode === 'create',
              },
              { field: 'mobile', label: '手机号码', placeholder: '请输入手机号码' },
              { field: 'email', label: '邮箱地址', placeholder: '请输入邮箱地址' },
              {
                field: 'password',
                label: '新密码',
                placeholder: '新增时必填，编辑留空不修改',
                requiredWhen: (mode) => mode === 'create',
              },
              {
                field: 'pwdConfirm',
                label: '确认密码',
                placeholder: '请再次输入密码',
                requiredWhen: (mode) => mode === 'create',
              },
              {
                field: 'rankId',
                label: '客户等级',
                type: 'select',
                placeholder: '请选择客户等级',
                optionsApi: '/user/userRank/list',
                optionsParams: { size: 200 },
                optionLabel: 'rankName',
                optionValue: 'rankId',
              },
            ],
          },
        ],
      }}
    />
  );
}
