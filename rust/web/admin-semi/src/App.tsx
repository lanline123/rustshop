import { useState } from 'react';
import { clearToken, enterDemo, getToken, isDemo } from './api';
import AdminLayout, { type PageKey } from './layout/AdminLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Orders from './pages/Orders';
import Members from './pages/Members';
import Content from './pages/Content';
import Brands from './pages/Brands';
import ProductGroups from './pages/ProductGroups';
import Placeholder from './pages/Placeholder';
import { pageTitles } from './config/adminMenu';

const knownPages = new Set(['dashboard', 'products', 'brandManage', 'productGroupManage', 'orders', 'members', 'content']);

if (new URLSearchParams(window.location.search).get('demo') === '1') {
  enterDemo();
}

function initialPage(): PageKey {
  const value = new URLSearchParams(window.location.search).get('page');
  return value && pageTitles[value] ? value : 'dashboard';
}

export default function App() {
  const [authed, setAuthed] = useState(() => Boolean(getToken() || isDemo()));
  const [page, setPage] = useState<PageKey>(initialPage);

  const changePage = (next: PageKey) => {
    setPage(next);
    const url = new URL(window.location.href);
    url.searchParams.set('page', next);
    window.history.replaceState(null, '', url);
  };

  if (!authed) {
    return <Login onSuccess={() => setAuthed(true)} />;
  }

  return (
    <AdminLayout
      page={page}
      onPageChange={changePage}
      onLogout={() => {
        clearToken();
        setAuthed(false);
      }}
    >
      {page === 'dashboard' && <Dashboard onNavigate={changePage} />}
      {page === 'products' && <Products />}
      {page === 'brandManage' && <Brands />}
      {page === 'productGroupManage' && <ProductGroups />}
      {page === 'orders' && <Orders />}
      {page === 'members' && <Members />}
      {page === 'content' && <Content />}
      {!knownPages.has(page) && <Placeholder page={page} />}
    </AdminLayout>
  );
}
