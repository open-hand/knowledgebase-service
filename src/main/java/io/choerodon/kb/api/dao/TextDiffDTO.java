package io.choerodon.kb.api.dao;

import difflib.Delta;

import java.util.List;

/**
 * diff数据，position都是取原序列的位置
 *
 * @author shinan.chen
 * @since 2019/5/16
 */
public class TextDiffDTO {

    List<Delta<String>> deleteData;
    List<Delta<String>> insertData;
    List<Delta<String>> changeData;

    public TextDiffDTO(List<Delta<String>> insertData, List<Delta<String>> deleteData, List<Delta<String>> changeData) {
        this.insertData = insertData;
        this.deleteData = deleteData;
        this.changeData = changeData;
    }

    public List<Delta<String>> getInsertData() {
        return insertData;
    }

    public void setInsertData(List<Delta<String>> insertData) {
        this.insertData = insertData;
    }

    public List<Delta<String>> getDeleteData() {
        return deleteData;
    }

    public void setDeleteData(List<Delta<String>> deleteData) {
        this.deleteData = deleteData;
    }

    public List<Delta<String>> getChangeData() {
        return changeData;
    }

    public void setChangeData(List<Delta<String>> changeData) {
        this.changeData = changeData;
    }
}
