package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProduce;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder holder;
    @Autowired
    private EventProduce eventProduce;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布帖子
     * @param title
     * @param content
     * @return
     */
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = holder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"你还没有登录！");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.insertDiscussPost(discussPost);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProduce.fireEvent(event);

        //计算帖子分数
        String rediskey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(rediskey,discussPost.getId());
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    /**
     * 查询帖子详情
     * @param disscussPostId
     * @param model
     * @return
     */
    @RequestMapping(path = "/detail/{disscussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("disscussPostId") int disscussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussPostByID(disscussPostId);
        model.addAttribute("discussPost",discussPost);
        //作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        //查看点赞数量
        Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,disscussPostId);
        int likeStatus= holder.getUser()== null ? 0 : likeService.findEntityLikeStatus(holder.getUser().getId(),ENTITY_TYPE_POST,disscussPostId);
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("likeStatus",likeStatus);
        //评论信息分页
        page.setLimit(5);
        page.setPath("/discuss/detail/"+disscussPostId);
        page.setRows(discussPost.getCommentCount());

        //评论：评论帖子的评论
        //回复:回复用户的评论
        //评论列表
        List<Comment> commentList = commentService.selectCommentsByEtity(
                ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
        //评论VO列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList!=null){
            for(Comment comment : commentList){
                Map<String,Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                //点赞状态
                likeStatus= holder.getUser()== null ? 0 :  likeService.findEntityLikeStatus(holder.getUser().getId(),ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                commentVo.put("likeStatus",likeStatus);
                //回复列表
                List<Comment> replyList = commentService.selectCommentsByEtity(
                        ENTITY_TYPE_COMMENT,comment.getId(),0,Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if(replyList!=null){
                    for(Comment reply: replyList){
                        Map<String,Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId()==0?null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        //点赞状态
                        likeStatus= holder.getUser()== null ? 0 :  likeService.findEntityLikeStatus(holder.getUser().getId(),ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        replyVo.put("likeStatus",likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }

    /**
     * 帖子置顶
     */
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);

        //触发事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProduce.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
    /**
     * 帖子加精
     */
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);

        //触发事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProduce.fireEvent(event);
        //计算帖子分数
        String rediskey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(rediskey,id);

        return CommunityUtil.getJSONString(0);
    }
    /**
     * 帖子删除
     */
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);

        //触发事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(holder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProduce.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }
}
