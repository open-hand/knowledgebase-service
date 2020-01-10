import React, { useContext, useState, createRef } from 'react';
import { observer } from 'mobx-react-lite';
import { Choerodon } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Icon } from 'choerodon-ui';
import { TextArea } from 'choerodon-ui/pro';
import PageStore from '../../../../stores';
import Editor from '../../../../../../components/Editor';
import PromptInput from '../../../../../../components/PromptInput';
import FileUpload from '../../file-upload';

function EditTemplate(props) {
  const { searchText, fullScreen, onCancel, onEdit } = props;
  const { pageStore, type: levelType } = useContext(PageStore);
  const { getDoc: { pageInfo, userSettingVO, workSpace }, getFileList: fileList } = pageStore;
  const initialEditType = userSettingVO ? userSettingVO.editMode : undefined;
  const [title, setTitle] = useState(pageInfo.title);
  const [description, setDescription] = useState(pageStore.getDoc.description);
  const [loading, setLoading] = useState(false);
  const [visible, setVisivble] = useState(false);
  let editorRef = createRef();
  const [removeList, setRemoveList] = useState([]);
  function handleFileListChange(e) {
    const newFileList = e.fileList.filter(file => file.id);
    if (e.file.status === 'removed' && e.file.id) {
      setRemoveList([...removeList, e.file.id]);
    } else if (e.file.status === 'removed') {
      Choerodon.prompt('无法删除正在上传的附件！');
      newFileList.unshift(e.file);
    }
    pageStore.setFileList(newFileList);
  }

  function handleBeforeUpload(file) {
    const config = {
      pageId: pageInfo.id,
      versionId: pageInfo.versionId,
      uid: file.uid,
    };
    const formData = new FormData();
    formData.append('file', file);
    Choerodon.prompt('附件上传中...');
    pageStore.uploadFileForTemplate(formData, config);
    return false;
  }

  function handleCancelClick() {
    const docData = pageStore.getDoc;
    const { pageInfo: { id }, hasDraft } = docData;
    if (hasDraft) {
      pageStore.deleteDraftDoc(id);
    }
    pageStore.setMode('view');
    onCancel();
  }

  function editDoc(doc) {
    pageStore.editTemplate(workSpace.id, doc, searchText).then(() => {
      setLoading(false);
      onEdit();
    }).catch(() => {
      setLoading(false);
    });
  }

  function handleSaveClick() {
    if (title.trim().length === 0) {
      Choerodon.prompt('模板名称不能为空');
      return;
    }
    setLoading(true);
    const md = editorRef.current && editorRef.current.editorInst && editorRef.current.editorInst.getMarkdown();
    const editMode = editorRef.current && editorRef.current.editorInst && editorRef.current.editorInst.currentMode;
    const doc = {
      title: title && title.trim(),
      description,
      content: md || '',
      minorEdit: false,
      objectVersionNumber: pageInfo.objectVersionNumber,
    };
    // 修改默认编辑模式
    if (initialEditType !== editMode) {
      const mode = {
        editMode,
        type: 'edit_mode',
      };
      if (userSettingVO) {
        mode.id = userSettingVO.id;
        mode.objectVersionNumber = userSettingVO.objectVersionNumber;
      }
      pageStore.editDefaultMode(mode);
    }
    if (removeList.length) {
      pageStore.batchDeleteFile(removeList).then(() => {
        editDoc(doc);
      }).catch(() => {
        editDoc(doc);
      });
    } else {
      editDoc(doc);
    }
  }

  function handleAutoSave() {
    const md = editorRef.current && editorRef.current.editorInst && editorRef.current.editorInst.getMarkdown();
    const doc = {
      content: md || '',
    };
    pageStore.autoSaveDoc(pageInfo.id, doc);
  }

  function setEditorRef(e) {
    editorRef = e;
  }

  function handleTitleChange(value) {
    setTitle(value);
  }

  function handleClick() {
    setVisivble(!visible);
  }

  return (
    <span style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <div style={{ padding: 10, maxWidth: 704, width: 'calc(100% - 130px)' }}>
        <PromptInput
          labelLayout="float"
          label="模板名称"
          maxLength={44}       
          style={{ width: '100%' }}
          defaultValue={title}
          onChange={handleTitleChange}
        />
      </div>
      <div style={{ padding: 10 }}>
        <TextArea
          labelLayout="float"
          label="模板简介"   
          value={description}
          style={{ maxWidth: 684, width: 'calc(100% - 150px)', lineHeight: '18px' }}
          onChange={(value) => { setDescription(value); }}
          resize="vertical" 
          rows={1}
        />
      </div>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflowY: 'scroll' }}>
        {fullScreen
          ? null
          : (
            <div className="doc-attachment" style={{ margin: '0 0.1rem 0.1rem' }}>
              <div>
                <Icon
                  className="doc-attachment-expend"
                  onClick={handleClick}
                  type={visible ? 'expand_less' : 'expand_more'}
                />
                {`附件 (${fileList.length})`}
              </div>
              {visible
                ? (
                  <FileUpload
                    fileList={fileList.map(file => (file.id ? ({ ...file, uid: file.id }) : file))}
                    beforeUpload={handleBeforeUpload}
                    onChange={handleFileListChange}
                  />
                )
                : null}
            </div>
          )}       
        {/* <div style={{ flex: 1 }}> */}
        <Editor
          wrapperHeight={fullScreen ? '100%' : false}
          data={pageInfo.content}
          initialEditType={initialEditType}
          editorRef={setEditorRef}
          onSave={handleAutoSave}
        />  
        {/* </div>             */}
      </div>
      <div style={{ padding: '10px 0' }}>
        <Button
          funcType="raised"
          type="primary"
          style={{ marginLeft: 10, verticalAlign: 'middle' }}
          onClick={handleSaveClick}
          loading={loading}
        >
          <FormattedMessage id="save" />
        </Button>
        <Button
          funcType="raised"
          style={{ marginLeft: 10, verticalAlign: 'middle' }}
          onClick={handleCancelClick}
        >
          <FormattedMessage id="cancel" />
        </Button>
      </div>
    </span>
  );
}

export default injectIntl(observer(EditTemplate));
