package com.cuieney.srouter_api.facade.template;


import com.cuieney.srouter_api.facade.Postcard;
import com.cuieney.srouter_api.facade.callback.InterceptorCallback;

/**
 * Used for inject custom logic when navigation.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 13:56
 */
public interface IInterceptor extends IProvider {

    /**
     * The operation of this interceptor.
     *
     * @param postcard meta
     * @param callback cb
     */
    void process(Postcard postcard, InterceptorCallback callback);
}
