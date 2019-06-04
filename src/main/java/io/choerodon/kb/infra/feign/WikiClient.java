package io.choerodon.kb.infra.feign;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by Zenger on 2019/5/31.
 */
public interface WikiClient {

    //获取迁移的组织层数据
    @Headers({"Content-Type:application/json;charset=UTF-8"})
    @GET("rest/v1/organization/page")
    Call<ResponseBody> getWikiOrganizationPage(
            @Header("username") String username,
            @Query("data") String data
    );
}
