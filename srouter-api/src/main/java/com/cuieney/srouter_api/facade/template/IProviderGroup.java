package com.cuieney.srouter_api.facade.template;


import com.cuieney.srouter_annotation.model.RouteMeta;

import java.util.Map;

/**
 * Template of provider group.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/08/30 12:42
 */
public interface IProviderGroup {
    /**
     * Load providers map to input
     *
     * @param providers input
     */
    void loadInto(Map<String, RouteMeta> providers);
}