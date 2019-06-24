package io.choerodon.kb.api.controller.v1;

import io.choerodon.base.annotation.Permission;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.api.dao.PageDTO;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by Zenger on 2019/6/11.
 */
@RestController
@RequestMapping(value = "/v1/work_space_share")
public class WorkSpaceShareController {

    @Autowired
    private WorkSpaceShareService workSpaceShareService;

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的树形结构")
    @GetMapping(value = "/tree")
    public ResponseEntity<Map<String, Object>> queryTree(
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryTree(token),
                HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的页面信息")
    @GetMapping(value = "/page")
    public ResponseEntity<PageDTO> queryPage(
            @ApiParam(value = "工作空间ID", required = true)
            @RequestParam("work_space_id") Long workSpaceId,
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryPage(workSpaceId, token),
                HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的页面附件")
    @GetMapping(value = "/page_attachment")
    public ResponseEntity<List<PageAttachmentDTO>> queryPageAttachment(
            @ApiParam(value = "页面ID", required = true)
            @RequestParam("page_id") Long pageId,
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryPageAttachment(pageId, token),
                HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的文章目录")
    @GetMapping(value = "/{id}/toc")
    public ResponseEntity<String> pageToc(
            @ApiParam(value = "页面ID", required = true)
            @PathVariable(name = "id") Long pageId,
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.pageToc(pageId, token), HttpStatus.OK);
    }

    @ResponseBody
    @Permission(permissionPublic = true)
    @ApiOperation("分享链接的文章导出为pdf")
    @GetMapping(value = "/export_pdf")
    public void exportMd2Pdf(@ApiParam(value = "页面id", required = true)
                             @RequestParam Long pageId,
                             @ApiParam(value = "分享链接token", required = true)
                             @RequestParam("token") String token,
                             HttpServletResponse response) {
        workSpaceShareService.exportMd2Pdf(pageId, token, response);
    }
}
