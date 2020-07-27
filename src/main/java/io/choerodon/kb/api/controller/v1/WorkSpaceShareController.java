package io.choerodon.kb.api.controller.v1;

import io.choerodon.kb.api.vo.WorkSpaceTreeVO;
import io.choerodon.kb.app.service.impl.WorkSpaceServiceImpl;
import io.choerodon.kb.infra.utils.EncrtpyUtil;
import io.choerodon.swagger.annotation.Permission;
import io.choerodon.kb.api.vo.PageAttachmentVO;
import io.choerodon.kb.api.vo.WorkSpaceInfoVO;
import io.choerodon.kb.app.service.WorkSpaceShareService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.tuple.Pair;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Zenger on 2019/6/11.
 */
@RestController
@RequestMapping(value = "/v1/work_space_share")
public class WorkSpaceShareController {

    @Autowired
    private WorkSpaceShareService workSpaceShareService;
    @Autowired
    private IEncryptionService encryptionService;

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的树形结构")
    @GetMapping(value = "/tree")
    public ResponseEntity<Map<String, Object>> queryTree(
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        Map<String, Object> map = workSpaceShareService.queryTree(token);
        Map<String, WorkSpaceTreeVO> wsMap = Optional.of(map.get(WorkSpaceServiceImpl.ITEMS))
                .map(type -> (Map<Long, WorkSpaceTreeVO>)type)
                .map(ws -> ws.entrySet().stream()
                        .map(entry -> EncrtpyUtil.encryptWsMap(entry, encryptionService))
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue))).orElse(null);
        map.put(WorkSpaceServiceImpl.ITEMS, wsMap);
        map.put(WorkSpaceServiceImpl.ROOT_ID,
                encryptionService.encrypt(map.get(WorkSpaceServiceImpl.ROOT_ID).toString(), EncrtpyUtil.BLANK_KEY));
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的页面信息")
    @GetMapping(value = "/page")
    public ResponseEntity<WorkSpaceInfoVO> queryPage(
            @ApiParam(value = "工作空间ID", required = true)
            @RequestParam("work_space_id") @Encrypt Long workSpaceId,
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        WorkSpaceInfoVO infoVO = workSpaceShareService.queryWorkSpaceInfo(workSpaceId, token);
        infoVO.setRoute(EncrtpyUtil.entryRoute(infoVO.getRoute(), encryptionService));
        return new ResponseEntity<>(infoVO, HttpStatus.OK);
    }

    @Permission(permissionPublic = true)
    @ApiOperation(value = "查询分享链接的页面附件")
    @GetMapping(value = "/page_attachment")
    public ResponseEntity<List<PageAttachmentVO>> queryPageAttachment(
            @ApiParam(value = "页面ID", required = true)
            @RequestParam("page_id") @Encrypt Long pageId,
            @ApiParam(value = "分享链接token", required = true)
            @RequestParam("token") String token) {
        return new ResponseEntity<>(workSpaceShareService.queryPageAttachment(pageId, token),
                HttpStatus.OK);
    }

    @ResponseBody
    @Permission(permissionPublic = true)
    @ApiOperation("分享链接的文章导出为pdf")
    @GetMapping(value = "/export_pdf")
    public void exportMd2Pdf(@ApiParam(value = "页面id", required = true)
                             @RequestParam @Encrypt Long pageId,
                             @ApiParam(value = "分享链接token", required = true)
                             @RequestParam("token") String token,
                             HttpServletResponse response) {
        workSpaceShareService.exportMd2Pdf(pageId, token, response);
    }
}
