import axios from 'axios';
import { observable, action, computed, toJS } from 'mobx';
import { store, stores } from '@choerodon/boot';
import { complexTree } from '../../../components/WorkSpace/mockData/complexTree';

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
  @observable proWorkSpace = [];

  @action setProWorkSpace(data) {
    this.proWorkSpace = data;
  }

  @computed get getProWorkSpace() {
    return toJS(this.proWorkSpace);
  }

  // 文章
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

  /**
   * 加载空间信息
   */
  loadWorkSpace = () => axios.get(`${this.apiGetway}/work_space/first/tree`).then((res) => {
    this.setWorkSpace(res);
  }).catch(() => {
    Choerodon.prompt('加载失败！');
  });

  /**
   * 加载组织下项目空间信息
   */
  loadProWorkSpace = () => axios.get(`${this.apiGetway}/work_space/project/tree`).then((res) => {
    this.setProWorkSpace(res);
  }).catch(() => {
    Choerodon.prompt('加载失败！');
  });

  /**
   * 加载子级空间信息
   */
  loadWorkSpaceByParent = ids => axios.post(`${this.apiGetway}/work_space/tree`, ids).then((res) => {
    this.setWorkSpace({
      ...this.workSpace,
      items: {
        ...this.workSpace.items,
        ...res,
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
   * 加载文档
   * @param id
   */
  loadDoc = id => axios.get(`${this.apiGetway}/work_space/${id}`).then((res) => {
    this.setDoc(res);
  }).catch(() => {
    Choerodon.prompt('加载失败！');
  });

  /**
   * 编辑文档
   * @param id
   * @param doc
   */
  editDoc = (id, doc) => {
    axios.put(`${this.apiGetway}/work_space/${id}`, doc).then((res) => {
      this.loadLog(this.getDoc.pageInfo.id);
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
    });
  };

  /**
   * 删除文档
   * @param id
   */
  deleteDoc = id => axios.delete(`${this.apiGetway}/work_space/${id}`);

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
    Choerodon.prompt('加载失败！');
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
    Choerodon.prompt('加载失败！');
  });

  /**
   * 删除评论
   * @param id
   */
  deleteComment = id => axios.delete(`${this.apiGetway}/page_comment/${id}`).then((res) => {
    this.loadLog(this.getDoc.pageInfo.id);
    this.setComment([
      ...this.comment.filter(c => c.id !== id),
    ]);
  }).catch(() => {
    Choerodon.prompt('删除失败！');
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
    Choerodon.prompt('加载失败！');
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
      this.loadLog(this.getDoc.pageInfo.id);
      this.setAttachment(this.attachment.filter(file => file.id !== id));
      Choerodon.prompt('删除成功');
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
    Choerodon.prompt('加载失败！');
  });

  /**
   * 加载版本
   * @param id
   */
  loadVersion = id => axios.get(`${this.apiGetway}/page_version/list?pageId=${id}`).then((res) => {
    this.setVersion(res);
  }).catch(() => {
    Choerodon.prompt('加载失败！');
  });
}

const docStore = new DocStore();
export default docStore;
