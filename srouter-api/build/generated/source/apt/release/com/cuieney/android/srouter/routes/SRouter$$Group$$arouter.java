package com.cuieney.android.srouter.routes;

import com.cuieney.srouter_annotation.enums.RouteType;
import com.cuieney.srouter_annotation.model.RouteMeta;
import com.cuieney.srouter_api.core.AutowiredServiceImpl;
import com.cuieney.srouter_api.core.InterceptorServiceImpl;
import com.cuieney.srouter_api.facade.template.IRouteGroup;
import java.lang.Override;
import java.lang.String;
import java.util.Map;

/**
 * DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY SROUTER. */
public class SRouter$$Group$$arouter implements IRouteGroup {
  @Override
  public void loadInto(Map<String, RouteMeta> atlas) {
    atlas.put("/arouter/service/autowired", RouteMeta.build(RouteType.PROVIDER, AutowiredServiceImpl.class, "/arouter/service/autowired", "arouter", null, -1, -2147483648));
    atlas.put("/arouter/service/interceptor", RouteMeta.build(RouteType.PROVIDER, InterceptorServiceImpl.class, "/arouter/service/interceptor", "arouter", null, -1, -2147483648));
  }
}
