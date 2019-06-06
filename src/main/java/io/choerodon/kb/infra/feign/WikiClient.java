package io.choerodon.kb.infra.feign;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import io.choerodon.kb.infra.dataobject.MigrationDO;

/**
 * Created by Zenger on 2019/5/31.
 */
public interface WikiClient {

    //获取迁移的组织层数据
    @Headers({"Content-Type:application/json;charset=UTF-8"})
    @POST("rest/v1/page/migration")
    Call<ResponseBody> getWikiPageMigration(
            @Header("username") String username,
            @Body MigrationDO migrationDO
    );

    //获取页面迁移附件的信息
    @Headers({"Content-Type:application/json;charset=UTF-8"})
    @GET("rest/v1/page/attachment")
    Call<ResponseBody> getWikiPageAttachment(
            @Header("username") String username,
            @Query("data") String data
    );
}
