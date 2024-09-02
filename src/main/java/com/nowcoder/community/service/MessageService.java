package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.util.SensitiveFilter;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId, offset, limit);
    }
    /**
     *  查询当前用户会话列表的会话数量
     */
    public int findConversationCount(int userId){
        return messageMapper.selectConversationCount(userId);
    }
    /**
     * 查询某个会话包含的私信列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> findLetters(String conversationId,int offset,int limit){
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * 查询私信列表的数量
     * @param conversationId
     * @return
     */
    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * 查询未读私信的数量（conversationId可传可不传，传代表单签的某个会话的未读数量，不传代表全部的未读数量）
     * @param userId
     * @param conversationId
     * @return
     */
    public int findLetterUnreadCount(int userId,String conversationId){
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }
    /**
     * 添加私信
     */
    public int addLetter(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertLetter(message);
    }
    /**
     * 修改状态
     */
    public int updateLetterStatus(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

    /**
     * 查询某主题下的最新通知
     * @param userId
     * @param topic
     * @return
     */
    public Message findLatestNotice(int userId, String topic){
        return messageMapper.selectLatestNotice(userId, topic);
    }

    /**
     * 查询某主题下的通知数量
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeCount(int userId, String topic){
        return messageMapper.selectNoticCount(userId, topic);
    }

    /**
     * 查询某主题下的未读通知数量
     * @param userId
     * @param topic
     * @return
     */
    public int findNoticeUnreadCount(int userId,String topic){
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    /**
     * 查询某主题下的信息列表
     */
    public List<Message> findNoticeList(int userId,String topic,int offset,int limit){
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
