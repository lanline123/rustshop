import { useState } from 'react';
import { Avatar, Button, Modal, Space, Tag, Toast, Typography } from '@douyinfe/semi-ui';
import { apiPost } from '../api';
import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';
import ImageUploader from '../components/ImageUploader';

const { Text } = Typography;

interface AdminUserRow {
  adminId: number;
  username: string;
  avatar?: string;
  roleId?: number;
  roleName?: string;
  email?: string;
  mobile?: string;
  addTime?: string;
}

const columns: ColumnConfig<AdminUserRow>[] = [
  {
    title: '管理员名称',
    dataIndex: 'username',
    width: 220,
    render: (value: string, record) => (
      <Space>
        <Avatar src={record.avatar} size="small">
          {(value || 'A').slice(0, 1)}
        </Avatar>
        <span>{value}</span>
      </Space>
    ),
  },
  {
    title: '权限组',
    dataIndex: 'roleName',
    width: 180,
    render: (value: string, record) =>
      record.roleId === 0 ? (
        <Tag color="blue">自定义权限</Tag>
      ) : (
        <Tag color="blue">{value || '-'}</Tag>
      ),
  },
  { title: '邮箱', dataIndex: 'email', width: 220, render: (value: string) => value || '-' },
  { title: '电话', dataIndex: 'mobile', width: 160, render: (value: string) => value || '-' },
  { title: '添加时间', dataIndex: 'addTime', width: 180, render: (value: string) => value || '-' },
];

export default function AdminUsers() {
  const [reloadKey, setReloadKey] = useState(0);

  const batchDelete = (ids: number[], clear: () => void) => {
    Modal.confirm({
      title: '批量删除管理员',
      content: `确定删除选中的 ${ids.length} 个管理员？`,
      onOk: async () => {
        try {
          await apiPost('/authority/adminUser/batch', { ids, type: 'del' });
          Toast.success({ content: '批量删除成功' });
          clear();
          setReloadKey((key) => key + 1);
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '批量删除失败' });
        }
      },
    });
  };

  return (
    <ListPage<AdminUserRow>
      key={reloadKey}
      api="/authority/adminUser/list"
      rowKey="adminId"
      pageSize={10}
      selectable
      actionWidth={150}
      filters={[
        {
          field: 'keyword',
          label: '管理员',
          type: 'input',
          placeholder: '输入管理员名称',
          width: 260,
        },
      ]}
      columns={columns}
      rowActions={(record) => (
        <Button
          theme="borderless"
          size="small"
          onClick={() => {
            const url = new URL(window.location.href);
            url.searchParams.set('page', 'adminLogManage');
            url.searchParams.set('keyword', record.username);
            window.location.href = url.toString();
          }}
        >
          日志
        </Button>
      )}
      batchActions={(rows, clear) => (
        <Space>
          <Button size="small" onClick={() => batchDelete(rows.map((row) => row.adminId), clear)}>
            批量删除
          </Button>
        </Space>
      )}
      drawer={{
        title: '管理员',
        addText: '添加管理员',
        width: 700,
        labelWidth: 'auto',
        inputWidth: 420,
        createApi: '/authority/adminUser/create',
        updateApi: '/authority/adminUser/update',
        detailApi: '/authority/adminUser/detail',
        deleteApi: '/authority/adminUser/del',
        idField: 'adminId',
        sections: [
          {
            key: 'basic',
            title: '管理员信息',
            fields: [
              {
                field: 'username',
                label: '管理员名称',
                required: true,
                placeholder: '请输入管理员名称',
              },
              {
                field: 'mobile',
                label: '手机号',
                required: true,
                placeholder: '请输入手机号',
              },
              { field: 'email', label: '邮箱', placeholder: '请输入邮箱' },
              {
                field: 'avatar',
                label: '头像',
                type: 'custom',
                render: ({ formApi, values }) => (
                  <ImageUploader
                    value={values.avatar ? [values.avatar] : []}
                    onChange={(urls) => formApi?.setValue('avatar', urls[0] || '')}
                    max={1}
                  />
                ),
              },
              {
                field: 'password',
                label: '设置密码',
                placeholder: '编辑时留空不修改',
                visibleWhen: (mode) => mode === 'create',
                requiredWhen: (mode) => mode === 'create',
              },
              {
                field: 'pwdConfirm',
                label: '确认密码',
                placeholder: '请再次输入密码',
                visibleWhen: (mode) => mode === 'create',
                requiredWhen: (mode) => mode === 'create',
              },
              {
                field: 'roleId',
                label: '管理员角色',
                type: 'select',
                placeholder: '请选择角色',
                optionsApi: '/authority/adminRole/list',
                optionsParams: { page: 1, pageSize: 100 },
                optionLabel: 'roleName',
                optionValue: 'roleId',
              },
            ],
          },
        ],
        toPayload: (values) => ({
          ...values,
          adminId: values.adminId ? Number(values.adminId) : undefined,
          roleId: values.roleId ? Number(values.roleId) : undefined,
        }),
      }}
    />
  );
}
