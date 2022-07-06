package io.choerodon.kb.api.controller.v1;

import com.yqcloud.wps.maskant.adaptor.WPSFileAdaptor;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2022/7/6
 */
@RestController
@RequestMapping("/v1/test")
public class TestController {

    @Autowired
    private WPSFileAdaptor wpsFileAdaptor;
    @Autowired
    private WorkSpaceService workSpaceService;


    @PostMapping
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation("导入word文档为markdown数据")
    public ResponseEntity<Void> test(
            @ApiParam(value = "word文档", required = true)
            @RequestParam("file") MultipartFile file) {
        workSpaceService.uploadMultipartFileWithMD5(1L, "", file.getOriginalFilename(), 0, null, file);
//        wpsFileAdaptor.uploadMultipartFileWithMD5(1L, "knowledgebase-service", "", file.getOriginalFilename(), 0, null, file, null);

        return null;
    }

}
