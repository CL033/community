package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.protocol.types.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder holder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headBucketUrl;



    /**
     * 访问个人设置页面
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){
        //上传文件名字
        String fileName = CommunityUtil.generateUUID();
        //设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //生成上传凭证
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);
        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "/site/setting";
    }

    //更新头像路径
    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空");
        }
        String url = headBucketUrl+"/"+fileName;
        userService.updateHeader(holder.getUser().getId(),url);
        return CommunityUtil.getJSONString(0);
    }

    //废弃
    /**
     * 修改用户头像
     * @param headerImg
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImg, Model model){
        if(headerImg==null){
            model.addAttribute("HeaderMsg","您还没有选择图片！");
            return "/site/setting";
        }
        String fielName = headerImg.getOriginalFilename();
        String suffix = fielName.substring(fielName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("HeaderMsg","您选择的文件格式不正确！");
            return "/site/setting";
        }
        //生成文件名字
        fielName = CommunityUtil.generateUUID()+suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath+"/"+fielName);
        try {
            headerImg.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: "+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常",e);
        }
        //更新当前用户的头像路径（Web访问路径）
        //http://localhost:8080/community/user/header/xxx.png
        User user = holder.getUser();
        String headUrl = domain+contextPath+"/user/header/"+fielName;
        userService.updateHeader(user.getId(),headUrl);
        return "redirect:/index";
    }

    //废弃
    /**
     * 获取头像图片
     */
    @RequestMapping(value = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //服务器的存放位置
        fileName = uploadPath+"/"+fileName;
        //获得文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //通过字节流响应图片
        response.setContentType("image/"+suffix);
        try( FileInputStream fis = new FileInputStream(fileName);
             OutputStream os = response.getOutputStream())
        {
            byte[] buffer = new byte[1024];
            int b=0;
            while ((b=fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
           logger.error("读取文件失败："+e.getMessage());
        }
    }

    /**
     * 修改用户密码
     * @param oldPwd
     * @param newPwd
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(value = "/updatePw",method = RequestMethod.POST)
    public String updatePassword(@RequestParam(name = "oldPwd",required = true)String oldPwd,
                              @RequestParam(name = "newPwd",required = true)String newPwd,
                              Model  model){
        if(StringUtils.isBlank(oldPwd)){
            model.addAttribute("passwordMsg","请输入原始密码！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPwd)){
            model.addAttribute("passwordMsg","请输入新密码！");
            return "/site/setting";
        }
        User user = holder.getUser();
        if(!user.getPassword().equals(CommunityUtil.md5(oldPwd+user.getSalt()))){
            model.addAttribute("passwordMsg","原密码输入错误！");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(),CommunityUtil.md5(newPwd+user.getSalt()));
        return "redirect:/login";

    }

    /**
     * 获取个人主页
     * @param userId
     * @param model
     * @return
     */
    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId,Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(userId);
        //点赞数量
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasfollowed = false;
        if(holder.getUser()!=null){
            hasfollowed = followService.hasFollowed(holder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasfollowed",hasfollowed);
        return "/site/profile";
    }
}
