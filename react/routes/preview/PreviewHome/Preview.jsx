import React, { useContext } from 'react';
import { Icon } from 'choerodon-ui';
import { Button } from 'choerodon-ui/pro';
import {
  Page, Header, Content, stores, axios,
} from '@choerodon/boot';
import { getFileSuffix, paramConverter } from '../../../utils';
import PreviewContext from './stores';
import './Preview.less';

const officeSuffix = ['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx'];

const Preview = () => {
  const { prefixCls, location } = useContext(PreviewContext);
  const searchArgs = paramConverter(location.search);
  const { fileService, fileName, fileUrl } = searchArgs;
  const renderPreviewContent = () => {
    if (officeSuffix.includes(getFileSuffix(fileUrl))) {
      return (<iframe title="附件预览" width="100%" height="620px" src={`https://view.officeapps.live.com/op/view.aspx?src=${fileService}${encodeURIComponent(fileUrl)}`} />);
    } else if (getFileSuffix(fileUrl) === 'pdf') {
      return (
        <object data={`${fileService}${fileUrl}`} type="application/pdf" width="100%" height="620px">
            This browser does not support PDFs. Please download the PDF to view it: 
          <a href={`${fileService}${fileUrl}`}>Download PDF</a>
        </object>
      );
    } else {
      return (
        <div className={`${prefixCls}-contnt-imageWrap`}>
          <img className={`${prefixCls}-content-image`} src={`${fileService}${fileUrl}`} alt="图片附件" />
        </div>
      );
    }
  };

  return (
    <Page className={`${prefixCls}`}>
      <Header title="附件预览">
        <Button funcType="flat">
          <span>
            <a style={{ marginRight: 6 }} href={`${fileService}${fileUrl}`}>
              <Icon type="get_app" style={{ color: '#000' }} />
              {decodeURIComponent(fileName)}
            </a>
          </span>
        </Button>
      </Header>
      <Content>
        {renderPreviewContent()}
      </Content>
    </Page>
  );
};

export default Preview;
