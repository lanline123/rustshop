import { useEffect, useRef, useState } from 'react';
import { Button, Card, Form, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost, apiUpload } from '../api';

function UploadButton({
  accept,
  onUploaded,
}: {
  accept: string;
  onUploaded: (url: string) => void;
}) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  const upload = async (file: File) => {
    setUploading(true);
    try {
      const url = await apiUpload<string>('/setting/appVersion/uploadFile', file);
      if (url) onUploaded(url);
      Toast.success({ content: '上传成功' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '上传失败' });
    } finally {
      setUploading(false);
    }
  };

  return (
    <>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        hidden
        onChange={(event) => {
          const file = event.target.files?.[0];
          if (file) void upload(file);
          event.target.value = '';
        }}
      />
      <Button size="small" loading={uploading} onClick={() => inputRef.current?.click()}>
        {uploading ? '上传中' : '上传'}
      </Button>
    </>
  );
}

export default function AppVersion() {
  const [formApi, setFormApi] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiGet<Record<string, any>>('/setting/appVersion/detail')
      .then((result) => formApi?.setValues(result || {}, { isOverride: true }))
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, [formApi]);

  const submit = async () => {
    if (!formApi) return;
    try {
      const values = await formApi.validate();
      setSaving(true);
      await apiPost('/setting/appVersion/update', values);
      Toast.success({ content: '保存成功' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card className="table-card" shadows="hover">
      <div className="app-version-header">
        <h3>APP版本</h3>
        <p>配置 Android / iOS 版本号与热更新包下载地址。</p>
      </div>
      <Form
        getFormApi={setFormApi}
        labelPosition="left"
        labelAlign="right"
        labelWidth={140}
        className="product-form app-version-form"
        initValues={{}}
      >
        <Form.Input field="androidVersion" label="Android版本号" placeholder="如 1.0.0" />
        <Form.Input
          field="androidLink"
          label="Android下载链接"
          placeholder="上传 APK / WGT 或填写下载地址"
          suffix={
            formApi ? (
              <UploadButton
                accept=".apk,.wgt"
                onUploaded={(url) => formApi.setValue('androidLink', url)}
              />
            ) : null
          }
        />
        <Form.Input field="iosVersion" label="iOS版本号" placeholder="如 1.0.0" />
        <Form.Input
          field="iosLink"
          label="iOS下载链接"
          placeholder="上传 WGT 或填写下载地址"
          suffix={
            formApi ? (
              <UploadButton
                accept=".wgt"
                onUploaded={(url) => formApi.setValue('iosLink', url)}
              />
            ) : null
          }
        />
        <div className="app-version-actions">
          <Button theme="solid" type="primary" loading={saving || loading} onClick={submit}>
            保存
          </Button>
        </div>
      </Form>
    </Card>
  );
}
