package io.choerodon.kb.api.controller.v1;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.kb.api.dao.PageAttachmentDTO;
import io.choerodon.kb.api.dao.PageCreateAttachmentDTO;
import io.choerodon.kb.app.service.PageAttachmentService;
import io.choerodon.kb.infra.common.BaseStage;

/**
 * Created by Zenger on 2019/4/30.
 */
@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/page_attachment")
public class PageAttachmentOrganizationController {

    private PageAttachmentService pageAttachmentService;

    public PageAttachmentOrganizationController(PageAttachmentService pageAttachmentService) {
        this.pageAttachmentService = pageAttachmentService;
    }

    /**
     * 页面上传附件
     *
     * @param organizationId          组织id
     * @param pageId                  页面id
     * @param pageCreateAttachmentDTO 附件信息
     * @param request
     * @return List<PageAttachmentDTO>
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    BaseStage.ORGANIZATION_MEMBER})
    @ApiOperation("页面上传附件")
    @PostMapping
    public ResponseEntity<List<PageAttachmentDTO>> create(@ApiParam(value = "组织ID", required = true)
                                                          @PathVariable(value = "organization_id") Long organizationId,
                                                          @ApiParam(value = "页面ID", required = true)
                                                          @RequestParam Long pageId,
                                                          @ApiParam(value = "附件信息", required = true)
                                                          @RequestBody @Valid PageCreateAttachmentDTO pageCreateAttachmentDTO,
                                                          HttpServletRequest request) {
        return new ResponseEntity<>(pageAttachmentService.create(pageId, pageCreateAttachmentDTO, request), HttpStatus.CREATED);
    }

    /**
     * 页面删除附件
     *
     * @param organizationId 组织id
     * @param id             附件id
     * @return ResponseEntity
     */
    @Permission(type = ResourceType.ORGANIZATION,
            roles = {InitRoleCode.ORGANIZATION_ADMINISTRATOR,
                    BaseStage.ORGANIZATION_MEMBER})
    @ApiOperation("页面删除附件")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@ApiParam(value = "组织ID", required = true)
                                 @PathVariable(value = "organization_id") Long organizationId,
                                 @ApiParam(value = "附件ID", required = true)
                                 @PathVariable Long id) {
        pageAttachmentService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
