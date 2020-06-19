import axios from 'axios';
import { observable, action, computed, toJS } from 'mobx';
import { store, Choerodon } from '@choerodon/boot';
import FileSaver from 'file-saver';

const FileUploadTimeout = 300000;

@store('DocStore')
class DocStore {
  @observable apiGateway = '';

  @observable orgId = '';

  @action initCurrentMenuType(data) {
    const { type, id, organizationId } = data;
    this.apiGateway = `/knowledge/v1/${type}s/${id}`;
    this.orgId = organizationId;
  }

  // 空间数据
  @observable workSpace = [];

  @action setWorkSpace(data) {
    this.workSpace = data;
    const map = {};
    data.forEach((item) => {
      map[item.code] = item.data;
    });
    this.workSpaceMap = map;
  }

  @computed get getWorkSpace() {
    return toJS(this.workSpace);
  }

  // 空间code
  @observable spaceCode = false;

  @computed get getSpaceCode() {
    return this.spaceCode;
  }

  // 空间数据Map {'pro': proData, 'org': orgData}
  @observable workSpaceMap = {};

  @action setWorkSpaceMap(code, data, flag = true) {
    if (flag) {
      this.spaceCode = code;
    }
    this.workSpaceMap = {
      ...this.workSpaceMap,
      [code]: data,
    };
  }

  @computed get getWorkSpaceMap() {
    return toJS(this.workSpaceMap);
  }

  @computed get getCurrentSpace() {
    return this.workSpaceMap[this.spaceCode];
  }

  @action setCurrentSpace(data) {
    this.workSpaceMap = {
      ...this.workSpaceMap,
      [this.spaceCode]: data,
    };
  }

  // 文档
  @observable doc = false;

  @action setDoc(data) {
    const map = {};
    this.workSpace.forEach((item) => {
      if (item.data.items[data.id]) {
        this.spaceCode = item.code;
      }
    });
    this.doc = data;
  }

  @computed get getDoc() {
    return toJS(this.doc);
  }

  // 评论
  @observable comment = [];

  @action setComment(data) {
    this.comment = data;
  }

  @computed get getComment() {
    return toJS(this.comment);
  }

  // 附件
  @observable attachment = [];

  @action setAttachment(data) {
    this.attachment = data;
  }

  @computed get getAttachment() {
    return toJS(this.attachment);
  }

  // 日志
  @observable log = [];

  @action setLog(data) {
    this.log = data;
  }

  @computed get getLog() {
    return toJS(this.log);
  }

  // 版本
  @observable version = [];

  @action setVersion(data) {
    this.version = data;
  }

  @computed get getVersion() {
    return toJS(this.version);
  }

  // 某个版本的文档
  @observable docVersion = false;

  @action setDocVersion(data) {
    this.docVersion = data;
  }

  @computed get getDocVersion() {
    return toJS(this.docVersion);
  }

  // 版本比较结果
  @observable docCompare = false;

  @action setDocCompare(data) {
    this.docCompare = data;
  }

  @computed get getDocCompare() {
    return toJS(this.docCompare);
  }

  // 附件
  @observable fileList = [];

  @action setFileList(data) {
    this.fileList = data;
  }

  @computed get getFileList() {
    return toJS(this.fileList);
  }

  // 分享配置
  @observable share = {};

  @action setShare(data) {
    this.share = data;
  }

  @computed get getShare() {
    return toJS(this.share);
  }

  // 空间
  @observable shareWorkSpace = {
    rootId: -1,
    items: {},
  };

  @action setShareWorkSpace(data) {
    this.shareWorkSpace = data;
  }

  @computed get getShareWorkSpace() {
    return toJS(this.shareWorkSpace);
  }

  @observable catalogVisible = false;

  @action setCatalogVisible(data) {
    this.catalogVisible = data;
  }

  @computed get getCatalogVisible() {
    return this.catalogVisible;
  }

  // 分享文档
  @observable shareDoc = false;

