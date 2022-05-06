package io.choerodon.kb.api.controller.v1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.kb.app.service.OnlyOfficeService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2022/5/6
 */
@RestController
@RequestMapping("/v1/projects/{project_id}/only_office")
public class OnlyOfficeCallBackController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnlyOfficeCallBackController.class);


    @Autowired
    private OnlyOfficeService onlyOfficeService;

    @Permission(permissionPublic = true)
    @ApiOperation("only_office保存编辑的回调")
    @PostMapping(value = "/save/file")
    public ResponseEntity<Void> saveFile(@ApiParam(value = "项目id", required = true)
                                                    @PathVariable(value = "project_id") Long projectId,
                                                    @RequestBody JSONObject obj) {
        LOGGER.info("通知此文件目前有哪些人正在协作param:{}", JSON.toJSONString(obj));
        onlyOfficeService.saveFile(projectId,obj);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
