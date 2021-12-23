import React, {
  Component, useContext, useEffect, useState, createRef,
} from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import {
  Input, Icon, Modal,
} from 'choerodon-ui';
import { TextField, Form, Button } from 'choerodon-ui/pro';
import {
  Page, Content, stores, Choerodon,
} from '@choerodon/boot';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Watermark } from '@choerodon/components';
import { useGetWatermarkInfo } from '@choerodon/master';
import PageStore from '../stores';
import DocEditor from '../../../components/Editor';
import WorkSpaceSelect from '../../../components/WorkSpaceSelect';
import './style/import.less';

const Footer = ({ onOk, onCancel }) => (
  <div>
    <Button color="primary" onClick={onOk}>确定</Button>
    <Button onClick={onCancel}>取消</Button>
  </div>
);
function ImportHome() {
  const { pageStore, type: levelType, history } = useContext(PageStore);
  const {
    getImportWorkSpace: spaceData,
    getImportDoc: importDoc,
    getImportTitle: importTitle,
  } = pageStore;
  const [spaceSelectVisible, setSpaceSelectVisible] = useState(false);
  const [selectId, setSelectId] = useState(0);
  const [currentSelectId, setCurrentSelectId] = useState(0);
  const [originData, setOriginData] = useState(false);
  const [originSelectId, setOriginSelectId] = useState(false);
  const [loading, seLoading] = useState(false);
  const [title, seTitle] = useState(importTitle);
  const { enable: watermarkEnable = false, waterMarkString = '' } = useGetWatermarkInfo() || {};
  let editorRef = createRef();
  const pathModalRef = createRef();

  useEffect(() => {
    pageStore.loadWorkSpaceSelect();
  }, []);

  function handleCancelClick() {
    pageStore.setImportMode(false);
  }

  /**
   * 将文档id加入url
   * @param spaceId
   */
  function changeUrl(spaceId) {
    const { origin } = window.location;
    const { pathname, search } = history.location;
    const params = queryString.parse(search);
    params.spaceId = spaceId;
    const newParams = queryString.stringify(params);
    const newUrl = `${origin}#${pathname}?${newParams}`;
    window.history.pushState({}, 0, newUrl);
  }

  function handleCreateDoc() {
    const md = editorRef.current && editorRef.current.editorInst && editorRef.current.editorInst.getMarkdown();
    seLoading(true);
    if (title && title.trim()) {
      const vo = {
        baseId: pageStore.baseId,
        title: title.trim(),
        content: md,
        parentWorkspaceId: selectId,
      };
      pageStore.createDoc(vo).then((data) => {
        changeUrl(data.workSpace.id);
        seLoading(false);
        handleCancelClick();
      });
    } else {
      Choerodon.prompt('文档标题不能为空！');
    }
  }

  function handlePathClick() {
    setSpaceSelectVisible(true);
    setOriginData(spaceData);
    setOriginSelectId(selectId);
    // openPath({
    //   pageStore, originSelectId, originData, pathModalRef, selectId, setSelectId, setOriginSelectId, currentSelectId, setCurrentSelectId,
    // });
  }

  function handlePathCancel() {
    pageStore.setImportWorkSpace(originData);
    setSpaceSelectVisible(false);
    setSelectId(originSelectId);
    setCurrentSelectId(0);
  }

  function handleSpaceClick(data, id) {
    pageStore.setImportWorkSpace(data);
    setCurrentSelectId(id || 0);
  }

  function handleSpaceChange(data) {
    pageStore.setImportWorkSpace(data);
  }

  function handlePathChange() {
    setSelectId(currentSelectId || 0);
    setSpaceSelectVisible(false);
  }

  function getPath() {
    if (selectId) {
      const data = spaceData.items[selectId];
      const parentIds = data.route && data.route.split('.');
      let path = '';
      if (parentIds.length > 3) {
        const firstTitle = spaceData.items[parentIds[0]] && spaceData.items[parentIds[0]].data.title;
        path += `/${firstTitle.length > 10 ? `${firstTitle.slice(0, 10)}...` : firstTitle}`;
        const secondTitle = spaceData.items[parentIds[1]] && spaceData.items[parentIds[1]].data.title;
        path += `/${secondTitle.length > 10 ? `${secondTitle.slice(0, 10)}...` : secondTitle}`;
        path += '/ ... ';
        const lastTitle = spaceData.items[parentIds[parentIds.length - 1]] && spaceData.items[parentIds[parentIds.length - 1]].data.title;
        path += `/${lastTitle.length > 10 ? `${lastTitle.slice(0, 10)}...` : lastTitle}`;
      } else if (parentIds.length > 1) {
        parentIds.forEach((item) => {
          const itemTitle = spaceData.items[item] && spaceData.items[item].data.title;
          if (itemTitle && itemTitle.length) {
            path += `/${itemTitle.length > 10 ? `${itemTitle.slice(0, 10)}...` : itemTitle}`;
          }
        });
      } else {
        parentIds.forEach((item) => {
          const itemTitle = spaceData.items[item] && spaceData.items[item].data.title;
          if (itemTitle && itemTitle.length) {
            path += `/${itemTitle.length > 10 ? `${itemTitle.slice(0, 30)}...` : itemTitle}`;
          }
        });
      }
      return path;
    }
    return '/';
  }

  function setEditorRef(e) {
    editorRef = e;
  }

  function handleTitleChange(value) {
    seTitle(value);
  }
  return (
    <Page
      className="c7n-docImport"
    >
      <Content>
        <Watermark enable={watermarkEnable} content={waterMarkString} style={{ height: '100%' }}>
          <Form style={{ marginLeft: -5 }}>
            <TextField
              id="importDocTitle"
              defaultValue={importTitle}
              showLengthInfo={false}
              style={{ width: 520 }}
              onChange={handleTitleChange}
              label="文档标题"
              placeholder="文档标题"
              labelLayout="float"
            />
          </Form>
          <div style={{ margin: '10px 0 20px' }}>
            {/* <div style={{ fontSize: 12, marginBottom: 3 }}>位置</div> */}
            <div
              role="none"
              onClick={handlePathClick}
              className="workSpace-select"
            >
              <div className="workSpace-select-label">位置</div>
              <div className="workSpace-select-value">{getPath()}</div>
              <Icon className="workSpace-select-icon" type="device_hub" />
            </div>
          </div>
          <DocEditor
            data={importDoc || ''}
            editorRef={setEditorRef}
            wrapperHeight="calc(100% - 172px)"
          />
          <div style={{ marginTop: 20, textAlign: 'right' }}>
            <Button
              color="primary"
              style={{ marginLeft: 10, verticalAlign: 'middle' }}
              onClick={handleCreateDoc}
              loading={loading}
            >
              <FormattedMessage id="create" />
            </Button>
            <Button
              style={{ marginLeft: 10, verticalAlign: 'middle' }}
              onClick={handleCancelClick}
            >
              <FormattedMessage id="cancel" />
            </Button>
          </div>
        </Watermark>
        {spaceSelectVisible
          ? (
            <Modal
              className="c7n-importDoc-pathModal"
              title="文档创建位置"
              visible={spaceSelectVisible}
              closable={false}
              // onOk={handlePathChange}
              // onCancel={handlePathCancel}
              footer={<Footer onOk={handlePathChange} onCancel={handlePathCancel} />}
            >
              <div style={{
                maxHeight: '300px', overflowY: 'scroll', overflowX: 'hidden',
              }}
              >
                {spaceData && spaceData.items[spaceData.rootId].children.length
                  ? (
                    <WorkSpaceSelect
                      data={spaceData}
                      selectId={currentSelectId ?? selectId}
                      onClick={handleSpaceClick}
                      onExpand={handleSpaceChange}
                      onCollapse={handleSpaceChange}
                    />
                  ) : (
                    <span>当前无父级文档可选，默认创建在根节点。</span>
                  )}
              </div>
            </Modal>
          ) : null}
      </Content>
    </Page>
  );
}

export default withRouter(injectIntl(observer(ImportHome)));