  @action setShareDoc(data) {
    this.shareDoc = data;
    this.fileList = data.pageAttachments;
  }

  @computed get getShareDoc() {
    return toJS(this.shareDoc);
  }

  // 分享附件
  @observable shareAttachment = [];

  @action setShareAttachment(data) {
    this.shareAttachment = data;
  }

  @computed get getShareAttachment() {
    return toJS(this.shareAttachment);
  }

  // 文档移动树形结构
  @observable moveTree = [];

  @action setMoveTree(data) {
    this.moveTree = data;
  }

  @computed get getMoveTree() {
    return toJS(this.moveTree);
  }

  // 搜索结果
  @observable searchList = [];

  @action setSearchList(data) {
    this.searchList = data;
  }

  @computed get getSearchList() {
    return this.searchList;
  }

  // Modal弹窗控制

  // 分享Modal
  @observable shareVisible = false;

  @action setShareVisible(data) {
    this.shareVisible = data;
  }

  @computed get getShareVisible() {
    return this.shareVisible;
  }

  // 导入Modal
  @observable importVisible = false;

  @action setImportVisible(data) {
    this.importVisible = data;
  }

  @computed get getImportVisible() {
    return this.importVisible;
  }

  // 移动Modal
  @observable moveVisible = false;

  @action setMoveVisible(data) {
    this.moveVisible = data;
  }

  @computed get getMoveVisible() {
    return this.moveVisible;
  }

  // 草稿Modal
  @observable draftVisible = false;

  @action setDraftVisible(data) {
    this.draftVisible = data;
  }

  @computed get getDraftVisible() {
    return this.draftVisible;
  }

  /**
   * 加载完整空间
   * @param id 默认展开文档id
   */
  loadWorkSpaceAll = id => axios.get(`${this.apiGateway}/work_space/all_tree?organizationId=${this.orgId}${id ? `&expandWorkSpaceId=${id}` : ''}`).then((res) => {
    if (res && !res.failed) {
      this.setWorkSpace(res);
    }
    return res;
  }).catch((e) => {
    Choerodon.prompt('加载失败！');
  });

  /**
   * 创建空间
   * @param vo
   */
  createWorkSpace = vo => axios.post(`${this.apiGateway}/work_space?organizationId=${this.orgId}`, vo).then((res) => {
    if (res && !res.failed) {
      return res;
    } else {
      Choerodon.prompt(res.message);
      return false;
    }
  }).catch(() => {
    Choerodon.prompt('创建失败！');
    return false;
  });

  /**
   * 创建文档
   * @param vo
   */
  createDoc = vo => axios.post(`${this.apiGateway}/page?organizationId=${this.orgId}`, vo).then((res) => {
    if (res && !res.failed) {
      return res;
    } else {
      Choerodon.prompt(res.message);
      return false;
    }
  }).catch(() => {
    Choerodon.prompt('创建失败！');
    return false;
  });

  /**
   * 加载文档
   * @param id
   * @param searchValue
   */
  loadDoc = (id, searchValue) => axios.get(`${this.apiGateway}/work_space/${id}?organizationId=${this.orgId}${searchValue ? `&searchStr=${searchValue}` : ''}`).then((res) => {
    if (res && !res.failed) {
      this.setDoc(res);
      if (res.hasDraft) {
        this.setDraftVisible(true);
      } else {
        this.setDraftVisible(false);
      }
    }
    return res;
  }).catch(() => {
    Choerodon.prompt('加载知识文档失败！');
  });

  /**
   * 编辑文档
   * @param id
   * @param doc
   */
  editDoc = (id, doc) => axios.put(`${this.apiGateway}/work_space/${id}?organizationId=${this.orgId}`, doc).then((res) => {
    if (res && !res.failed) {
      this.setDraftVisible(false);
      this.setDoc(res);
      if (this.workSpace.length) {
        const spaceCode = this.workSpace[this.workSpace.rootId].code;
        const spaceData = this.workSpaceMap[spaceCode];
        const newWorkSpace = {
          ...spaceData,
          items: {
            ...spaceData.items,
            [id]: {
              ...spaceData.items[id],
              data: {
                title: doc.title,
              },
            },
          },
        };
        this.setWorkSpaceMap(spaceCode, newWorkSpace);
      }
      Choerodon.prompt('保存成功！');
    }
  }).catch((e) => {
    Choerodon.prompt('保存失败！');
  });

