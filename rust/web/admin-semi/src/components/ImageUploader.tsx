import { useRef, useState } from 'react';
import { Toast } from '@douyinfe/semi-ui';
import { IconClose, IconPlus } from '@douyinfe/semi-icons';
import { apiUpload, isDemo } from '../api';

interface ImageUploaderProps {
  value: string[];
  onChange: (next: string[]) => void;
  max?: number;
}

interface UploadResult {
  picUrl: string;
  picThumb?: string;
}

export default function ImageUploader({ value, onChange, max = 9 }: ImageUploaderProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  const pick = () => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持上传图片' });
      return;
    }
    inputRef.current?.click();
  };

  const upload = async (files: FileList | null) => {
    if (!files || !files.length) return;
    const remaining = max - value.length;
    if (remaining <= 0) {
      Toast.warning({ content: `最多上传 ${max} 张图片` });
      return;
    }
    setUploading(true);
    try {
      const next = [...value];
      for (const file of Array.from(files).slice(0, remaining)) {
        const result = await apiUpload<UploadResult>('/setting/galleryPic/uploadImg?galleryId=0', file);
        if (result?.picUrl) next.push(result.picUrl);
      }
      onChange(next);
      Toast.success({ content: '图片上传成功' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '图片上传失败' });
    } finally {
      setUploading(false);
      if (inputRef.current) inputRef.current.value = '';
    }
  };

  const remove = (index: number) => {
    onChange(value.filter((_, i) => i !== index));
  };

  const setMain = (index: number) => {
    if (index === 0) return;
    const next = [...value];
    const [item] = next.splice(index, 1);
    next.unshift(item);
    onChange(next);
  };

  return (
    <div className="image-uploader">
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        multiple
        hidden
        onChange={(event) => upload(event.target.files)}
      />
      <div className="image-uploader-list">
        {value.map((url, index) => (
          <div className="image-uploader-item" key={`${url}-${index}`}>
            <img src={url} alt={`商品图片 ${index + 1}`} />
            {index === 0 && <span className="image-main-badge">主图</span>}
            <div className="image-uploader-actions">
              {index !== 0 && (
                <button type="button" onClick={() => setMain(index)}>
                  设为主图
                </button>
              )}
              <button type="button" className="is-danger" onClick={() => remove(index)}>
                <IconClose size="small" />
              </button>
            </div>
          </div>
        ))}
        {value.length < max && (
          <button type="button" className="image-uploader-add" onClick={pick} disabled={uploading}>
            <IconPlus />
            <span>{uploading ? '上传中...' : '上传图片'}</span>
          </button>
        )}
      </div>
      <div className="image-uploader-tip">
        支持 JPG / PNG / WebP，最多 {max} 张；第一张作为商品主图，可通过“设为主图”调整顺序。
      </div>
    </div>
  );
}
