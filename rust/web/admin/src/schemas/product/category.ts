import type { TableSchema } from '@/components/schema/types';

export const categoryTableSchema: TableSchema = {
  title: '商品分类管理',
  apiList: '/product/category/list',
  apiDelete: '/product/category/del',
  primaryKey: 'categoryId',
  searchFields: [
    {
      prop: 'categoryName',
      label: '分类名称',
      type: 'input',
      placeholder: '搜索分类名称...'
    },
    {
      prop: 'isShow',
      label: '显示状态',
      type: 'select',
      options: [
        { label: '全部', value: '' },
        { label: '显示', value: 1 },
        { label: '隐藏', value: 0 }
      ]
    }
  ],
  columns: [
    { prop: 'categoryId', label: 'ID', width: 80, align: 'center' },
    { prop: 'categoryPic', label: '分类图标', type: 'image', width: 90 },
    { prop: 'categoryName', label: '分类名称', minWidth: 160 },
    { prop: 'sortOrder', label: '排序权重', width: 100, align: 'center', sortable: true },
    {
      prop: 'isShow',
      label: '是否显示',
      type: 'switch',
      width: 100,
      switchApi: '/product/category/updateField'
    }
  ],
  formSchema: {
    title: '商品分类',
    apiSave: '/product/category/update',
    labelWidth: '120px',
    sections: [
      {
        fields: [
          {
            prop: 'categoryName',
            label: '分类名称',
            type: 'input',
            rules: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
          },
          {
            prop: 'categoryPic',
            label: '分类图标',
            type: 'image',
            extra: '建议尺寸 120x120 像素'
          },
          {
            prop: 'sortOrder',
            label: '排序权重',
            type: 'number',
            defaultValue: 50
          },
          {
            prop: 'isShow',
            label: '是否显示',
            type: 'switch',
            defaultValue: 1
          }
        ]
      }
    ]
  }
};