  /**
   * 自动保存
   * @param id
   * @param doc
   */
  autoSaveDoc = (id, doc) => {
    axios.put(`${this.apiGateway}/page/auto_save?organizationId=${this.orgId}&pageId=${id}`, doc).then((res) => {
      this.setDraftVisible(true);
      // Choerodon.prompt('自动保存成功！');
    }).catch(() => {
      Choerodon.prompt('自动保存失败！');
    });
  };

  /**
   * 加载草稿文档
   * @param id
   */
  loadDraftDoc = id => axios.get(`${this.apiGateway}/page/draft_page?organizationId=${this.orgId}&pageId=${id}`).then((res) => {
    if (res && !res.failed) {
      this.setDoc({
        ...this.doc,
        pageInfo: {
          ...this.doc.pageInfo,
          content: res,
        },
      });
      this.setDraftVisible(true);
    }
    return res;
  }).catch(() => {
    Choerodon.prompt('加载知识文档失败！');
  });

  /**
   * 删除草稿
   * @param id
   */
  deleteDraftDoc = id => axios.delete(`${this.apiGateway}/page/delete_draft?organizationId=${this.orgId}&pageId=${id}`).then(() => {
    this.setDraftVisible(false);
  });

  /**
   * 创建者删除文档，后端进行创建人校验
   * @param id
   */
  deleteDoc = id => axios.delete(`${this.apiGateway}/work_space/delete_my/${id}?organizationId=${this.orgId}`);

  /**
   * 管理员删除文档，后端进行权限校验
   * @param id
   */
  adminDeleteDoc = id => axios.delete(`${this.apiGateway}/work_space/${id}?organizationId=${this.orgId}`);

  /**
   * 移动空间
   * @param id 移动到空间id
   * @param vo
   */
  moveWorkSpace = (id, vo) => axios.post(`${this.apiGateway}/work_space/to_move/${id}?organizationId=${this.orgId}`, vo).then((res) => {
    if (res && res.failed) {
      Choerodon.prompt(res.message);
    }
  }).catch(() => {
    Choerodon.prompt('移动失败！');
    return false;
  });

  /**
   * 设置默认编辑模式
   * @param vo 评论
   */
  editDefaultMode = vo => axios.post(`${this.apiGateway}/user_setting?organizationId=${this.orgId}`, vo);

  /**
   * 创建评论
   * @param vo 评论
   */
  createComment = vo => axios.post(`${this.apiGateway}/page_comment?organizationId=${this.orgId}`, vo).then((res) => {
    if (res && res.failed) {
      Choerodon.prompt(res.message);
    } else {
      this.loadLog(vo.pageId);
      this.setComment([
        res,
        ...this.comment,
      ]);
    }
  }).catch(() => {
    Choerodon.prompt('评论失败！');
    return false;
  });

  /**
   * 加载评论
   * @param id
   */
  loadComment = id => axios.get(`${this.apiGateway}/page_comment/list?pageId=${id}&organizationId=${this.orgId}`).then((res) => {
    this.setComment(res);
  }).catch(() => {
    Choerodon.prompt('加载评论失败！');
  });

  /**
   * 编辑评论
   * @param id
   * @param vo
   */
  editComment = (id, vo) => axios.put(`${this.apiGateway}/page_comment/${id}?organizationId=${this.orgId}`, vo).then((res) => {
    this.loadLog(vo.pageId);
    this.setComment([
      res,
      ...this.comment.filter(c => c.id !== res.id),
    ]);
  }).catch(() => {
    Choerodon.prompt('加载评论失败！');
  });

