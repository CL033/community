package com.nowcoder.community.config;

import com.nowcoder.community.controller.Interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sun.dc.pr.PRError;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;
    @Autowired
    private MessageInterceptor messageInterceptor;
    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.jpg","/**/*.png","/**/*.jepg")
                .addPathPatterns("/register","/login");
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.jpg","/**/*.png","/**/*.jepg");
//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.jpg","/**/*.png","/**/*.jepg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.jpg","/**/*.png","/**/*.jepg");
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.jpg","/**/*.png","/**/*.jepg");
    }
}
