package com.nowcoder.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@RequestMapping("/message")
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder holder;

    /**
     * 私信列表
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(path = "/list" ,method = RequestMethod.GET)
    public String getConversations(Model model, Page page){
        User user = holder.getUser();
        //分页信息
        page.setPath("/message/list");
        page.setLimit(5);
        page.setRows(messageService.findConversationCount(user.getId()));

        //获取会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(),
                page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        for(Message message : conversationList){
            Map<String,Object> map =new HashMap<>();
            map.put("conversation",message);
            //每个会话的私信总数
            map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
            //每个会话的未读私信数量
            map.put("unreadCount",messageService.findLetterUnreadCount(
                    user.getId(),message.getConversationId()));
            int targetId = user.getId() == message.getFromId()? message.getToId() : message.getFromId();
            map.put("target",userService.findUserById(targetId));
            conversations.add(map);
        }
        model.addAttribute("conversations",conversations);

        //查询未读消息数量
        int count = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",count);
        //查询未读通知
        int noticerUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticerUnreadCount",noticerUnreadCount);
        return "/site/letter";
    }

    /**
     * 获取私信详情
     * @param conversationId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path="/list/detail/{conversationId}",method = RequestMethod.GET)
    public String getConversationDetail(@PathVariable("conversationId")String conversationId,Page page,Model model){

        //设置分页
        page.setLimit(5);
        page.setPath("/message/list/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        User user = holder.getUser();
        User fromUser = null;
        //获取私信详情
        List<Message> lettersList =  messageService.findLetters(conversationId,page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters = new ArrayList<>();
        if(lettersList!=null) {
            for (Message letter : lettersList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser",userService.findUserById(letter.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters",letters);
        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));
        List<Integer> ids = getLetterIds(lettersList);
        if(!ids.isEmpty()){
            messageService.updateLetterStatus(ids);
        }
        //修改已读状态

        return "/site/letter-detail";
    }
    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0= Integer.parseInt(ids[0]);
        int id1= Integer.parseInt(ids[1]);
        if(holder.getUser().getId()==id0){
            return userService.findUserById(id1);
        }else {
            return userService.findUserById(id0);
        }
    }

    /**
     * 发送私信
     * @param toName
     * @param content
     * @return
     */
    @RequestMapping(path = "/list/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName,String content){

        User target = userService.findUser(toName);
        if(target==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setFromId(holder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId()< message.getToId()){
            message.setConversationId(message.getFromId()+"_"+ message.getToId());
        }else {
            message.setConversationId(message.getToId()+"_"+ message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addLetter(message);
        return CommunityUtil.getJSONString(0);
    }

    //获取id列表
    private List<Integer> getLetterIds(List<Message> letterList){
      List<Integer> ids = new ArrayList<>();
      for(Message message : letterList){
          if(message.getToId()==holder.getUser().getId()&&message.getStatus()==0){
              ids.add(message.getId());
          }
      }
      return ids;
    }

    //获取系统通知
    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = holder.getUser();
        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String,Object> messageVO = new HashMap<>();
        messageVO.put("message",message);
        if(message!=null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("eneityId",data.get("eneityId"));
            messageVO.put("postId",data.get("postId"));

            //所有数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count",count);
            //所有数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread",unread);
        }
        model.addAttribute("commentNotice",messageVO);

        //查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        messageVO.put("message",message);
        if(message!=null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("eneityId",data.get("eneityId"));
            messageVO.put("postId",data.get("postId"));

            //所有数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count",count);
            //所有数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread",unread);
        }
        model.addAttribute("likeNotice",messageVO);

        //查询关注类通知
       message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        messageVO.put("message",message);
        if(message!=null){
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("eneityId",data.get("eneityId"));

            //所有数量
            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count",count);
            //所有数量
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread",unread);
        }
        model.addAttribute("followNotice",messageVO);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(),null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticerUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticerUnreadCount",noticerUnreadCount);
        return "/site/notice";
    }

    /**
     * 获取某个通知主题下的私信列表
     */
    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page,Model model){
        User user = holder.getUser();
        //设置分页
        page.setLimit(5);
        page.setPath("/message/notice/detail/"+topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        //获取数据
        List<Message> noticeList = messageService.findNoticeList(user.getId(),topic, page.getOffset(), page.getLimit());
        List<Map<String,Object>> noticeVOList = new ArrayList<>();
        if(noticeList!=null){
            for(Message notice : noticeList){
                Map<String,Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                //内容
                String content= HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);
                map.put("user",userService.findUserById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("eneityId",data.get("eneityId"));
                map.put("postId",data.get("postId"));

                //通知作者
                map.put("fromUser",userService.findUserById(notice.getFromId()));
                noticeVOList.add(map);
            }
        }
        model.addAttribute("noties",noticeVOList);
        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.updateLetterStatus(ids);
        }
        return "/site/notice-detail";
    }
}
