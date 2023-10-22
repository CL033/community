package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId, int offset ,int limit);//userId为0表示首页不加入sql语句，非0则加入sql语句之后;offset表示起始行的行号，limit表示最多几条数据

    //@Param注解用来给参数取别名
    //如果只有一个参数，并且在<if>里面使用，则必须加别名
    int selectDiscussPostRows(@Param("userId")int userId);

    int insertDiscussPost(DiscussPost discussPost);
    DiscussPost findDiscussPostByID(int id);

    int updateCommentCount(int id,int commentCount);

}
