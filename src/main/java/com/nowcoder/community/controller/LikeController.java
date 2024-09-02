package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProduce;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.aspectj.weaver.AjAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder holder;

    @Autowired
    private EventProduce eventProduce;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user = holder.getUser();
        //点赞
        likeService.like(user.getId(), entityType,entityId,entityUserId);

        //获取点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //获取点赞状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType ,entityId);
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        //触发事件
        Event event = new Event()
                .setTopic(TOPIC_LIKE)
                .setUserId(holder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityUserId)
                .setData("postId",postId);
        eventProduce.fireEvent(event);
        if(entityType==ENTITY_TYPE_POST){
            //计算帖子分数
            String rediskey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(rediskey,postId);
        }
        return CommunityUtil.getJSONString(0,null,map);
    }
}
