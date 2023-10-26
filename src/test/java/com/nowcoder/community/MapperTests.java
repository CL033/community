package com.nowcoder.community;

import com.nowcoder.community.dao.*;
import com.nowcoder.community.entity.*;
import com.nowcoder.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder11@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());
        System.out.println(user);
        int row = userMapper.insertUser(user);
        System.out.println(row);
        System.out.println(user.getId());

    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(151, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(151, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(151, "hello");
        System.out.println(rows);
    }

    @Test
    public void testSelectPost(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149,0,10);
        for (DiscussPost discussPost: list){
            System.out.println(discussPost);
        }
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    /**
     * 测试新增login_ticket
     */
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    /**
     * 测试查询login_ticket
     */
    @Test
    public void testSelectByTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    /**
     * 测试查询login_ticket的状态
     */
    @Test
    public void testUpdateStatus(){
        int abc = loginTicketMapper.updateStatus("abc", 1);
        System.out.println(abc);
    }

    /**
     * 测试修改密码
     */
    @Test
    public void testUpdatePassword(){
        int abc = userMapper.updatePassword(154,CommunityUtil.md5("456"+"2cfda"));
        System.out.println(abc);
    }

    @Test
    public void testInsertDiscussPost(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle("111");
        discussPost.setContent("222");
        discussPost.setUserId(151);
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);
    }

    @Test
    public void testSelectDiscussPost(){
        System.out.println(discussPostMapper.findDiscussPostByID(283));
    }
    @Test
    public void testselectCommentsByEtity(){
        commentMapper.selectCommentsByEtity(1,288,0,5);
        System.out.println(commentMapper.selectCommentsByEtity(1,228,0,5));
    }
    @Test
    public void testinsertComment(){
        Comment comment = new Comment();
        comment.setUserId(154);
        comment.setStatus(0);
        comment.setContent("哈哈");
        comment.setCreateTime(new Date());
        comment.setEntityId(275);
        comment.setEntityType(1);
        comment.setTargetId(0);
        System.out.println(commentMapper.insertComment(comment));
    }
    @Test
    public void testMEssageMApper(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for(Message m :messages){
            System.out.println(m);
        }
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

       messages = messageMapper.selectLetters("111_112", 0, 10);
        for(Message m :messages){
            System.out.println(m);
        }
        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count);
    }

    @Test
    public void testFindNotices(){
        List<Message> follow = messageMapper.selectNotices(111, "follow", 5, 10);
        for(Message message : follow){
            System.out.println(message);
        }

    }
}
