package io.choerodon.kb.infra.common.aspect;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.kb.repository.*;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.annotation.DataLog;
import io.choerodon.kb.infra.utils.TypeUtil;
import io.choerodon.kb.infra.dto.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * Created by Zenger on 2019/5/16.
 */
@Aspect
@Component
@Transactional(rollbackFor = Exception.class)
public class DataLogAspect {

    private static final String ERROR_UPDATE = "error.LogDataAspect.update";
    private static final String ERROR_METHOD_EXECUTE = "error.dataLog.methodExecute";

    @Autowired
    private PageLogRepository pageLogRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageVersionRepository pageVersionRepository;

    @Autowired
    private PageCommentRepository pageCommentRepository;

    @Autowired
    private PageAttachmentRepository pageAttachmentRepository;

    @Autowired
    private WorkSpacePageRepository workSpacePageRepository;

    /**
     * 定义拦截规则：拦截Spring管理的后缀为RepositoryImpl的bean中带有@DataLog注解的方法。
     */
    @Pointcut("bean(*RepositoryImpl) && @annotation(io.choerodon.kb.infra.common.annotation.DataLog)")
    public void updateMethodPointcut() {
        throw new UnsupportedOperationException();
    }

    @Around("updateMethodPointcut()")
    public Object interceptor(ProceedingJoinPoint pjp) {
        Object result = null;
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        //获取被拦截的方法
        Method method = signature.getMethod();
        DataLog dataLog = method.getAnnotation(DataLog.class);
        //获取被拦截的方法名
        Object[] args = pjp.getArgs();
        if (dataLog != null && args != null) {
            if (dataLog.single()) {
                switch (dataLog.type()) {
                    case BaseStage.PAGE_CREATE:
                        result = handleCreatePageDataLog(pjp);
                        break;
                    case BaseStage.PAGE_UPDATE:
                        handleUpdatePageDataLog(args);
                        break;
                    case BaseStage.COMMENT_CREATE:
                        result = handleCreateCommentDataLog(args, pjp);
                        break;
                    case BaseStage.COMMENT_UPDATE:
                        handleUpdateCommentDataLog(args);
                        break;
                    case BaseStage.COMMENT_DELETE:
                        handleDeleteCommentDataLog(args);
                        break;
                    case BaseStage.ATTACHMENT_CREATE:
                        result = handleCreateAttachmentDataLog(args, pjp);
                        break;
                    case BaseStage.ATTACHMENT_DELETE:
                        handleUpdateAttachmentDataLog(args);
                        break;
                    default:
                        break;
                }
            }
        } else {
            throw new CommonException(ERROR_UPDATE);
        }
        try {
            // 一切正常的情况下，继续执行被拦截的方法
            if (result == null) {
                result = pjp.proceed();
            }
        } catch (Throwable e) {
            throw new CommonException(ERROR_METHOD_EXECUTE, e);
        }
        return result;
    }

    private void handleUpdateAttachmentDataLog(Object[] args) {
        Long id = null;
        for (Object arg : args) {
            if (arg instanceof Long) {
                id = (Long) arg;
            }
        }
        if (id != null) {
            PageAttachmentDTO pageAttachmentDTO = pageAttachmentRepository.selectById(id);
            if (pageAttachmentDTO != null) {
                createDataLog(pageAttachmentDTO.getPageId(),
                        BaseStage.DELETE_OPERATION,
                        BaseStage.ATTACHMENT,
                        pageAttachmentDTO.getName(),
                        null,
                        TypeUtil.objToString(pageAttachmentDTO.getId()),
                        null);
            }
        }
    }

    private Object handleCreateAttachmentDataLog(Object[] args, ProceedingJoinPoint pjp) {
        PageAttachmentDTO pageAttachmentDTO = null;
        Object result = null;
        for (Object arg : args) {
            if (arg instanceof PageAttachmentDTO) {
                pageAttachmentDTO = (PageAttachmentDTO) arg;
            }
        }
        if (pageAttachmentDTO != null) {
            try {
                result = pjp.proceed();
                pageAttachmentDTO = (PageAttachmentDTO) result;
                createDataLog(pageAttachmentDTO.getPageId(),
                        BaseStage.CREATE_OPERATION,
                        BaseStage.ATTACHMENT,
                        null,
                        pageAttachmentDTO.getName(),
                        null,
                        pageAttachmentDTO.getId().toString());
            } catch (Throwable throwable) {
                throw new CommonException(ERROR_METHOD_EXECUTE, throwable);
            }
        }
        return result;
    }