  /**
   * 删除评论
   * @param id
   */
  deleteComment = id => axios.delete(`${this.apiGateway}/page_comment/delete_my/${id}?organizationId=${this.orgId}`).then((res) => {
    this.loadLog(this.getDoc.pageInfo.id);
    this.setComment([
      ...this.comment.filter(c => c.id !== id),
    ]);
  }).catch(() => {
    Choerodon.prompt('删除评论失败！');
  });

  /**
   * admin删除评论，校验权限
   * @param id
   */
  adminDeleteComment = id => axios.delete(`${this.apiGateway}/page_comment/${id}?organizationId=${this.orgId}`).then((res) => {
    this.loadLog(this.getDoc.pageInfo.id);
    this.setComment([
      ...this.comment.filter(c => c.id !== id),
    ]);
  }).catch(() => {
    Choerodon.prompt('删除评论失败！');
  });

  /**
   * 加载附件
   * @param id
   */
  loadAttachment = id => axios.get(`${this.apiGateway}/page_attachment/list?pageId=${id}&organizationId=${this.orgId}`).then((res) => {
    this.setAttachment(res.map(file => ({
      ...file,
      uid: file.id,
    })));
  }).catch(() => {
    Choerodon.prompt('加载附件失败！');
  });

  /**
   * 删除附件
   * @param id
   */
  deleteFile = (id) => {
    axios.delete(`${this.apiGateway}/page_attachment/${id}?organizationId=${this.orgId}`).then((res) => {
      if (res && res.failed) {
        Choerodon.prompt('删除失败');
      } else {
        this.loadLog(this.getDoc.pageInfo.id);
        this.setAttachment(this.attachment.filter(file => file.id !== id));
        Choerodon.prompt('删除成功');
      }
    }).catch(() => {
      Choerodon.prompt('删除失败，请稍后重试');
    });
  };

  /**
   * 加载日志
   * @param id
   */
  loadLog = id => axios.get(`${this.apiGateway}/page_log/${id}?organizationId=${this.orgId}`).then((res) => {
    this.setLog(res);
  }).catch(() => {
    Choerodon.prompt('加载日志失败！');
  });

  /**
   * 加载版本
   * @param id
   */
  loadVersion = id => axios.get(`${this.apiGateway}/page_version/list?organizationId=${this.orgId}&pageId=${id}`).then((res) => {
    this.setVersion(res);
  }).catch(() => {
    Choerodon.prompt('加载版本失败！');
  });

  /**
   * 比较版本
   * @param firstVersionId
   * @param secondVersionId
   * @param id
   */
  compareVersion = (firstVersionId, secondVersionId, id) => axios.get(`${this.apiGateway}/page_version/compare?organizationId=${this.orgId}&firstVersionId=${firstVersionId}&secondVersionId=${secondVersionId}&pageId=${id}`).then((res) => {
    this.setDocCompare(res.diffContent);
  }).catch(() => {
    Choerodon.prompt('加载版本失败！');
  });

  /**
   * 回滚
   * @param versionId
   * @param id
   */
  rollbackVersion =
    (versionId, id) => axios.get(`${this.apiGateway}/page_version/rollback?organizationId=${this.orgId}&versionId=${versionId}&pageId=${id}`)
      .then(res => res).catch(() => {
        Choerodon.prompt('回滚版本失败！');
      });

  /**
   * 查看某个版本
   * @param versionId
   * @param id
   */
  loadDocByVersion = (versionId, id) => axios.get(`${this.apiGateway}/page_version/${versionId}?organizationId=${this.orgId}&pageId=${id}`).then((res) => {
    this.setDocVersion(res.content);
  }).catch(() => {
    Choerodon.prompt('加载版本失败！');
  });

