/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.model.images;

import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.utils.ExpandFileClient;
import io.choerodon.kb.infra.utils.SpringBeanUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The DefaultConversionImageHandler is a pure File-based ImageHandler.
 */
public class FileConversionImageHandler extends AbstractConversionImageHandler {

    /**
     * Creates a DefaultConversionImageHandler.
     *
     * @param imageDirPath
     * @param includeUUID
     */
    public FileConversionImageHandler(String imageDirPath, boolean includeUUID) {
        super(imageDirPath, includeUUID);
    }

    @Override
    protected String createStoredImage(BinaryPart binaryPart, byte[] bytes) throws Docx4JException {
        String filename = setupImageName(binaryPart);
        String uri = storeImage(binaryPart, bytes, filename);
        return uri;
    }

    protected String storeImage(BinaryPart binaryPart, byte[] bytes, String filename) throws Docx4JException {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem("file", "text/plain", true, "file");
        try {
            OutputStream os = item.getOutputStream();
            os.write(bytes);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MultipartFile file = new CommonsMultipartFile(item);
        ExpandFileClient expandFileClient = SpringBeanUtil.getBean(ExpandFileClient.class);
        return expandFileClient.uploadFile(0L, BaseStage.BACKETNAME, null,filename, file);
    }
}
