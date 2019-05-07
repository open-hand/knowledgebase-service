import axios from 'axios';
import { observable, action, computed } from 'mobx';
import { store, stores } from '@choerodon/boot';

const { AppState } = stores;

@store('DocStore')
class DocStore {
  @observable workSpace = [];

  @action setWorkSpace(data) {
    this.workSpace = data;
  }

  @computed get getWorkSpace() {
    return this.workSpace;
  }

  loadWorkSpace = () => {
    axios.get(`/knowledge/v1/organizations/${AppState.currentMenuType.organizationId}/work_space/tree?id=0`).then((res) => {
      this.setWorkSpace(res);
    });
  };
}

const docStore = new DocStore();
export default docStore;
