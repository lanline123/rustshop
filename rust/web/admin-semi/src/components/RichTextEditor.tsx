import { useEffect, useState } from 'react';
import { Toast } from '@douyinfe/semi-ui';
import '@wangeditor/editor/dist/css/style.css';
import { Editor, Toolbar } from '@wangeditor/editor-for-react';
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor';
import { apiUpload } from '../api';

interface UploadResult {
  picUrl: string;
}

interface RichTextEditorProps {
  value: string;
  onChange: (html: string) => void;
  height?: number;
  placeholder?: string;
}

export default function RichTextEditor({
  value,
  onChange,
  height = 360,
  placeholder = '请输入商品详情...',
}: RichTextEditorProps) {
  const [editor, setEditor] = useState<IDomEditor | null>(null);

  const toolbarConfig: Partial<IToolbarConfig> = {
    excludeKeys: ['fullScreen', 'group-video'],
  };
  const editorConfig: Partial<IEditorConfig> = {
    placeholder,
    MENU_CONF: {
      uploadImage: {
        async customUpload(file: File, insertFn: (url: string, alt: string, href: string) => void) {
          try {
            const result = await apiUpload<UploadResult>(
              '/setting/galleryPic/uploadImg?galleryId=0',
              file,
            );
            insertFn(result.picUrl, file.name, result.picUrl);
          } catch (error) {
            Toast.error({ content: error instanceof Error ? error.message : '图片上传失败' });
          }
        },
      },
    },
  };

  useEffect(() => {
    return () => {
      if (editor) {
        editor.destroy();
        setEditor(null);
      }
    };
  }, [editor]);

  useEffect(() => {
    if (editor && value !== editor.getHtml()) {
      editor.setHtml(value || '');
    }
  }, [value, editor]);

  return (
    <div className="rich-editor">
      <Toolbar
        editor={editor}
        defaultConfig={toolbarConfig}
        mode="default"
        style={{ borderBottom: '1px solid #e6eaf0' }}
      />
      <Editor
        defaultConfig={editorConfig}
        value={value}
        onCreated={setEditor}
        onChange={(instance) => onChange(instance.getHtml())}
        mode="default"
        style={{ height, overflowY: 'hidden' }}
      />
    </div>
  );
}
