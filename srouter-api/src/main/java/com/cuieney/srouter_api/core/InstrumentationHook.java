package com.cuieney.srouter_api.core;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;


import com.cuieney.srouter_api.launcher.SRouter;
import com.cuieney.srouter_api.utils.Consts;
import com.cuieney.srouter_api.utils.TextUtils;

import java.lang.reflect.Field;


/**
 * Use ARouter.getInstance().inject(this) now!
 *
 * Hook the instrumentation, inject values for activity's field.
 * Support normal activity only, not contain unit test.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 2016/11/24 16:42
 */
@Deprecated
public class InstrumentationHook extends Instrumentation {
    /**
     * Hook the instrumentation's newActivity, inject
     * <p>
     * Perform instantiation of the process's {@link Activity} object.  The
     * default implementation provides the normal system behavior.
     *
     * @param cl        The ClassLoader with which to instantiate the object.
     * @param className The name of the class implementing the Activity
     *                  object.
     * @param intent    The Intent object that specified the activity class being
     *                  instantiated.
     * @return The newly instantiated Activity object.
     */
    public Activity newActivity(ClassLoader cl, String className,
                                Intent intent)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {

//        return (Activity)cl.loadClass(className).newInstance();

        Class<?> targetActivity = cl.loadClass(className);
        Object instanceOfTarget = targetActivity.newInstance();

        if (SRouter.canAutoInject()) {
            String[] autoInjectParams = intent.getStringArrayExtra(SRouter.AUTO_INJECT);
            if (null != autoInjectParams && autoInjectParams.length > 0) {
                for (String paramsName : autoInjectParams) {
                    Object value = intent.getExtras().get(TextUtils.getLeft(paramsName));
                    if (null != value) {
                        try {
                            Field injectField = targetActivity.getDeclaredField(TextUtils.getLeft(paramsName));
                            injectField.setAccessible(true);
                            injectField.set(instanceOfTarget, value);
                        } catch (Exception e) {
                            SRouter.logger.error(Consts.TAG, "Inject values for activity error! [" + e.getMessage() + "]");
                        }
                    }
                }
            }
        }

        return (Activity) instanceOfTarget;
    }
}
