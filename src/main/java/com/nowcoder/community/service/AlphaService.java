package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
public class AlphaService {

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private TransactionTemplate template;
    public AlphaService(){
        System.out.println("实例化");
    }
    @PostConstruct
    public void  init(){
        System.out.println("初始化");
    }
    @PreDestroy
    public void destory(){
        System.out.println("销毁");
    }

   //REQUIRED:支持当前事务（外部事务），如果不存在则创建新事务
    //REQUIRES_NEW：创建一个新事务，并且暂停当前事务（外部事务）
    //NESTED：如果存在当前事务（外部事务），则嵌套在当前事务中，不存在则和RQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save1(){
        User user = new User();
        user.setUsername("111");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        user.setEmail("123@com");
        user.setHeaderUrl("http://images.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle("Hello");
        discussPost.setContent("报道！");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(discussPost);

        return "Ok";
    }
    public Object save2(){
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
                user.setEmail("bate@com");
                user.setHeaderUrl("http://images.nowcoder.com/head/66t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                DiscussPost discussPost = new DiscussPost();
                discussPost.setUserId(user.getId());
                discussPost.setTitle("你好");
                discussPost.setContent("新人！");
                discussPost.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(discussPost);

                return "Ok";
            }
        });
    }

    //让该方法在多线程的环境下被异步调用
//    @Async
//    public void excute1(){
//        logger.debug("Hello excute1");
//    }
//    @Scheduled(initialDelay = 10000,fixedRate = 1000)
//    public void excute2(){
//        logger.debug("Hello excute2");
//    }
}
