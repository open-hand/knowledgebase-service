/* eslint-disable */
import React, {
  useContext, useEffect, useState, useRef,useCallback, useMemo,
} from 'react';
import { observer } from 'mobx-react-lite';
import queryString from 'query-string';
import { TextField, Modal,notification,Spin,Icon} from 'choerodon-ui/pro';
import {
  Page, Header, Content, stores, Permission, Breadcrumb, Choerodon, axios
} from '@choerodon/boot';
import FileSaver from 'file-saver';
import { HeaderButtons, useGetWatermarkInfo, workSpaceApi } from '@choerodon/master';
import { Watermark } from '@choerodon/components';
import Loading, { LoadingProvider } from '@choerodon/agile/lib/components/Loading';
import { withRouter } from 'react-router-dom';
import { injectIntl, useIntl } from 'react-intl';
import { mutateTree } from '@atlaskit/tree';
import useFormatMessage from '@/hooks/useFormatMessage';
import {
  TREE_FOLDER,
  TREE_FILE,
  TREE_DOC,
} from './CONSTANTS';
import TreeFolder from './components/tree-folder';
import TreeFile from './components/tree-file';
import DocDetail from '../../../components/DocDetail';
import DocEditor from './components/doc-editor';
import PageStore from '../stores';
import { removeItemFromTree, addItemToTree } from '../../../components/WorkSpaceTree';
import ResizeContainer from '../../../components/ResizeDivider/ResizeContainer';
import WorkSpace from '../components/work-space';
import SearchList from '../../../components/SearchList';
import Catalog from '../../../components/Catalog';
import DocModal from './components/docModal';
import HomePage from './components/home-page';
import CreateDoc from './components/create-doc';
import CreateTemplate from './components/create-template';
import Template from './components/template';
import useFullScreen from './components/fullScreen/useFullScreen';
import './style/index.less';
import openShare from './components/docModal/ShareModal';
import openImport from './components/docModal/ImportModal';
import openMove from './components/docModal/MoveMoal';
import ShareDoc from './components/share';
import './DocHome.less';
import { uploadFile, secretMultipart } from '@/api/knowledgebaseApi'; 
import wordSvg from '@/assets/image/word.svg';
import pptSvg from '@/assets/image/ppt.svg';
import pdfSvg from '@/assets/image/pdf.svg';
import txtSvg from '@/assets/image/txt.svg';
import xlsxSvg from '@/assets/image/xlsx.svg';
import mp4Svg from '@/assets/image/mp4.svg';
import { Tooltip } from 'choerodon-ui';

const { Section, Divider } = ResizeContainer;
const { AppState } = stores;