  /**
   * 导出pdf
   * @param id
   * @param fileName
   */
  exportPdf = (id, fileName) => axios.get(`${this.apiGateway}/page/export_pdf?pageId=${id}&organizationId=${this.orgId}`, { responseType: 'arraybuffer', headers: { 'Access-Control-Allow-Origin': '*' } }).then((data) => {
    // data为arraybuffer格式，判断已经无效
    if (data && !data.failed) {
      const blob = new Blob([data], { type: 'application/pdf' });
      FileSaver.saveAs(blob, `${fileName}.pdf`);
      Choerodon.prompt('导出成功');
    } else {
      Choerodon.prompt('网络错误，请重试。');
    }
  });

  queryShareMsg = id => axios.get(`${this.apiGateway}/work_space_share?work_space_id=${id}&organizationId=${this.orgId}`).then((data) => {
    if (data && !data.failed) {
      this.setShare(data);
    } else {
      Choerodon.prompt('网络错误，请重试。');
    }
  });

  updateShare = (id, spaceId, vo) => axios.put(`${this.apiGateway}/work_space_share/${id}?organizationId=${this.orgId}`, vo).then((res) => {
    if (res && !res.failed) {
      this.setShare(res);
    } else {
      this.queryShareMsg(spaceId);
      Choerodon.prompt('网络错误，请重试。');
    }
  }).catch(() => {
    Choerodon.prompt('修改失败！');
  });

  importWord = (data) => {
    const axiosConfig = {
      headers: { 'content-type': 'multipart/form-data' },
      timeout: FileUploadTimeout,
    };
    return axios.post(`${this.apiGateway}/page/import_word?organizationId=${this.orgId}`, data, axiosConfig);
  };

  /**
   * 分享-查询空间
   * @param token
   */
  getSpaceByToken = token => axios.get(`/knowledge/v1/work_space_share/tree?token=${token}`).then((data) => {
    if (data && !data.failed) {
      this.setShareWorkSpace(data);
    } else {
      Choerodon.prompt('请求失败');
      this.setShareWorkSpace({
        noAccess: true,
      });
    }
  });

  getDocByToken = (id, token) => axios.get(`/knowledge/v1/work_space_share/page?work_space_id=${id}&token=${token}`).then((data) => {
    if (data && !data.failed) {
      this.setShareDoc(data);
    } else {
      Choerodon.prompt('请求失败');
    }
  });

  getAttachmentByToken = (id, token) => axios.get(`/knowledge/v1/work_space_share/page_attachment?page_id=${id}&token=${token}`).then((data) => {
    if (data && !data.failed) {
      this.setAttachment(data.map(file => ({
        ...file,
        uid: file.id,
      })));
    } else {
      Choerodon.prompt('请求失败');
    }
  });

  exportPdfByToken = (id, fileName, token) => axios.get(`/knowledge/v1/work_space_share/export_pdf?pageId=${id}&token=${token}`, { responseType: 'arraybuffer', headers: { 'Access-Control-Allow-Origin': '*' } }).then((data) => {
    // data为arraybuffer格式，判断已经无效
    if (data && !data.failed) {
      const blob = new Blob([data], { type: 'application/pdf' });
      FileSaver.saveAs(blob, `${fileName}.pdf`);
      Choerodon.prompt('导出成功');
    } else {
      Choerodon.prompt('网络错误，请重试。');
    }
  });

  queryMoveTree = () => axios.get(`${this.apiGateway}/work_space?organizationId=${this.orgId}`).then((data) => {
    if (data && !data.failed) {
      const tree = [{
        children: data,
        id: 0,
        name: '全部',
        route: '',
      }];
      this.setMoveTree(tree);
    } else {
      Choerodon.prompt('请求失败');
    }
  });

  querySearchList = str => axios.get(`${this.apiGateway}/page/full_text_search?organizationId=${this.orgId}&searchStr=${str}`).then((data) => {
    if (data && !data.failed) {
      this.setSearchList(data);
    } else {
      Choerodon.prompt('请求失败');
      this.setSearchList([]);
    }
  }).catch(() => {
    Choerodon.prompt('请求失败');
    this.setSearchList([]);
  });
}

const docStore = new DocStore();
export default docStore;
