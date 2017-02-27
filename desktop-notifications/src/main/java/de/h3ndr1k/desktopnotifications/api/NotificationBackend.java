package de.h3ndr1k.desktopnotifications.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by hendrik on 23.02.17.
 */

public interface NotificationBackend {
    @POST("v1/notify/{code}")
    Call<StatusResponse> notify(@Path("code") String code, @Body DNotification dNotification);
}
