import { useEffect, useState } from 'react';
import {
  Button,
  Card,
  Input,
  Select,
  Space,
  Table,
  Tabs,
  Tag,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { IconPlus, IconRefresh, IconSearch } from '@douyinfe/semi-icons';
import { apiGet, isDemo } from '../api';
import { mockArticlePage } from '../mock';
import type { ArticleCategoryRow, ArticleRow, PageResult } from '../types';

const { Text } = Typography;

export default function Content() {
  const [tab, setTab] = useState('articles');
  const [rows, setRows] = useState<ArticleRow[]>(isDemo() ? mockArticlePage.records : []);
  const [categories, setCategories] = useState<ArticleCategoryRow[]>([]);
  const [total, setTotal] = useState(isDemo() ? mockArticlePage.total : 0);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [loading, setLoading] = useState(!isDemo());
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (isDemo()) return;
    let alive = true;
    apiGet<ArticleCategoryRow[]>('/content/articleCategory/list')
      .then((result) => alive && setCategories(result || []))
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, []);

  useEffect(() => {
    if (isDemo()) return;
    let alive = true;
    setLoading(true);
    apiGet<PageResult<ArticleRow>>('/content/article/list', {
      page,
      pageSize: 10,
      keyword: keyword || undefined,
      articleCategoryId: categoryId || undefined,
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

  const categoryName = (id: number) =>
    categories.find((item) => item.articleCategoryId === id)?.articleCategoryName || `分类 ${id}`;

  return (
    <Card className="table-card" shadows="hover">
      <Tabs
        type="line"
        activeKey={tab}
        onChange={setTab}
        tabList={[
          { tab: '文章管理', itemKey: 'articles' },
          { tab: '文章分类', itemKey: 'categories' },
        ]}
      />

      {tab === 'articles' ? (
        <>
          <div className="table-toolbar" style={{ marginTop: 16 }}>
            <Space>
              <Input
                prefix={<IconSearch />}
                placeholder="搜索文章标题 / 关键词"
                value={keyword}
                onChange={setKeyword}
                onEnterPress={search}
                style={{ width: 260 }}
              />
              <Select
                value={categoryId}
                onChange={(value) => {
                  setCategoryId(String(value || ''));
                  setPage(1);
                }}
                optionList={[
                  { label: '全部分类', value: '' },
                  ...categories.map((item) => ({
                    label: item.articleCategoryName,
                    value: String(item.articleCategoryId),
                  })),
                ]}
                style={{ width: 160 }}
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
                新增文章
              </Button>
            </Space>
          </div>

          <Table
            loading={loading}
            dataSource={rows}
            rowKey="articleId"
            pagination={{
              currentPage: page,
              pageSize: 10,
              total,
              onChange: (nextPage: number) => setPage(nextPage),
            }}
            columns={[
              { title: 'ID', dataIndex: 'articleId', width: 70 },
              { title: '标题', dataIndex: 'articleTitle' },
              {
                title: '分类',
                dataIndex: 'articleCategoryId',
                width: 130,
                render: (value: number) => <Tag color="blue">{categoryName(value)}</Tag>,
              },
              {
                title: '描述',
                dataIndex: 'description',
                render: (value: string) => (
                  <Text type="tertiary" ellipsis={{ showTooltip: true }} style={{ maxWidth: 280 }}>
                    {value || '-'}
                  </Text>
                ),
              },
              { title: '点击', dataIndex: 'clickCount', width: 80 },
              {
                title: '显示',
                dataIndex: 'isShow',
                width: 90,
                render: (value: number) =>
                  value === 1 ? <Tag color="green">显示</Tag> : <Tag color="grey">隐藏</Tag>,
              },
              { title: '操作', width: 130, render: () => <Button theme="borderless" size="small">编辑</Button> },
            ]}
          />
        </>
      ) : (
        <div style={{ marginTop: 16 }}>
          <div className="table-toolbar">
            <Text type="tertiary">共 {categories.length} 个文章分类</Text>
            <Button theme="solid" type="primary" icon={<IconPlus />}>
              新增分类
            </Button>
          </div>
          <Table
            dataSource={categories}
            rowKey="articleCategoryId"
            pagination={false}
            columns={[
              { title: 'ID', dataIndex: 'articleCategoryId', width: 80 },
              { title: '分类名称', dataIndex: 'articleCategoryName' },
              { title: '标识', dataIndex: 'categorySn', render: (value: string) => value || '-' },
              { title: '父级 ID', dataIndex: 'parentId', width: 100 },
              { title: '操作', width: 120, render: () => <Button theme="borderless" size="small">编辑</Button> },
            ]}
          />
        </div>
      )}
    </Card>
  );
}
