import React from 'react';
import { observer } from 'mobx-react-lite';
import { Viewer } from '@toast-ui/react-editor';
import FileList from '../FileList';
import './TemplateViewer.less';

const TemplateViewer = ({ data }) => (
  <div className="c7n-kb-templatePreview">
    <Viewer   
      initialValue={data.pageInfo.content}
      usageStatistics={false}
      exts={[
        'table',
        'attachment',
      ]}
    />   
    {
        data.pageAttachments && data.pageAttachments.length > 0 && (
        <FileList fileList={data.pageAttachments} readOnly />
        )
    }    
  </div>
);
export default observer(TemplateViewer);
