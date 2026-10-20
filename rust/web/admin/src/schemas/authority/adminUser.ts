import type { TableSchema } from '@/components/schema/types';

export const adminUserTableSchema: TableSchema = {
  title: '管理员与员工账号',
  apiList: '/authority/adminUser/list',
  apiDelete: '/authority/adminUser/del',
  primaryKey: 'adminId',
  searchFields: [
    {
      prop: 'username',
      label: '账号名称',
      type: 'input',
      placeholder: '搜索用户名...'
    }
  ],
  columns: [
    { prop: 'adminId', label: 'ID', width: 80, align: 'center' },
    { prop: 'avatar', label: '头像', type: 'image', width: 90 },
    { prop: 'username', label: '用户名', minWidth: 140 },
    { prop: 'roleName', label: '所属角色', minWidth: 140 },
    { prop: 'lastLoginIp', label: '最后登录 IP', minWidth: 140 },
    { prop: 'lastLoginTime', label: '最后登录时间', minWidth: 160 },
    {
      prop: 'status',
      label: '状态',
      type: 'tag',
      width: 100,
      tagMap: {
        1: { text: '正常', type: 'success' },
        0: { text: '禁用', type: 'danger' }
      }
    }
  ],
  formSchema: {
    title: '管理员账号',
    apiSave: '/authority/adminUser/update',
    labelWidth: '120px',
    sections: [
      {
        fields: [
          {
            prop: 'username',
            label: '登录用户名',
            type: 'input',
            rules: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
          },
          {
            prop: 'password',
            label: '登录密码',
            type: 'password',
            placeholder: '不修改密码请留空'
          },
          {
            prop: 'avatar',
            label: '头像',
            type: 'image'
          },
          {
            prop: 'status',
            label: '账号状态',
            type: 'radio',
            defaultValue: 1,
            options: [
              { label: '启用', value: 1 },
              { label: '禁用', value: 0 }
            ]
          }
        ]
      }
    ]
  }
};