function DocHome() {
  const {
    pageStore, history, id: proId, organizationId: orgId, type: levelType, formatMessage,location: { search },
  } = useContext(PageStore);
  const bootFormatMessage = useFormatMessage('boot');
  const uploadInput = useRef(null);
  const [loading, setLoading] = useState(false);
  const [docLoading, setDocLoading] = useState(false);
  const [searchValue, setSearchValue] = useState('');
  const [logVisible, setLogVisible] = useState(false);
  const [creating, setCreating] = useState(false);
  const [saving, setSaving] = useState(false);
  const [catalogTag, setCatalogTag] = useState(false);
  const [readOnly, setReadOnly] = useState(true);
  const [fileIsEdit, setFileIsEdit] = useState(false);
  const { section } = pageStore;
  const workSpaceRef = useRef(null);
  const [uploading, setuploading] = useState('info');

  const fileRef = useRef(null);
  const folderRef = useRef(null);
  const editNameRef = useRef('编辑');
  const prefix='c7n-kb-doc';
  const spaceCode = pageStore.getSpaceCode;
  const { enable: watermarkEnable = false, waterMarkString = '' } = useGetWatermarkInfo() || {};
  const onFullScreenChange = (fullScreen) => {
    pageStore.setFullScreen(!!fullScreen);
    if (catalogTag) {
      pageStore.setCatalogVisible(true);
      setCatalogTag(false);
    } else {
      pageStore.setCatalogVisible(false);
    }
  };
  const [isFullScreen, toggleFullScreen] = useFullScreen(() => document.body, onFullScreenChange, 'c7nagile-doc-fullScreen');
  const {
    getSpaceCode: code,
    getSearchVisible: searchVisible,
    getSelectId: selectId,
    getMode: mode,
  } = pageStore;

  function getTypeCode() {
    return levelType === 'project' ? 'pro' : 'org';
  }

  const downloadByUrl = (url, fileType, fileName = '未命名') => {
    FileSaver.saveAs(url, fileName);
    // debugger;
    // return axios.get(url, {
    //   responseType: 'blob',
    //   beforeErrorAction () {
    //     return false;
    //   },
    // }).then(res => {
    //   let mime = '';
    //   switch (fileType) {
    //     case 'xlsx':
    //       mime = 'application/vnd.ms-excel';
    //       break;
    //     case 'csv':
    //       mime = 'text/csv';
    //       break;
    //     default:
    //       break;
    //   }
    //   const blob = new Blob([res], { type: mime });
    //   fileSaver.saveAs(blob, `${fileName}.${fileType}`);
    // }).catch(() => {
    //   const aTag = document.createElement('a');
    //   aTag.href = url;
    //   aTag.download = fileName; // 此属性仅适用于同源 URL https://developer.mozilla.org/zh-CN/docs/Web/HTML/Element/a
    //   aTag.style.display = 'none';
    //   document.body.appendChild(aTag);
    //   aTag.click();
    //   // 下载完成后删除dom节点
    //   document.body.removeChild(aTag);
    // });
  };

  const fullScreen = (ele) => {
    if (ele.requestFullscreen) {
        ele.requestFullscreen();
    } else if (ele.mozRequestFullScreen) {
        ele.mozRequestFullScreen();
    } else if (ele.webkitRequestFullscreen) {
        ele.webkitRequestFullscreen();
    } else if (ele.msRequestFullscreen) {
        ele.msRequestFullscreen();
    }
  }

  const getFileEditDisplay = useMemo(() => {
    const item = pageStore.getSelectItem;
    const splitList = item?.title?.split('.');
    const suffix = splitList?.[splitList.length - 1];
    const map = ['DOC', 'DOCX', 'XLSX', 'XLS', 'XLSM', 'CSV', 'PPT', 'PPTX', 'PPS', 'PPSX'];
    const flag = map.map(i => i?.toLowerCase())?.includes(suffix?.toLowerCase());
    // const isOnlyOffice = fileRef?.current?.getIsOnlyOffice();
    if (flag) {
      return true;
    }
    return false;
  }, [pageStore.getSelectItem, fileRef?.current?.getIsOnlyOffice()]);
  const getTreeFileItems = () => {
    return ([{
      name: function() {
        const getEditDisplay = fileRef?.current?.getIsEdit();
        return getEditDisplay ? '退出编辑' : '编辑';
      }(),
      icon: 'edit-o',
      display: getFileEditDisplay,
      handler: async () => {
        const getEditDisplay = fileRef?.current?.getIsEdit();
        // editNameRef.current = getEditDisplay ? '退出编辑' : '编辑';
        // debugger;
        // setFileIsEdit(getEditDisplay ? true : false);
        if (getEditDisplay) {
          await pageStore.loadWorkSpaceAll();
          goView()
        } else {
          goEdit();
        }
      },
    }, {
      element: <ShareDoc isFile hasText store={pageStore} />,
    }, {
      name: '下载',
      icon: 'file_download_black-o',
      handler: async () => {
        const res = levelType === 'project' ? await workSpaceApi.getFileData(pageStore.getSelectItem?.id) : await workSpaceApi.getOrgFiledData(pageStore.getSelectItem?.id);
        const url = res?.url;
        const splitList = url.split('.');
        const fileType = splitList[splitList.length - 1];
        const splitList2 = url.split('@');
        const fileName = splitList2[splitList2.length - 1];
        downloadByUrl(url, fileType, fileName);
      },
    }, {
      name: '更多操作',
      groupBtnItems: [{
        name: '移动',
        handler: () => {
          handleMove(pageStore.getSelectItem);
        }
      }, {
        name: '复制',
        handler: () => {
          handleCopyClick(pageStore.getSelectItem);
        }
      }, {
        name: '删除',
        handler: () => {
          const callback = (id) => {
            const workSpace = pageStore.getWorkSpace;
            const spaceData = workSpace[code].data;
            const item = spaceData.items[id];
            pageStore.setSelectItem(item);
          }
          handleDeleteDoc(pageStore.getSelectItem, 'admin', callback);
        }
      }, {
        name: '切换WPS/OnlyOffice',
        handler: () => {
          fileRef?.current?.changeMode();
        }
      }]
    }, {
      icon: 'zoom_out_map',
      handler: () => {
        const item = document.querySelector('.c7ncd-knowledge-file');
        const wpsItem = document.querySelector('#c7ncd-center-wps');
        fullScreen(item || wpsItem);
      }
    }, {
      icon: 'refresh',
      handler: () => {
        const getEditDisplay = !fileRef?.current?.getIsEdit();
        if (getEditDisplay) {
          fileRef?.current?.initView();
        } else {
          fileRef?.current?.initEdit();
        }
      }
    }, {
      element: headerSearchTextField(),
      display: true,
    }])
  }

  /**
   * 将文档id加入url
   * @param spaceId
   */
  function changeUrl(spaceId) {
    const { origin, hash } = window.location;
    const { pathname } = history.location;
    const search = hash.split('?').length > 1 ? hash.split('?')[1] : '';
    const params = queryString.parse(search);
    params.spaceId = spaceId;
    const newParams = queryString.stringify(params);
    const newUrl = `${origin}#${pathname}?${newParams}`;
    window.history.pushState({}, 0, newUrl);
  }

  function handleCancel(spaceId) {
    setCreating(false);
    setSaving(false);
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[code].data;
    const item = spaceData.items[spaceId];
    const newTree = removeItemFromTree(spaceData, {
      ...item,
      parentId: item.parentId || item.workSpaceParentId || spaceData.rootId,
    }, true);
    pageStore.setWorkSpaceByCode(code, newTree);
  }

  function checkPermission(type) {
    if (levelType === 'organization') {
      // const orgData = HeaderStore.getOrgData;
      // const orgObj = orgData.find(v => String(v.id) === String(orgId));
      // if (!orgObj || (orgObj && !orgObj.into)) {
      // setReadOnly(true);
      // } else {
      setReadOnly(false);
      // }
    } else {
      setReadOnly(getTypeCode() !== type);
    }
  }

  /**
   * 加载文档详情
   * @param spaceId 空间id
   * @param isCreate
   * @param searchText
   */
  function loadPage(spaceId = false, isCreate = false, searchText) {
    if (!isCreate && creating) {
      handleCancel('create');
      return;
    }
    setDocLoading(true);
    pageStore.setCatalogVisible(false);
    const id = spaceId; // getDefaultSpaceId();
    if (id) {
      // 更新url中文档ID
      changeUrl(id);
      pageStore.loadDoc(id, searchText).then((res) => {
        if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
          // 访问无权限文档或已被删除的文档
          if (searchVisible || searchText) {
            pageStore.setSelectId(id);
            setDocLoading(false);
            pageStore.setDoc(false);
            setReadOnly(true);
          } else {
            pageStore.setSelectId(id);
            loadPage();
          }
        } else {
          if (logVisible) {
            if (levelType === 'project' && !res.pageInfo.projectId) {
              // 项目查看组织文章，则关闭日志
              setLogVisible(false);
            } else {
              // 否则更新日志
              pageStore.loadLog(res.pageInfo.id);
            }
          }
          checkPermission(res.pageInfo.projectId ? 'pro' : 'org');
          pageStore.setSelectId(id);
          setDocLoading(false);
          pageStore.setMode(isCreate ? 'edit' : 'view');
        }
        const workSpace = pageStore.getWorkSpace;
        const spaceData = workSpace?.[code]?.data;
        const item = spaceData?.items?.[id];
        if (item) {
          pageStore.setSelectItem(item);
        }
      }).catch((e) => {
        console.log(e);
        setReadOnly(true);
        setDocLoading(false);
      });
    } else {
      // 没选文档时，显示主页
      // pageStore.setSpaceCode(levelType === 'project' ? 'pro' : 'org');
      // pageStore.setSpaceCode(levelType === 'project' ? 'pro' : 'org');
      pageStore.setSelectId(false);
      pageStore.setDoc(false);
      pageStore.setSection('recent');
      checkPermission(getTypeCode());
      pageStore.queryRecentUpdate();
      setDocLoading(false);
    }
  }

  /**
   * 加载空间
   */
  function loadWorkSpace(spaceId) {
    let id = spaceId;
    if (!id) {
      const { hash } = window.location;
      const search = hash.split('?').length > 1 ? hash.split('?')[1] : '';
      const params = queryString.parse(search);
      id = params.spaceId;
    }
    if (id) {
      pageStore.setSelectId(id);
    }

    pageStore.loadWorkSpaceAll(id || selectId).then((res) => {
      if (res && res.failed && ['error.workspace.illegal', 'error.workspace.notFound'].indexOf(res.code) !== -1) {
        // 如果id错误或不存在
        pageStore.loadWorkSpaceAll().then(() => {
          pageStore.setSelectId(false);
          setLoading(false);
          // loadPage();
        });
      } else {
        const selectItem=Object.values(res.data.items).filter((item)=>{return item.isClick;});
        pageStore.setSelectItem(selectItem[0]);
        setLoading(false);
        loadPage(id || selectId);
      }
    }).catch((e) => {
      setLoading(false);
    });
  }
  useEffect(() => {
    // 加载数据
    // MenuStore.setCollapsed(true);
    loadWorkSpace();
    pageStore.loadOrgOrigin();
   
  }, []);

  /**
   * 移动文档至回收站
   * @param {*} spaceId
   * @param {*} role
   */
  function deleteDoc(spaceId, role, callback) {
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[code].data;
    const item = spaceData.items[spaceId];
    
    const request = role === 'admin' ? pageStore.adminDeleteDoc : pageStore.deleteDoc;
    request(spaceId).then(() => {
      // 更改
      notification['success']({
        placement: 'bottomLeft',
        key:'1',
        message: '删除成功',
        description:<div>该内容已成功删除，后续可在回收站中恢复。<a onClick={()=>{notification.close('1')}} href={`${window.location.origin}/#/knowledge/project/${search}`}>转至回收站</a></div>,
      });
      let newTree = removeItemFromTree(spaceData, {
        ...item,
        parentId: item.parentId || item.workSpaceParentId || spaceData.rootId,
      }, true);
      const newSelectId = item.parentId || item.workSpaceParentId || spaceData.rootId;
      newTree = mutateTree(newTree, newSelectId, { isClick: true });
      pageStore.setWorkSpaceByCode(code, newTree);
      pageStore.setSelectId(newSelectId);
        pageStore.setSection('recent');
        pageStore.queryRecentUpdate();
        pageStore.setDoc(false);
        pageStore.loadWorkSpaceAll();
      callback && callback(newSelectId);
    }).catch((error) => {
      console.log(error);
    });
  }
  const fileImageList = {
    docx: wordSvg, pptx: pptSvg, pdf: pdfSvg,xlsx: xlsxSvg,
  };

  const preview=(file,res)=>{
    pageStore.setSection('tree');
    pageStore.setSelectItem(res.workSpace);
    notification.close('2');
  }
  const renderNotification=(file,status,res)=>{
    const name=file.name;
    const type=file.name.split('.')[1];
    return <div className={`${prefix}-notification-content-container`}>
       <img src={fileImageList[type]} alt="" style={{ marginRight: '6px' }} />
       <div className={`${prefix}-notification-content-main`}>
         <Tooltip title={name}>
          <div className={`${prefix}-notification-content-name`} id="notification-name">{name}</div>
          <div>{(file.size/Math.pow(1024,2)).toFixed(2)+'MB'}</div>
        </Tooltip>
       </div>
       {status==='doing'&&<Spin className={`${prefix}-notification-content-spin`}/>}
       {status==='success'&&<span className={`${prefix}-notification-content-preview`} onClick={()=>preview(file,res)}>预览</span>}
      </div>
  }
  const upload = useCallback((file) => {
    const workSpace = pageStore.getWorkSpace;
    const id = pageStore.getSelectUploadId;
    const type=file.name.split('.').at(-1);
    if(!fileImageList[type]){
      Choerodon.prompt('暂不支持上传该格式的文件');
      return;
    }
    const spaceData = workSpace[levelType === 'project' ? 'pro' : 'org']?.data;
    if (!file) {
      Choerodon.prompt('请选择文件');
      return;
    }
    if (file.size > 1024 * 1024 * 100) {
      Choerodon.prompt('文件不能超过100M');
      return;
    }
    const formData = new FormData();
    formData.append('file', file);
    notification['info']({
      message: '上传中',
      key:'1',
      placement:'bottomLeft',
      description:renderNotification(file,'doing'),
      duration:null,
    });
    secretMultipart(formData, AppState.currentMenuType.type).then((res) => {
      const data = {
        fileKey: res.fileKey,
        baseId: pageStore.baseId,
        parentWorkspaceId: id,
        title: spaceData.items[spaceData?.rootId].title,
        type: 'file',
      };
      uploadFile(data, AppState.currentMenuType.type).then((response) => {
        if (res && !res.failed) {
          const selected = pageStore.getSelectItem;
          notification['success']({
            message: '上传成功',
            key:'2',
            description:renderNotification(file,'success',response),
            placement:'bottomLeft',
          });
          pageStore.loadWorkSpaceAll();
          if (selected?.type === TREE_FOLDER) {
            folderRef?.current?.refresh();
          }
        }
      }).catch((err)=>{
        
      }).finally(()=>{
        notification.close('1');
      });
    }).catch((err)=>{
      notification.close('1');
    });
  }, []);
  const beforeUpload = useCallback((e) => {
    if (e.target.files[0]) {
      upload(e.target.files[0]);
    }
  }, [upload]);
   const handleUpload = useCallback((id,e) => {
     if (e && e?.domEvent) {
      e.domEvent.stopPropagation();
     }
    pageStore.setSelectUploadId(id);
    uploadInput.current?.click();
  }, []);
  const itemUpload = useCallback((id) => {
    pageStore.setSelectUploadId(id);
    uploadInput.current?.click();
  }, []);
  function handleDeleteDoc(item, role, callback) {
    const typeList={'folder':'文件夹','document':'文档','file':'文件'}
    Modal.open({
      title: `删除${typeList[item.type]}"${item?.data?.title || item?.name}"`,
      children: `${typeList[item.type]}"${item?.data?.title || item?.name}"将被移至回收站，和工作项的关联将会移除；若已对外分享，也将不能查看；后续您可以在回收站中进行恢复。`,
      okText: '删除',
      cancelText: '取消',
      width: 520,
      onOk() {
        deleteDoc(item.id, role, callback);
      },
      onCancel() { },
    });
  }

  function handleShare(id) {
    pageStore.queryShareMsg(id).then(() => {
      openShare({ store: pageStore });
    });
  }

  const disabled = getTypeCode() === 'pro' ? ['share', 'org'].includes(spaceCode) : false;
  function handleCreateClick(item, callback) {
    pageStore.setMode('view');
    CreateDoc({
      onCreate: async ({ title, template: templateId, root}) => {
        const currentCode = pageStore.getSpaceCode;
        const workSpace = pageStore.getWorkSpace;
        const spaceData = workSpace[levelType === 'project' ? 'pro' : spaceCode]?.data;
        let newTree = spaceData;
        const getParentWorkspaceId = () => {
          if(root){
            return spaceData?.rootId;
          }
          return item?.id||spaceData?.rootId;
        };
        const vo = {
          title: title.trim(),
          content: '',
          type: 'document',
          parentWorkspaceId: getParentWorkspaceId(),
        };
        const data = templateId ? await pageStore.createWorkSpaceWithTemplate(vo, templateId) : await pageStore.createWorkSpace(vo);
        if (selectId) {
          if (currentCode !== spaceCode) {
            const newSpace = mutateTree(workSpace[currentCode].data, selectId, { isClick: false });
            pageStore.setWorkSpaceByCode(currentCode, newSpace);
          } else {
            newTree = mutateTree(spaceData, selectId, { isClick: false });
          }
        }
       
        if (item?.data?.rootId === spaceData.rootId) {
          loadWorkSpace(data.workSpace.id);
        } else {
          newTree = addItemToTree(
            newTree,
            { ...data.workSpace, createdBy: data.createdBy, isClick: true },
            'create',
          );
          pageStore.setWorkSpaceByCode(spaceCode, newTree);
          loadPage(data.workSpace.id, 'create');
        }
        setSaving(false);
        setCreating(false);
        setLoading(false);
        callback && callback();
      },
      pageStore,
    });
  }
  function handleCreateClickInTree(parent) {
    if (levelType === 'project') {
      pageStore.setSpaceCode('pro');
    }
    pageStore.setMode('view');
    // 新建时，创建项所在分组展开
    if (workSpaceRef && workSpaceRef.current) {
      const openKeys = workSpaceRef.current.openKeys || [];
      const openKey = getTypeCode();
      if (openKeys.indexOf(openKey) === -1) {
        openKeys.push(openKey);
      }
      workSpaceRef.current.handlePanelChange(openKeys);
    }
    if (saving) {
      return;
    }

    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[spaceCode].data;
    if (!creating && spaceData) {
      setCreating(true);
      // 构建虚拟空间节点
      const item = {
        data: { title: 'create' },
        hasChildren: false,
        isExpanded: false,
        id: 'create',
        parentId: (parent && parent.id) || spaceData.rootId,
      };
      const newTree = addItemToTree(spaceData, item);

      pageStore.setWorkSpaceByCode(spaceCode, newTree);
    }
  }
  /**
   * 回车/确认按钮创建空间
   * @param value
   * @param item
   */
  function handleSpaceSave(value, item) {
    setSaving(true);
    setLoading(true);
    const currentCode = pageStore.getSpaceCode;
    const workSpace = pageStore.getWorkSpace;
    const spaceData = workSpace[spaceCode].data;
    let newTree = spaceData;
    if (creating) {
      setCreating(false);
      newTree = removeItemFromTree(spaceData, {
        ...item,
        parentId: item.parentId || item.workSpaceParentId || spaceData.rootId,
      });
    }
    pageStore.setWorkSpaceByCode(spaceCode, newTree);
    if (!value || !value.trim() || saving) {
      return;
    }
    const vo = {
      title: value.trim(),
      content: '',
      parentWorkspaceId: item.parentId,
      type: 'folder',
    };
    pageStore.createWorkSpace(vo).then((data) => {
        newTree = addItemToTree(
          workSpace[currentCode].data,
          { ...data.workSpace, createdBy: data.createdBy, isClick: false },
          'create',
        );
      pageStore.setWorkSpaceByCode(spaceCode, newTree);
      setSaving(false);
      setCreating(false);
      setLoading(false);
    });
  }
  function handleTemplateCreateClick() {
    CreateTemplate({
      pageStore,
    });
  }
  function handleImportClick(item) {
    pageStore.setImportDefaultItem(item);
    openImport({ store: pageStore });
  }

  function handleLogClick() {
    const { workSpace } = pageStore.getDoc;
    if (workSpace) {
      const { id: workSpaceId } = workSpace;
      handleShare(workSpaceId);
    }
  }
  async function handleCopyClick(item) {
    openMove({ store: pageStore,flag:'copy', id: item.id?item.id:selectId,title:item.data.title, refresh: loadWorkSpace });
  }
  function handleEditClick() {
    pageStore.setCatalogVisible(false);
    pageStore.setMode('edit');
    setLogVisible(false);
  }

  const handleSearchClick = (value) => {
    if (value) {
      pageStore.querySearchList(value).then((res) => {
        pageStore.setSearchVisible(true);
        const searchList = pageStore.getSearchList;
        if (searchList && searchList.length) {
          loadPage(searchList[0].workSpaceId, false, value);
        } else {
          pageStore.setDoc(false);
        }
      });
    } else {
      pageStore.setSearchVisible(false);
    }
  };

  function handleSearchChange(value) {
    setSearchValue(value);
    handleSearchClick(value);
  }

  function handleClearSearch() {
    pageStore.setSearchVisible(false);
    setSearchValue('');
    loadWorkSpace();
  }

  function handleLoadDraft() {
    const docData = pageStore.getDoc;
    const { pageInfo: { id } } = docData;
    pageStore.loadDraftDoc(id).then(() => {
      pageStore.setCatalogVisible(false);
      pageStore.setMode('edit');
    });
  }

  function handleDeleteDraft() {
    const docData = pageStore.getDoc;
    const hasDraft = pageStore.getDraftVisible;
    const { pageInfo: { id } } = docData;
    if (hasDraft) {
      pageStore.deleteDraftDoc(id).then(() => {
        loadPage(selectId);
      });
    }
  }

  function toggleFullScreenEdit() {
    const { catalogVisible } = pageStore;
    if (catalogVisible) {
      pageStore.setCatalogVisible(false);
      setCatalogTag(true);
    }
    toggleFullScreen();
    pageStore.setFullScreen(!isFullScreen);
  }
  const handleMove = (item) => {
    openMove({ store: pageStore,flag:'move', id: item.id?item.id:selectId,title:item.data.title, refresh: loadWorkSpace });
  };

  const renderTreeSection = () => {
    const selectItem = pageStore.getSelectItem;
    const type = selectItem?.type;
    switch (type) {
      case TREE_FOLDER: {
        return (
          <TreeFolder
            data={selectItem}
            onDelete={handleDeleteDoc}
            cRef={folderRef}
            store={pageStore}
            loadPage={loadPage}
            refresh={loadWorkSpace}
          />
        );
        break;
      }
      case TREE_FILE: {
        return (
          <TreeFile
            data={selectItem}
            cRef={fileRef}
            store={pageStore}
            setFileIsEdit={setFileIsEdit}
           />
        );
        break;
      }
      case TREE_DOC: {
        return (
          <DocEditor
            readOnly={disabled || readOnly}
            loadWorkSpace={loadWorkSpace}
            searchText={searchValue}
            editTitleBefore={() => setLogVisible(false)}
            fullScreen={isFullScreen}
            exitFullScreen={toggleFullScreenEdit}
            editDoc={handleEditClick}
            handleCreateClick={handleCreateClick}
          />
        )
      }
      default: {
        return '';
        break;
      }
    }
  };

  const goEdit = () => {
    fileRef?.current?.goEdit();
  }

  const goView = () => {
    fileRef?.current?.goView();
  }

  const headerSearchTextField = () => (
    <TextField
      style={{ marginRight: 8, marginTop: disabled || readOnly ? 4 : 0 }}
      placeholder={formatMessage({ id: 'search' })}
      value={searchValue}
      valueChangeAction="input"
      wait={300}
      onChange={handleSearchChange}
    />
  )

  const getHeaders = useCallback(() => {
    const getNonTemplage = () => {
      const selected = pageStore.getSelectItem;
      if (section === 'recent'
      || (section === 'tree' && ![TREE_FOLDER, TREE_FILE].includes(selected?.type))
      ) {
        return (
          <HeaderButtons items={[{
            name: formatMessage({ id: 'create' }),
            icon: 'playlist_add',
            handler: handleCreateClick,
            disabled: disabled || readOnly,
            display: true,
          }, {
            name: bootFormatMessage({ id: 'import' }),
            icon: 'archive-o',
            handler: () => handleImportClick(pageStore.getSelectItem),
            disabled: disabled || readOnly,
            display: true,
          }, {
            name: bootFormatMessage({ id: 'modify' }),
            icon: 'edit-o',
            handler: handleEditClick,
            disabled: disabled || readOnly,
            display: section === 'tree' && selectId,
          }, {
            name: bootFormatMessage({ id: 'copy' }),
            icon: 'file_copy-o',
            handler: ()=>handleCopyClick(pageStore.getSelectItem),
            disabled: disabled || readOnly,
            display: section === 'tree' && selectId,
          }, {
            display: section === 'tree' && selectId,
            name: formatMessage({ id: 'more_actions' }),
            disabled: disabled || readOnly,
            groupBtnItems: [{
              name: bootFormatMessage({ id: 'export' }),
              icon: 'unarchive-o',
              handler: () => {
                const { pageInfo, workSpace } = pageStore.getDoc;
                if (!pageInfo || !workSpace) {
                  return;
                }
                const { id, title } = pageInfo;
                Choerodon.prompt('正在导出，请稍候...');
                pageStore.exportPdf(id, title);
              },
              disabled: disabled || readOnly,
            }, {
              name: formatMessage({ id: 'move' }),
              disabled: disabled || readOnly,
              handler: () => handleMove(pageStore.getSelectItem),
            }, {
              name: formatMessage({ id: 'operation_history' }),
              disabled: disabled || readOnly,
              handler: () => {
                const { pageInfo, workSpace } = pageStore.getDoc;
                if (!pageInfo || !workSpace) {
                  return;
                }
                setLogVisible(true);
              },
            }, {
              name: formatMessage({ id: 'version_comparison' }),
              disabled: disabled || readOnly,
              handler: () => {
                const { pageInfo, workSpace } = pageStore.getDoc;
                if (!pageInfo || !workSpace) {
                  return;
                }
                const { id: workSpaceId } = workSpace;
                const urlParams = AppState.currentMenuType;
                history.push(`/knowledge/${urlParams.type}/version/${pageStore.baseId}?type=${urlParams.type}&id=${urlParams.id}&name=${encodeURIComponent(urlParams.name)}&organizationId=${urlParams.organizationId}&orgId=${urlParams.organizationId}&spaceId=${workSpaceId}`);
              },
            }, {
              name: bootFormatMessage({ id: 'delete' }),
              disabled: disabled || readOnly,
              permissions: levelType === 'project'
                ? ['choerodon.code.project.cooperation.knowledge.ps.doc.delete']
                : ['choerodon.code.organization.knowledge.ps.doc.delete'],
              handler: () => {
                const docData = pageStore.getDoc;
                const { pageInfo, workSpace } = pageStore.getDoc;
                if (!pageInfo || !workSpace) {
                  return;
                }
                const { title } = pageInfo;
                const { id: workSpaceId } = workSpace;
                const callback = (id) => {
                  const workSpace = pageStore.getWorkSpace;
                  const spaceData = workSpace[code].data;
                  const item = spaceData.items[id];
                  pageStore.setSelectItem(item);
                }
                if (AppState.userInfo.id === docData.createdBy) {
                  handleDeleteDoc(workSpace, '', callback);
                } else {
                  handleDeleteDoc(workSpace,'admin', callback);
                }
              },
            }],
          }, {
            // name: formatMessage({ id: 'share' }),
            // icon: 'share',
            // handler: handleLogClick,
            disabled: disabled || readOnly,
            display: section === 'tree' && selectId,
            // iconOnly: true,
            element: <ShareDoc store={pageStore} disabled={disabled || readOnly} />,
          }, {
            icon: isFullScreen ? 'fullscreen_exit' : 'zoom_out_map',
            iconOnly: true,
            handler: toggleFullScreenEdit,
            display: true,
            tooltipsConfig: {
              title: isFullScreen ? '退出全屏' : '全屏',
            },
          }, {
            display: true,
            element: headerSearchTextField(),
          }]}
          />
        )
      } else if (selected?.type === TREE_FILE) {
        return (
          <HeaderButtons
            items={getTreeFileItems()}
           />
        )
      } else if (selected?.type === TREE_FOLDER) {
        return (
          <HeaderButtons
            items={[{
              name: '创建',
              icon: 'playlist_add',
              groupBtnItems: [{
                name: '创建文档',
                handler: () => {
                  handleCreateClick(pageStore.getSelectItem, folderRef?.current?.refresh);
                }
              }, {
                name: '上传本地文件',
                handler: () => {
                  handleUpload(pageStore.getSelectItem?.id);
                }
              }, {
                name: '导入为在线文档',
                handler: () => {
                  handleImportClick(pageStore.getSelectItem);
                }
              }, {
                name: '创建文件夹',
                handler: () => {
                  handleCreateClickInTree(pageStore.getSelectItem);
                }
              }]
            }, {
              element: headerSearchTextField(),
              display: true,
            }]}
           />
        )
      }
      return '';
    }

    if (!isFullScreen) {
      return (
        <Header>
          {section !== 'template'
            ? getNonTemplage() : (
              <HeaderButtons items={[{
                name: formatMessage({ id: 'create_template' }),
                handler: handleTemplateCreateClick,
                icon: 'playlist_add',
                display: true,
              }]}
              />
            )}
        </Header>
      )
    }
    return '';
  }, [fileRef?.current?.getIsEdit(), section, pageStore.getSelectItem, disabled, readOnly, fileIsEdit])

  return (
    <Page
      className="c7n-kb-doc"
    >
      {
        !isFullScreen && getHeaders()
      }
      {!isFullScreen && <Breadcrumb title={queryString.parse(history.location.search).baseName || ''} />}
      <Content style={{
        padding: 0, height: '100%', margin: 0, overflowY: 'hidden',
      }}
      >
        <Watermark enable={watermarkEnable} content={waterMarkString} style={{ height: '100%' }}>
          <LoadingProvider loading={loading} style={{ height: '100%' }}>
            <ResizeContainer type="horizontal" style={{ overflow: 'hidden' }}>
              {searchVisible
                ? (
                  <SearchList
                    searchText={searchValue}
                    store={pageStore}
                    onClearSearch={handleClearSearch}
                    onClickSearch={loadPage}
                    searchId={selectId}
                  />
                ) : null}
              {!searchVisible && !isFullScreen
                ? (
                  <Section
                    size={{
                      width: 230,
                      minWidth: 230,
                      maxWidth: 600,
                    }}
                    style={{
                      minWidth: 200,
                      maxWidth: 600,
                    }}
                  >
                    <div className="c7n-kb-doc-left">
                      <WorkSpace // 树结构
                        readOnly={disabled}
                        forwardedRef={workSpaceRef}
                        onClick={loadPage}
                        onSave={handleSpaceSave}
                        onDelete={handleDeleteDoc}
                        onCreate={handleCreateClickInTree}
                        onCancel={handleCancel}
                        onCreateDoc={handleCreateClick}
                        importOnline={handleImportClick}
                        onCopy={handleCopyClick}
                        onMove={handleMove}
                        onUpload={handleUpload}
                        itemUpload={itemUpload}
                      />
                    </div>
                  </Section>
                ) : null}
              {!searchVisible
                ? (
                  <Divider />
                ) : null}
              <Section
                style={{ flex: 1 }}
                size={{
                  width: 'auto',
                }}
              >
                <Loading loading={docLoading} allowSelfLoading style={{ height: '100%', overflow: 'auto' }} loadId="doc">
                  <div className="c7n-kb-doc-doc">
                    <div className="c7n-kb-doc-content">
                      {section === 'recent' && <HomePage pageStore={pageStore} onClick={loadWorkSpace} />}
                      {section === 'tree' && renderTreeSection()}
                      {section === 'template' && <Template />}
                    </div>
                  </div>
                </Loading>
              </Section>
              {pageStore.catalogVisible
                ? (
                  <Divider />
                ) : null}
              {pageStore.catalogVisible
                ? (
                  <Section
                    size={{
                      width: 200,
                      minWidth: 200,
                      maxWidth: 400,
                    }}
                    style={{
                      minWidth: 200,
                      maxWidth: 400,
                    }}
                  >
                    <Catalog store={pageStore} />
                  </Section>
                ) : null}
            </ResizeContainer>
          </LoadingProvider>
        </Watermark>
      </Content>
      {logVisible
        ? (
          <DocDetail onCollapse={() => setLogVisible(false)} store={pageStore} />
        ) : null}
      <Permission
        key="adminDelete"
        type={levelType}
        projectId={proId}
        organizationId={orgId}
        service={levelType === 'project'
          ? ['choerodon.code.project.cooperation.knowledge.ps.doc.delete']
          : ['choerodon.code.organization.knowledge.ps.doc.delete']}
      >
        {null}
      </Permission>
      <DocModal
        store={pageStore}
        selectId={selectId}
        mode={mode}
        refresh={loadWorkSpace}
        handleDeleteDraft={handleDeleteDraft}
        handleLoadDraft={handleLoadDraft}
      />
       <input
              ref={uploadInput}
              type="file"
              onChange={beforeUpload}
              style={{ display: 'none' }}
            />
    </Page>
  );
}

export default withRouter(injectIntl(observer(DocHome)));
