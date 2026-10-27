import { useState } from 'react';
import { clearToken, enterDemo, getToken, isDemo } from './api';
import AdminLayout, { type PageKey } from './layout/AdminLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Orders from './pages/Orders';
import Members from './pages/Members';
import Content from './pages/Content';
import Placeholder from './pages/Placeholder';

const placeholderPages: PageKey[] = ['aftersales', 'marketing', 'finance', 'settings'];

if (new URLSearchParams(window.location.search).get('demo') === '1') {
  enterDemo();
}

export default function App() {
  const [authed, setAuthed] = useState(() => Boolean(getToken() || isDemo()));
  const [page, setPage] = useState<PageKey>('dashboard');

  if (!authed) {
    return <Login onSuccess={() => setAuthed(true)} />;
  }

  return (
    <AdminLayout
      page={page}
      onPageChange={setPage}
      onLogout={() => {
        clearToken();
        setAuthed(false);
      }}
    >
      {page === 'dashboard' && <Dashboard onNavigate={setPage} />}
      {page === 'products' && <Products />}
      {page === 'orders' && <Orders />}
      {page === 'members' && <Members />}
      {page === 'content' && <Content />}
      {placeholderPages.includes(page) && <Placeholder page={page} />}
    </AdminLayout>
  );
}
