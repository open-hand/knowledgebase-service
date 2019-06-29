import axios from 'axios';
import { observable, action, computed, toJS } from 'mobx';
import { store, stores } from '@choerodon/boot';
import FileSaver from 'file-saver';

const { AppState } = stores;

@store('DocStore')
class DocStore {
  @observable apiGetway = '';

  @observable orgId = '';

  @action initCurrentMenuType(data) {
    const { type, id, organizationId } = data;
    this.apiGetway = `/knowledge/v1/${type}s/${id}`;
    this.orgId = organizationId;
  }

  // 空间
  @observable workSpace = {
    rootId: -1,
    items: {},
  };

  @action setWorkSpace(data) {
    this.workSpace = data;
  }

  @computed get getWorkSpace() {
    return toJS(this.workSpace);
  }

  // 项目空间
  @observable proWorkSpace = {};

  @action setProWorkSpace(data) {
    this.proWorkSpace = data;
  }

  @computed get getProWorkSpace() {
    return toJS(this.proWorkSpace);
  }

  // 项目列表
  @observable proList = [];

  @action setProList(data) {
    this.proList = data;
  }

  @computed get getProList() {
    return toJS(this.proList);
  }

  // 文档
  @observable doc = false;

  @action setDoc(data) {
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

  // 分享文档
  @observable shareDoc = false;

  @action setShareDoc(data) {
    this.shareDoc = data;
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

  /**
   * 加载完整空间
   * @param id 默认展开文档id
   */
  loadWorkSpaceAll = id => axios.get(`${this.apiGetway}/work_space/all_tree${id ? `?expandWorkSpaceId=${id}` : ''}`).then((res) => {
    if (res && !res.failed) {
      this.setWorkSpace(res);
    }
    return res;
  }).catch((e) => {
    Choerodon.prompt('加载失败！');
  });

  /**
   * 加载组织下项目空间信息
   */
  loadProWorkSpace = () => axios.get(`${this.apiGetway}/work_space/project/tree`).then((res) => {
    if (res && res.failed) {
      Choerodon.prompt('加载失败！');
    } else if (res.length) {
      const proList = [];
      const proWorkSpace = {};
      res.forEach((pro) => {
        proList.push({
          projectId: pro.projectId,
          projectName: pro.projectName,
        });
        proWorkSpace[pro.projectId] = pro.workSpace;
      });
      this.setProList(proList);
      this.setProWorkSpace(proWorkSpace);
    } else {
      this.setProList([]);
      this.setProWorkSpace({});
    }
  }).catch(() => {
    Choerodon.prompt('加载失败！');
  });

  /**
   * 组织加载项目子级空间信息
   */
  loadProWorkSpaceByParent = (ids, proId) => axios.post(`/knowledge/v1/projects/${proId}/work_space/tree`, ids).then((res) => {
    this.setProWorkSpace({
      ...this.proWorkSpace,
      [proId]: {
        ...this.proWorkSpace[proId],
        items: {
          ...this.proWorkSpace[proId].items,
          ...res,
        },
      },
    });
  }).catch(() => {
    Choerodon.prompt('加载失败！');
  });

  /**
   * 创建空间
   * @param dto
   */
  createWorkSpace = dto => axios.post(`${this.apiGetway}/work_space`, dto).then((res) => {
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
   * @param dto
   */
  createDoc = dto => axios.post(`${this.apiGetway}/page?organizationId=${this.orgId}`, dto).then((res) => {
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
   */
  loadDoc = id => axios.get(`${this.apiGetway}/work_space/${id}`).then((res) => {
    if (res && !res.failed) {
      this.setDoc(res);
    }
    return res;
  }).catch(() => {
    Choerodon.prompt('加载文档失败！');
  });

  /**
   * 组织加载项目文档
   * @param id
   * @param proId
   */
  loadProDoc = (id, proId) => axios.get(`/knowledge/v1/projects/${proId}/work_space/${id}`).then((res) => {
    this.setDoc(res);
  }).catch(() => {
    Choerodon.prompt('加载文档失败！');
  });

  /**
   * 编辑文档
   * @param id
   * @param doc
   */
  editDoc = (id, doc) => {
    axios.put(`${this.apiGetway}/work_space/${id}`, doc).then((res) => {
      if (res && !res.failed) {
        this.setDoc(res);
        this.setWorkSpace({
          ...this.workSpace,
          items: {
            ...this.workSpace.items,
            [id]: {
              ...this.workSpace.items[id],
              data: {
                title: doc.title,
              },
            },
          },
        });
        Choerodon.prompt('保存成功！');
      }
    }).catch(() => {
      Choerodon.prompt('保存失败！');
    });
  };

  /**
   * 创建者删除文档，后端进行创建人校验
   * @param id
   */
  deleteDoc = id => axios.delete(`${this.apiGetway}/work_space/delete_my/${id}`);

  /**
   * 管理员删除文档，后端进行权限校验
   * @param id
   */
  adminDeleteDoc = id => axios.delete(`${this.apiGetway}/work_space/${id}`);

  /**
   * 移动空间
   * @param id 移动到空间id
   * @param dto
   */
  moveWorkSpace = (id, dto) => {
    axios.post(`${this.apiGetway}/work_space/to_move/${id}`, dto).then((res) => {
      if (res && res.failed) {
        Choerodon.prompt(res.message);
      }
    }).catch(() => {
      Choerodon.prompt('移动失败！');
      return false;
    });
  };

  /**
   * 创建评论
   * @param dto 评论
   */
  createComment = dto => axios.post(`${this.apiGetway}/page_comment`, dto).then((res) => {
    if (res && res.failed) {
      Choerodon.prompt(res.message);
    } else {
      this.loadLog(dto.pageId);
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
  loadComment = id => axios.get(`${this.apiGetway}/page_comment/list?pageId=${id}`).then((res) => {
    this.setComment(res);
  }).catch(() => {
    Choerodon.prompt('加载评论失败！');
  });

  /**
   * 编辑评论
   * @param id
   * @param dto
   */
  editComment = (id, dto) => axios.put(`${this.apiGetway}/page_comment/${id}`, dto).then((res) => {
    this.loadLog(dto.pageId);
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
  deleteComment = id => axios.delete(`${this.apiGetway}/page_comment/delete_my/${id}`).then((res) => {
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
  adminDeleteComment = id => axios.delete(`${this.apiGetway}/page_comment/${id}`).then((res) => {
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
  loadAttachment = id => axios.get(`${this.apiGetway}/page_attachment/list?pageId=${id}`).then((res) => {
    this.setAttachment(res.map(file => ({
      ...file,
      uid: file.id,
    })));
  }).catch(() => {
    Choerodon.prompt('加载附件失败！');
  });

  /**
   * 上传附件
   * @param data
   * @param config
   */
  uploadFile = (data, config) => {
    const {
      pageId, versionId,
    } = config;
    const axiosConfig = {
      headers: { 'content-type': 'multipart/form-datal' },
    };
    return axios.post(
      `${this.apiGetway}/page_attachment?pageId=${pageId}&versionId=${versionId}`,
      data,
      axiosConfig,
    ).then((res) => {
      this.loadLog(this.getDoc.pageInfo.id);
      this.setAttachment([
        ...this.attachment,
        ...res.map(file => ({
          ...file,
          uid: file.id,
          status: 'done',
        })),
      ]);
      Choerodon.prompt('上传成功');
    });
  };

  /**
   * 删除附件
   * @param id
   */
  deleteFile = (id) => {
    axios.delete(`${this.apiGetway}/page_attachment/${id}`).then((res) => {
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
  loadLog = id => axios.get(`${this.apiGetway}/page_log/${id}`).then((res) => {
    this.setLog(res);
  }).catch(() => {
    Choerodon.prompt('加载日志失败！');
  });

  /**
   * 加载版本
   * @param id
   */
  loadVersion = id => axios.get(`${this.apiGetway}/page_version/list?organizationId=${this.orgId}&pageId=${id}`).then((res) => {
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
  compareVersion = (firstVersionId, secondVersionId, id) => axios.get(`${this.apiGetway}/page_version/compare?organizationId=${this.orgId}&firstVersionId=${firstVersionId}&secondVersionId=${secondVersionId}&pageId=${id}`).then((res) => {
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
    (versionId, id) => axios.get(`${this.apiGetway}/page_version/rollback?organizationId=${this.orgId}&versionId=${versionId}&pageId=${id}`)
      .then(res => res).catch(() => {
        Choerodon.prompt('回滚版本失败！');
      });

  /**
   * 查看某个版本
   * @param versionId
   * @param id
   */
  loadDocByVersion = (versionId, id) => axios.get(`${this.apiGetway}/page_version/${versionId}?organizationId=${this.orgId}&pageId=${id}`).then((res) => {
    this.setDocVersion(res.content);
  }).catch(() => {
    Choerodon.prompt('加载版本失败！');
  });

  /**
   * 导出pdf
   * @param id
   * @param fileName
   */
  exportPdf = (id, fileName) => axios.get(`${this.apiGetway}/page/export_pdf?pageId=${id}&organizationId=${this.orgId}`, { responseType: 'arraybuffer', headers: { 'Access-Control-Allow-Origin': '*' } }).then((data) => {
    // data为arraybuffer格式，判断已经无效
    if (data && !data.failed) {
      const blob = new Blob([data], { type: 'application/pdf' });
      FileSaver.saveAs(blob, fileName);
      Choerodon.prompt('导出成功');
    } else {
      Choerodon.prompt('网络错误，请重试。');
    }
  });

  migration = path => axios.post(`${this.apiGetway}/xwiki_data/migration`, { data: path || '' });

  queryShareMsg = id => axios.get(`${this.apiGetway}/work_space_share?work_space_id=${id}`).then((data) => {
    if (data && !data.failed) {
      this.setShare(data);
    } else {
      Choerodon.prompt('网络错误，请重试。');
    }
  });

  updateShare = (id, spaceId, dto) => axios.put(`${this.apiGetway}/work_space_share/${id}`, dto).then((res) => {
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
    };
    return axios.post(`${this.apiGetway}/page/import_word?organizationId=${this.orgId}`, data, axiosConfig);
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

  getCatalogByToken = (id, token) => axios.get(`/knowledge/v1/work_space_share/${id}/toc?token=${token}`).then((res) => {
    this.setCatalog(res);
  }).catch(() => {
    Choerodon.prompt('加载目录失败！');
  });

  exportPdfByToken = (id, fileName, token) => axios.get(`/knowledge/v1/work_space_share/export_pdf?pageId=${id}&token=${token}`, { responseType: 'arraybuffer', headers: { 'Access-Control-Allow-Origin': '*' } }).then((data) => {
    // data为arraybuffer格式，判断已经无效
    if (data && !data.failed) {
      const blob = new Blob([data], { type: 'application/pdf' });
      FileSaver.saveAs(blob, fileName);
      Choerodon.prompt('导出成功');
    } else {
      Choerodon.prompt('网络错误，请重试。');
    }
  });
}

const docStore = new DocStore();
export default docStore;
