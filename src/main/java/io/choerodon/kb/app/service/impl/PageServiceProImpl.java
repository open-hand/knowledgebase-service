package io.choerodon.kb.app.service.impl;

import feign.FeignException;
import io.choerodon.kb.api.vo.*;
import io.choerodon.kb.infra.feign.IamFeignClient;
import io.choerodon.kb.infra.repository.PageRepository;
import io.choerodon.kb.infra.utils.PdfProUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;

/**
 * @author shinan.chen
 * @since 2019/7/17
 */
@Service
@Primary
@Transactional(rollbackFor = Exception.class)
public class PageServiceProImpl extends PageServiceImpl {

    @Autowired
    private IamFeignClient iamFeignClient;
    @Autowired
    private PageRepository pageRepository;

    @Override
    public void exportMd2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response) {
        PageInfoVO pageInfoVO = pageRepository.queryInfoById(organizationId, projectId, pageId);
        WatermarkVO waterMark = queryWaterMarkConfigFromIam(organizationId);
        PdfProUtil.markdown2Pdf(pageInfoVO.getTitle(), pageInfoVO.getContent(), response, waterMark);
    }

    private WatermarkVO queryWaterMarkConfigFromIam(Long organizationId) {
        WatermarkVO waterMark = null;
        boolean isOpenIam = false;
        try {
            ResponseEntity<WatermarkVO> responseEntity = iamFeignClient.getWaterMarkConfig(organizationId);
            waterMark = responseEntity.getBody();
        } catch (Exception e) {
            Throwable throwable =  ExceptionUtils.getRootCause(e);
            if (throwable != null && throwable instanceof FeignException) {
                FeignException feignException = (FeignException) throwable;
                if (HttpStatus.NOT_FOUND.value() == feignException.status()) {
                    isOpenIam = true;
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        }
        if (waterMark == null) {
            waterMark = new WatermarkVO();
        }
        boolean doWaterMark = !isOpenIam && Boolean.TRUE.equals(waterMark.getEnable());
        waterMark.setDoWaterMark(doWaterMark);
        return waterMark;
    }

}
