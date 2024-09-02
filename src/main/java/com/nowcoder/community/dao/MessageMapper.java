package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.kafka.common.protocol.types.Field;

import java.util.List;

@Mapper
public interface MessageMapper {
    //查询当前用户的会话列表，针对每一个会话只返回一条最新消息
    List<Message> selectConversations(int userId,int offset,int limit);
    //查询当前用户会话列表的会话数量
    int selectConversationCount(int userId);
    //查询某个会话包含的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);
    //查询私信列表的数量
    int selectLetterCount(String conversationId);
    // 查询未读私信的数量（conversationId可传可不传，传代表单签的某个会话的未读数量，不传代表全部的未读数量）
    int selectLetterUnreadCount(int userId,String conversationId);
    //新增消息
    int insertLetter(Message message);
    //修改状态
    int updateStatus(List<Integer> ids,int status);
    //查询某个主题下的最新消息通知
    Message selectLatestNotice(int userId,String topic);
    //查询某个主题下所包含的通知数量
    int selectNoticCount(int userId, String topic);
    //查询未读通知的数量
    int selectNoticeUnreadCount(int userId,String topic);
    //查询某个主题的通知列表
    List<Message> selectNotices(int userId,String topic,int offset,int limit);

}