    private void handleDeleteCommentDataLog(Object[] args) {
        Long id = null;
        for (Object arg : args) {
            if (arg instanceof Long) {
                id = (Long) arg;
            }
        }
        PageCommentDTO pageCommentDTO = pageCommentRepository.selectById(id);
        if (pageCommentDTO != null) {
            createDataLog(pageCommentDTO.getPageId(),
                    BaseStage.DELETE_OPERATION,
                    BaseStage.COMMENT,
                    pageCommentDTO.getComment(),
                    null,
                    TypeUtil.objToString(pageCommentDTO.getId()),
                    null);
        }
    }

    private void handleUpdateCommentDataLog(Object[] args) {
        PageCommentDTO pageCommentDTO = null;
        for (Object arg : args) {
            if (arg instanceof PageCommentDTO) {
                pageCommentDTO = (PageCommentDTO) arg;
            }
        }
        if (pageCommentDTO != null) {
            PageCommentDTO oldPageComment = pageCommentRepository.selectById(pageCommentDTO.getId());
            createDataLog(pageCommentDTO.getPageId(),
                    BaseStage.UPDATE_OPERATION,
                    BaseStage.COMMENT,
                    oldPageComment.getComment(),
                    pageCommentDTO.getComment(),
                    TypeUtil.objToString(pageCommentDTO.getId()),
                    TypeUtil.objToString(pageCommentDTO.getId()));
        }
    }

    private Object handleCreateCommentDataLog(Object[] args, ProceedingJoinPoint pjp) {
        PageCommentDTO pageCommentDTO = null;
        Object result = null;
        for (Object arg : args) {
            if (arg instanceof PageCommentDTO) {
                pageCommentDTO = (PageCommentDTO) arg;
            }
        }
        if (pageCommentDTO != null) {
            try {
                result = pjp.proceed();
                pageCommentDTO = (PageCommentDTO) result;
                createDataLog(pageCommentDTO.getPageId(),
                        BaseStage.CREATE_OPERATION,
                        BaseStage.COMMENT,
                        null,
                        pageCommentDTO.getComment(),
                        null,
                        pageCommentDTO.getId().toString());
            } catch (Throwable e) {
                throw new CommonException(ERROR_METHOD_EXECUTE, e);
            }
        }
        return result;
    }

    private void handleUpdatePageDataLog(Object[] args) {
        PageDTO pageDTO = null;
        Boolean flag = false;
        for (Object arg : args) {
            if (arg instanceof PageDTO) {
                pageDTO = (PageDTO) arg;
            }
            if (arg instanceof Boolean) {
                flag = (Boolean) arg;
            }
        }
        if (pageDTO != null) {
            PageDTO page = pageRepository.selectById(pageDTO.getId());
            Long oldVersionId = page.getLatestVersionId();
            PageVersionDTO pageVersionDTO = pageVersionRepository.queryByVersionId(oldVersionId, pageDTO.getId());
            Long newVersionId = oldVersionId;
            PageVersionDTO newPageVersionDTO = null;
            if (!pageDTO.getLatestVersionId().equals(oldVersionId)) {
                newVersionId = pageDTO.getLatestVersionId();
                newPageVersionDTO = pageVersionRepository.queryByVersionId(newVersionId, pageDTO.getId());
            }
            if (flag) {
                createDataLog(pageDTO.getId(),
                        BaseStage.UPDATE_OPERATION,
                        BaseStage.PAGE,
                        pageVersionDTO.getName(),
                        newPageVersionDTO == null ? pageVersionDTO.getName() : newPageVersionDTO.getName(),
                        oldVersionId.toString(),
                        newVersionId.toString());
            }
        }
    }

    private Object handleCreatePageDataLog(ProceedingJoinPoint pjp) {
        Object result;
        try {
            result = pjp.proceed();
            PageDTO pageDTO = (PageDTO) result;
            if (pageDTO.getId() != null) {
                createDataLog(pageDTO.getId(),
                        BaseStage.CREATE_OPERATION,
                        BaseStage.PAGE,
                        null,
                        pageDTO.getTitle(),
                        null,
                        pageDTO.getId().toString());
            }
        } catch (Throwable e) {
            throw new CommonException(ERROR_METHOD_EXECUTE, e);
        }
        return result;
    }

    private void createDataLog(Long pageId, String operation, String field, String oldString,
                               String newString, String oldValue, String newValue) {
        PageLogDTO pageLogDTO = new PageLogDTO();
        pageLogDTO.setPageId(pageId);
        pageLogDTO.setOperation(operation);
        pageLogDTO.setField(field);
        pageLogDTO.setOldString(oldString);
        pageLogDTO.setNewString(newString);
        pageLogDTO.setOldValue(oldValue);
        pageLogDTO.setNewValue(newValue);
        pageLogRepository.insert(pageLogDTO);
    }
}
