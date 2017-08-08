package com.cuieney.srouter_api.launcher;

import android.app.Application;
import android.content.Context;
import android.net.Uri;


import com.cuieney.srouter_api.exception.InitException;
import com.cuieney.srouter_api.facade.Postcard;
import com.cuieney.srouter_api.facade.callback.NavigationCallback;
import com.cuieney.srouter_api.facade.template.ILogger;
import com.cuieney.srouter_api.utils.Consts;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * SRouter facade
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/16 14:36
 */
public final class SRouter {
    // Key of raw uri
    public static final String RAW_URI = "NTeRQWvye18AkPd6G";
    public static final String AUTO_INJECT = "wmHzgD4lOj5o4241";

    private volatile static SRouter instance = null;
    private volatile static boolean hasInit = false;
    public static ILogger logger;

    private SRouter() {
    }

    /**
     * Init, it must be call before used router.
     */
    public static void init(Application application) {
        if (!hasInit) {
            logger = _SRouter.logger;
            _SRouter.logger.info(Consts.TAG, "SRouter init start.");
            hasInit = _SRouter.init(application);

            if (hasInit) {
                _SRouter.afterInit();
            }

            _SRouter.logger.info(Consts.TAG, "SRouter init over.");
        }
    }

    /**
     * Get instance of router. A
     * All feature U use, will be starts here.
     */
    public static SRouter getInstance() {
        if (!hasInit) {
            throw new InitException("SRouter::Init::Invoke init(context) first!");
        } else {
            if (instance == null) {
                synchronized (SRouter.class) {
                    if (instance == null) {
                        instance = new SRouter();
                    }
                }
            }
            return instance;
        }
    }

    public static synchronized void openDebug() {
        _SRouter.openDebug();
    }

    public static boolean debuggable() {
        return _SRouter.debuggable();
    }

    public static synchronized void openLog() {
        _SRouter.openLog();
    }

    public static synchronized void printStackTrace() {
        _SRouter.printStackTrace();
    }

    public static synchronized void setExecutor(ThreadPoolExecutor tpe) {
        _SRouter.setExecutor(tpe);
    }

    public synchronized void destroy() {
        _SRouter.destroy();
        hasInit = false;
    }

    /**
     * The interface is not stable enough, use 'SRouter.inject();';
     */
    @Deprecated
    public static synchronized void enableAutoInject() {
        _SRouter.enableAutoInject();
    }

    public static boolean canAutoInject() {
        return _SRouter.canAutoInject();
    }

    /**
     * The interface is not stable enough, use 'SRouter.inject();';
     */
    @Deprecated
    public static void attachBaseContext() {
        _SRouter.attachBaseContext();
    }

    public static synchronized void monitorMode() {
        _SRouter.monitorMode();
    }

    public static boolean isMonitorMode() {
        return _SRouter.isMonitorMode();
    }

    public static void setLogger(ILogger userLogger) {
        _SRouter.setLogger(userLogger);
    }

    /**
     * Inject params and services.
     */
    public void inject(Object thiz) {
        _SRouter.inject(thiz);
    }

    /**
     * Build the roadmap, draw a postcard.
     *
     * @param path Where you go.
     */
    public Postcard build(String path) {
        return _SRouter.getInstance().build(path);
    }

    /**
     * Build the roadmap, draw a postcard.
     *
     * @param path  Where you go.
     * @param group The group of path.
     */
    @Deprecated
    public Postcard build(String path, String group) {
        return _SRouter.getInstance().build(path, group);
    }

    /**
     * Build the roadmap, draw a postcard.
     *
     * @param url the path
     */
    public Postcard build(Uri url) {
        return _SRouter.getInstance().build(url);
    }

    /**
     * Launch the navigation by type
     *
     * @param service interface of service
     * @param <T>     return type
     * @return instance of service
     */
    public <T> T navigation(Class<? extends T> service) {
        return _SRouter.getInstance().navigation(service);
    }

    /**
     * Launch the navigation.
     *
     * @param mContext    .
     * @param postcard    .
     * @param requestCode Set for startActivityForResult
     * @param callback    cb
     */
    public Object navigation(Context mContext, Postcard postcard, int requestCode, NavigationCallback callback) {
        return _SRouter.getInstance().navigation(mContext, postcard, requestCode, callback);
    }
}
