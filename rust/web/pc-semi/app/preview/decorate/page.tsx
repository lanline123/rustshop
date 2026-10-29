import type { Metadata } from 'next';
import DecoratePreview from '@/components/DecoratePreview';

export const metadata: Metadata = {
  title: '装修模块回归 · 万物新生',
  robots: { index: false, follow: false },
};

export default function DecoratePreviewPage() {
  return <DecoratePreview />;
}
