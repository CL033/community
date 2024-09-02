package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * DiscussPost:处理的实体类型
 * Integer：实体类的主键
 * ElasticsearchRepository：父接口，其中已经事先定义好了对es服务器的增删改查的方法，加上注解后Spring会自动调用，直接使用即可
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository <DiscussPost,Integer>{
}
