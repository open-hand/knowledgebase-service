import React, { Component, useContext, useEffect, useState, createRef } from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import { Input, Icon, Modal, Button } from 'choerodon-ui';
import {
  Page, Content, stores,
} from '@choerodon/master';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import PageStore from '../stores';
import DocEditor from '../../../components/Editor';
import WorkSpaceSelect from '../../../components/WorkSpaceSelect';
import './style/import.less';

function ImportHome() {
  const { pageStore, type: levelType, history } = useContext(PageStore);
  const {
    getImportWorkSpace: spaceData,
    getImportDoc: importDoc,
    getImportTitle: importTitle,
  } = pageStore;
  const [spaceSelectVisible, setSpaceSelectVisible] = useState(false);
  const [selectId, setSelectId] = useState(0);
  const [currentSelectId, setCurrentSelectId] = useState(false);
  const [originData, setOriginData] = useState(false);
  const [originSelectId, setOriginSelectId] = useState(false);
  const [loading, seLoading] = useState(false);
  const [title, seTitle] = useState(importTitle);
  let editorRef = createRef();

  useEffect(() => {
    const type = levelType === 'project' ? 'pro' : 'org';
    pageStore.loadWorkSpaceSelect(type);
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
  }

  function handlePathCancel() {
    pageStore.setImportWorkSpace(originData);
    setSpaceSelectVisible(false);
    setSelectId(originSelectId);
    setCurrentSelectId(false);
  }

  function handleSpaceClick(data, id) {
    pageStore.setImportWorkSpace(data);
    setCurrentSelectId(id);
  }

  function handleSpaceChange(data) {
    pageStore.setImportWorkSpace(data);
  }

  function handlePathChange() {
    setSelectId(currentSelectId);
    setCurrentSelectId(false);
    setSpaceSelectVisible(false);
  }

  function getPath() {
    if (selectId) {
      const data = spaceData.items[selectId];
      const parentIds = data.route && data.route.split('.');
      let path = '';
      parentIds.forEach((item) => {
        path += `/${spaceData.items[Number(item)] && spaceData.items[Number(item)].data.title}`;
      });
      return path;
    } else {
      return '/';
    }
  }

  function setEditorRef(e) {
    editorRef = e;
  }

  function handleTitleChange(e) {
    seTitle(e.target.value);
  }

  return (
    <Page
      className="c7n-docImport"
    >
      <Content>
        <div>
          <Input
            id="importDocTitle"
            defaultValue={importTitle}
            showLengthInfo={false}
            style={{ width: 520 }}
            onChange={handleTitleChange}
          />
        </div>
        <div style={{ margin: '10px 0 20px' }}>
          <div style={{ fontSize: 12 }}>位置</div>
          <div
            onClick={handlePathClick}
            style={{
              borderBottom: '1px solid #000',
              width: 520,
              padding: 5,
              color: '#3F51B5',
              cursor: 'pointer',
            }}
          >
            {getPath()}
            <Icon type="device_hub" style={{ float: 'right' }} />
          </div>
        </div>
        <DocEditor
          data={importDoc || ''}
          editorRef={setEditorRef}
        />
        <div style={{ marginTop: 20 }}>
          <Button
            funcType="raised"
            type="primary"
            style={{ marginLeft: 10, verticalAlign: 'middle' }}
            onClick={handleCreateDoc}
            loading={loading}
          >
            <FormattedMessage id="create" />
          </Button>
          <Button
            funcType="raised"
            style={{ marginLeft: 10, verticalAlign: 'middle' }}
            onClick={handleCancelClick}
          >
            <FormattedMessage id="cancel" />
          </Button>
        </div>
        {spaceSelectVisible
          ? (
            <Modal
              title="文档创建位置"
              visible={spaceSelectVisible}
              closable={false}
              onOk={handlePathChange}
              onCancel={handlePathCancel}
            >
              <div style={{ padding: 10, maxHeight: '300px', overflowY: 'scroll' }}>
                <WorkSpaceSelect
                  data={spaceData}
                  selectId={currentSelectId || selectId}
                  onClick={handleSpaceClick}
                  onExpand={handleSpaceChange}
                  onCollapse={handleSpaceChange}
                />
              </div>
            </Modal>
          ) : null
        }
      </Content>
    </Page>
  );
}

export default withRouter(injectIntl(observer(ImportHome)));
