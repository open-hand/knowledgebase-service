package io.choerodon.kb.api.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import difflib.*;
import java.util.ArrayList;
import java.util.List;

/**
 * diff数据，position都是取原序列的位置
 *
 * @author shinan.chen
 * @since 2019/5/16
 */
public class TextDiffVO {

    List<Delta<String>> deleteData;
    List<Delta<String>> insertData;
    List<Delta<String>> changeData;

    public TextDiffVO() {
    }

    public TextDiffVO(List<Delta<String>> insertData, List<Delta<String>> deleteData, List<Delta<String>> changeData) {
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

    public static TextDiffVO jsonToVO(JSONObject json) {
        TextDiffVO vo = new TextDiffVO();
        vo.setChangeData(handleDeltaList(Delta.TYPE.CHANGE, json.getJSONArray("changeData")));
        vo.setInsertData(handleDeltaList(Delta.TYPE.INSERT, json.getJSONArray("insertData")));
        vo.setDeleteData(handleDeltaList(Delta.TYPE.DELETE, json.getJSONArray("deleteData")));
        return vo;
    }

    private static List<Delta<String>> handleDeltaList(Delta.TYPE type, JSONArray array) {
        List<Delta<String>> deltas = new ArrayList<>(array.size());
        for (Object value : array) {
            if (value instanceof JSONObject) {
                JSONObject json = (JSONObject) value;
                Chunk<String> revised = JSON.parseObject(json.getString("revised"), new TypeReference<Chunk<String>>() {
                });
                Chunk<String> original = JSON.parseObject(json.getString("original"), new TypeReference<Chunk<String>>() {
                });
                switch (type) {
                    case DELETE:
                        Delta<String> delete = new DeleteDelta(original, revised);
                        deltas.add(delete);
                        break;
                    case CHANGE:
                        Delta<String> change = new ChangeDelta(original, revised);
                        deltas.add(change);
                        break;
                    case INSERT:
                        Delta<String> insert = new InsertDelta(original, revised);
                        deltas.add(insert);
                }

            }
        }
        return deltas;
    }
}
