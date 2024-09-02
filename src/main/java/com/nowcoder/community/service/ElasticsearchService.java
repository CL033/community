package com.nowcoder.community.service;

import co.elastic.clients.elasticsearch.ml.Page;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.apache.kafka.common.protocol.types.Field;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository disscussRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticTemplate;

    public void saveDiscussPost(DiscussPost post){
        disscussRepository.save(post);
    }

    public void deleteDiscussPost(int id){
        disscussRepository.deleteById(id);
    }

    public SearchPage<DiscussPost> searchDiscussPost(String keywords,int current,int limit ){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keywords,"title","content"))
                .withSort(Sort.by("type","score","createTime").descending())
                .withPageable(PageRequest.of(current,limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        //得到查询的结果
        SearchHits<DiscussPost> search = elasticTemplate.search(searchQuery,DiscussPost.class);
        //将其结果返回并进行分页
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search,searchQuery.getPageable());
        if(!page.isEmpty()){
            for (SearchHit<DiscussPost> discussPostSearch : page) {

                DiscussPost discussPost = discussPostSearch.getContent();
                if(discussPostSearch.getHighlightFields()!=null) {
                    //取高亮值,存在就将其替换原来的字段
                    List<String> title = discussPostSearch.getHighlightFields().get("title");
                    if (title != null) {
                        discussPost.setTitle(title.get(0));
                    }
                    List<String> content = discussPostSearch.getHighlightFields().get("content");
                    if (content != null) {
                        discussPost.setContent(content.get(0));
                    }
                }

            }
        }
        return page;
    }

}
