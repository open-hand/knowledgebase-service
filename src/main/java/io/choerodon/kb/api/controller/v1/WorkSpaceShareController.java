package io.choerodon.kb.api.controller.v1;

import io.choerodon.kb.api.vo.PageAttachmentVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.choerodon.kb.infra.enums.ShareType;
import io.choerodon.kb.infra.utils.EncrtpyUtil;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author: CaiShuangLian
 * @Date: 2022/01/12
 * @Description:覆盖知识库分享接口
 */
@RestController
@RequestMapping({"/v1/work_space_share"})
public class WorkSpaceShareController {

    @Autowired
    private WorkSpaceShareService workSpaceShareService;

    @Autowired
    private IEncryptionService encryptionService;

    public WorkSpaceShareController() {

    }


    @Permission(permissionPublic = true)
    @ApiOperation("查询分享链接的树形结构")
    @GetMapping({"/tree"})
    public ResponseEntity<Map<String, Object>> queryTree(@ApiParam(value = "分享链接token", required = true)
                                                         @RequestParam("token") String token) {
        Map<String, Object> map = this.workSpaceShareService.queryTree(token);
        String shareType = (String) map.get("shareType");
        map.put("shareType", shareType);
        if (shareType.equals(ShareType.DISABLE)) {
            return new ResponseEntity(map, HttpStatus.OK);
        }
        map.put("rootId", this.encryptionService.encrypt(map.get("rootId").toString(), ""));
        return new ResponseEntity(map, HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation("查询分享链接的页面信息")
    @GetMapping({"/page"})
    public ResponseEntity<Map> queryPage(@ApiParam(value = "工作空间ID", required = true)
                                         @RequestParam("work_space_id")
                                         @Encrypt(ignoreUserConflict = true) Long workSpaceId,
                                         @ApiParam(value = "分享链接token", required = true)
                                         @RequestParam("token") String token) {
        WorkSpaceInfoVO infoVO = this.workSpaceShareService.queryWorkSpaceInfo(workSpaceId, token);
        infoVO.setRoute(EncrtpyUtil.entryRoute(infoVO.getRoute(), this.encryptionService));
        return new ResponseEntity(infoVO, HttpStatus.OK);
    }


    @Permission(permissionPublic = true)
    @ApiOperation("查询分享链接的页面附件")
    @GetMapping({"/page_attachment"})
    public ResponseEntity<List<PageAttachmentVO>> queryPageAttachment(@ApiParam(value = "页面ID", required = true)
                                                                      @RequestParam("page_id")
                                                                      @Encrypt(ignoreUserConflict = true) Long pageId,
                                                                      @ApiParam(value = "分享链接token", required = true)
                                                                      @RequestParam("token") String token) {
        return new ResponseEntity(this.workSpaceShareService.queryPageAttachment(pageId, token), HttpStatus.OK);
    }

    @ResponseBody
    @Permission(permissionPublic = true)
    @ApiOperation("分享链接的文章导出为pdf")
    @GetMapping({"/export_pdf"})
    public void exportMd2Pdf(@ApiParam(value = "页面id", required = true)
                             @RequestParam @Encrypt(ignoreUserConflict = true) Long pageId,
                             @ApiParam(value = "分享链接token", required = true)
                             @RequestParam("token") String token, HttpServletResponse response) {
        this.workSpaceShareService.exportMd2Pdf(pageId, token, response);
    }
}

