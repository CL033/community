package com.nowcoder.community.controller.Interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从Cookie种获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket!=null) {
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查ticket
            if(loginTicket!=null&&loginTicket.getStatus()==0&&loginTicket.getExpired().after(new Date())) {
                //根据凭证寻找用户
                User user = userService.findUserById(loginTicket.getUserId());
                if(user!=null){
                    //在本次请求中持有用户
                    hostHolder.setUser(user);
                    //构建用户认证的结果，并存入SecurityContext中，以便后续Security授权使用
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            user,user.getPassword(),userService.getAuthorities(user.getId()));
                    SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
                }
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null&&modelAndView!=null) {
            modelAndView.addObject("LoginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
       hostHolder.clear();
//       SecurityContextHolder.clearContext();
    }
}
