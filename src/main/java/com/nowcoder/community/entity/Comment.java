package com.nowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    private int id;
    private int userId;
    private int entityType;//表示评论的类型，指的是用户还是帖子
    private int entityId;
    private int targetId;//回复用户的用户Id
    private String content;
    private int status;
    private Date createTime;
}
