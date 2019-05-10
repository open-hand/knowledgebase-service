import axios from 'axios';
import { observable, action, computed, toJS } from 'mobx';
import { store, stores } from '@choerodon/boot';
import { complexTree } from '../../../components/WorkSpace/mockData/complexTree';

const { AppState } = stores;

@store('DocStore')
class DocStore {
  @observable workSpace = {
    rootId: 0,
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
    // axios.get(`/knowledge/v1/organizations/${orgId}/work_space/tree?id=0`).then((res) => {
    //   this.setWorkSpace(res);
    // });
    this.setWorkSpace(complexTree);
  };

  /**
   * 加载文档
   * @param id
   */
  loadDoc = (id) => {
    const orgId = AppState.currentMenuType.organizationId;
    // axios.get(`/knowledge/v1/organizations/${orgId}/work_space/${id}`).then((res) => {
    //   this.setDoc(res);
    // });
    this.setDoc({
      title: `title ${id}`,
      content: `Choerodon ${id}`,
    });
  };

  /**
   * 编辑文档
   * @param id
   */
  editDoc = (id, doc) => {
    const orgId = AppState.currentMenuType.organizationId;
    // axios.put(`/knowledge/v1/organizations/${orgId}/work_space/${id}`, doc).then((res) => {
    //   this.setDoc(res);
    // });
    this.setDoc({
      title: 'test',
      content: 'test',
    });
  };

  /**
   * 删除文档
   * @param id
   */
  deleteDoc = (id) => {
    const orgId = AppState.currentMenuType.organizationId;
    // axios.delete(`/knowledge/v1/organizations/${orgId}/work_space/${id}`);
  };
}

const docStore = new DocStore();
export default docStore;
