package com.nowcoder.community.controller;

import com.nowcoder.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @RequestMapping("/hello")
    @ResponseBody
    public String saveHello(){
        return "hello";
    }

    //GET请求

    // /students?current=1&limit=20
    @RequestMapping(path = "/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@RequestParam(name = "current",required = false,defaultValue = "1") int current,
                              @RequestParam(name = "limit",required = false,defaultValue = "1") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // /students/123
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id ) {
        System.out.println(id);

        return "a students";
    }


    /**
     * Cookie测试
     */
    @RequestMapping(path = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie的生效路径
        cookie.setPath("/community/alpha");
        //设置cookie的生效时间
        cookie.setMaxAge(60*10);
        //发送cookie
        response.addCookie(cookie);
        return "set Cookie";
    }

    @RequestMapping(path = "/cookie/get",method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String cookie){
        System.out.println(cookie);
        return "get cookie";
    }

}
