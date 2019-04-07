package com.example.screenalike;

import android.app.Application;
import android.content.Context;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.annotation.Nullable;
import screenAlike.AppData;
import screenAlike.HttpServer;
import screenAlike.ImageGenerator;

public class ScreenAlike extends Application {
    private static ScreenAlike sAppInstance;
    private MediaProjection.Callback mProjectionCallback;
    private AppData mAppData;
    private ImageGenerator mImageGenerator;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;
    private HttpServer mHttpServer;
    @Override
    public void onCreate() {
        super.onCreate();
        sAppInstance = this;
        mAppData = new AppData(this);
        getAppData().initIndexHtmlPage(this);
        mHttpServer = new HttpServer();
        mImageGenerator = new ImageGenerator();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mHttpServer.start();
     }



    public void onDestroy() {
        mHttpServer.stop(null);
    }

    public static AppData getAppData() {
        return sAppInstance.mAppData;
    }

    public static void setMediaProjection(final MediaProjection mediaProjection) {
        sAppInstance.mMediaProjection = mediaProjection;
    }
    @Nullable
    public static MediaProjection getMediaProjection() {
        return sAppInstance == null ? null : sAppInstance.mMediaProjection;
    }

    @Nullable
    public static ImageGenerator getImageGenerator() {
        return sAppInstance == null ? null : sAppInstance.mImageGenerator;
    }
    @Nullable
    public static MediaProjectionManager getProjectionManager() {
        return sAppInstance == null ? null : sAppInstance.mMediaProjectionManager;
    }
}
