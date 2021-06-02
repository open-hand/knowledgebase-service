import React, {
  useContext, useState, createRef,
} from 'react';
import { observer } from 'mobx-react-lite';
import { Choerodon } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Icon } from 'choerodon-ui';
import { TextField, Form } from 'choerodon-ui/pro';
import PageStore from '../../../stores';
import Editor from '../../../../../components/Editor';
import FileUpload from '../file-upload';

function EditMode(props) {
  const { searchText, fullScreen } = props;
  const { pageStore, type: levelType } = useContext(PageStore);
  const { getDoc: { pageInfo, userSettingVO, workSpace }, getFileList: fileList } = pageStore;
  const initialEditType = userSettingVO ? userSettingVO.editMode : undefined;
  const [title, setTitle] = useState(pageInfo.title);
  const [loading, setLoading] = useState(false);
  const [visible, setVisivble] = useState(false);
  let editorRef = createRef();
  const [removeList, setRemoveList] = useState([]);
  function handleFileListChange(e) {
    const newFileList = e.fileList.filter((file) => file.id);
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
    pageStore.uploadFile(formData, config);
    return false;
  }

  function handleCancelClick() {
    const docData = pageStore.getDoc;
    const { pageInfo: { id }, hasDraft } = docData;
    if (hasDraft) {
      pageStore.deleteDraftDoc(id);
    }
    pageStore.setMode('view');
  }

  function editDoc(doc) {
    pageStore.editDoc(workSpace.id, doc, searchText).then(() => {
      setLoading(false);
      handleCancelClick();
    }).catch(() => {
      setLoading(false);
    });
  }

  function handleSaveClick() {
    setLoading(true);
    const md = editorRef.current && editorRef.current.editorInst && editorRef.current.editorInst.getMarkdown();
    const editMode = editorRef.current && editorRef.current.editorInst && editorRef.current.editorInst.currentMode;
    const doc = {
      title: title && title.trim(),
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

  function handleTitleChange(e) {
    setTitle(e.target.value);
  }

  function handleClick() {
    setVisivble(!visible);
  }

  return (
    <span style={{ paddingTop: 10 }}>
      <Form style={{ marginLeft: 15, marginTop: 12 }}>
        <TextField
          size="large"
          showLengthInfo={false}
          maxLength={40}
          style={{ maxWidth: 684, width: 'calc(100% - 150px)' }}
          defaultValue={title}
          onChange={handleTitleChange}
          label="文档标题"
          valueChangeAction="input"
          labelLayout="float"
        />
      </Form>
      <div style={{
        height: 'calc(100% - 114px)', display: 'flex', flexDirection: 'column', overflowY: 'scroll', marginTop: -10,
      }}
      >
        {fullScreen
          ? null
          : (
            <div className="doc-attachment" style={{ margin: '0 20px 10px' }}>
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
                    fileList={fileList.map((file) => (file.id ? ({ ...file, uid: file.id }) : file))}
                    beforeUpload={handleBeforeUpload}
                    onChange={handleFileListChange}
                  />
                )
                : null}
            </div>
          )}
        <Editor
          wrapperHeight={fullScreen ? '100%' : false}
          data={pageInfo.content}
          initialEditType={initialEditType}
          editorRef={setEditorRef}
          onSave={handleAutoSave}
        />
      </div>
      <div style={{ padding: '10px 0', textAlign: 'right' }}>
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

export default injectIntl(observer(EditMode));
