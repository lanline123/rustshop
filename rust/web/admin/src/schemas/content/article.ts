import type { TableSchema } from '@/components/schema/types';

export const articleTableSchema: TableSchema = {
  title: '文章资讯与帮助中心',
  apiList: '/content/article/list',
  apiDelete: '/content/article/del',
  primaryKey: 'articleId',
  searchFields: [
    { prop: 'title', label: '文章标题', type: 'input', placeholder: '搜索标题...' }
  ],
  columns: [
    { prop: 'articleId', label: 'ID', width: 80, align: 'center' },
    { prop: 'thumb', label: '文章封面', type: 'image', width: 90 },
    { prop: 'title', label: '文章标题', minWidth: 200 },
    { prop: 'categoryName', label: '所属分类', minWidth: 120 },
    { prop: 'clickCount', label: '浏览量', width: 100, align: 'center', sortable: true },
    {
      prop: 'isShow',
      label: '是否显示',
      type: 'switch',
      width: 100,
      switchApi: '/content/article/updateField'
    },
    { prop: 'addTime', label: '发布时间', minWidth: 160 }
  ],
  formSchema: {
    title: '文章资讯',
    apiSave: '/content/article/update',
    labelWidth: '120px',
    sections: [
      {
        fields: [
          {
            prop: 'title',
            label: '文章标题',
            type: 'input',
            rules: [{ required: true, message: '请输入文章标题', trigger: 'blur' }]
          },
          {
            prop: 'thumb',
            label: '文章封面',
            type: 'image'
          },
          {
            prop: 'description',
            label: '文章简介',
            type: 'textarea'
          },
          {
            prop: 'sortOrder',
            label: '排序权重',
            type: 'number',
            defaultValue: 50
          },
          {
            prop: 'isShow',
            label: '立即显示',
            type: 'switch',
            defaultValue: 1
          }
        ]
      }
    ]
  }
};
