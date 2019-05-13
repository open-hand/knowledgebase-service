import axios from 'axios';
import { observable, action, computed, toJS } from 'mobx';
import { store, stores } from '@choerodon/boot';
import { complexTree } from '../../../components/WorkSpace/mockData/complexTree';

const { AppState } = stores;

@store('DocStore')
class DocStore {
  @observable workSpace = {
    rootId: -1,
    items: {},
  };

  @observable doc = {
    title: '',
    content: '',
  };

  @action setWorkSpace(data) {
    this.workSpace = data;
  }

  @computed get getWorkSpace() {
    return toJS(this.workSpace);
  }

  @action setDoc(data) {
    this.doc = data;
  }

  @computed get getDoc() {
    return toJS(this.doc);
  }

  /**
   * 加载空间信息
   */
  loadWorkSpace = () => {
    const orgId = AppState.currentMenuType.organizationId;
    return axios.get(`/knowledge/v1/organizations/${orgId}/work_space/first/tree`).then((res) => {
      this.setWorkSpace(res);
    }).catch(() => {
      Choerodon.prompt('加载失败！');
    });
  };

  /**
   * 加载子级空间信息
   */
  loadWorkSpaceByParent = (ids) => {
    const orgId = AppState.currentMenuType.organizationId;
    return axios.post(`/knowledge/v1/organizations/${orgId}/work_space/tree`, ids).then((res) => {
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
  };

  /**
   * 创建空间
   * @param dto
   */
  createWorkSpace = (dto) => {
    const orgId = AppState.currentMenuType.organizationId;
    return axios.post(`/knowledge/v1/organizations/${orgId}/work_space`, dto).then((res) => {
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
  };

  /**
   * 加载文档
   * @param id
   */
  loadDoc = (id) => {
    const orgId = AppState.currentMenuType.organizationId;
    axios.get(`/knowledge/v1/organizations/${orgId}/work_space/${id}`).then((res) => {
      this.setDoc(res);
      // this.setDoc({
      //   title: `title ${id}`,
      //   content: `Choerodon ${id}`,
      // });
    }).catch(() => {
      this.setDoc({
        title: `title ${id}`,
        content: `Choerodon ${id}`,
      });
    });
  };

  /**
   * 编辑文档
   * @param id
   * @param doc
   */
  editDoc = (id, doc) => {
    const orgId = AppState.currentMenuType.organizationId;
    axios.put(`/knowledge/v1/organizations/${orgId}/work_space/${id}`, doc).then((res) => {
      this.setDoc(res);
    });
  };

  /**
   * 删除文档
   * @param id
   */
  deleteDoc = (id) => {
    const orgId = AppState.currentMenuType.organizationId;
    return axios.delete(`/knowledge/v1/organizations/${orgId}/work_space/${id}`);
  };
}

const docStore = new DocStore();
export default docStore;
