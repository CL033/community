package com.nowcoder.community;

import co.elastic.clients.elasticsearch.ml.Page;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    //插入一条数据
    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.findDiscussPostByID(241));
        discussPostRepository.save(discussPostMapper.findDiscussPostByID(242));
        discussPostRepository.save(discussPostMapper.findDiscussPostByID(243));
    }
    //插入多条数据
//    @Test
//    public void testInsertList(){
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100));
//        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100));
//    }

    //搜索
    @Test
    public void testRepository(){

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSort(Sort.by("type","score","createTime").descending())
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery,DiscussPost.class);
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(search,searchQuery.getPageable());
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit<DiscussPost> discussPostSearchHit : page) {
            //System.out.println(discussPostSearchHit.getHighlightFields());
            DiscussPost discussPost = discussPostSearchHit.getContent();

            //取高亮值
            List<String> title = discussPostSearchHit.getHighlightFields().get("title");
            if(title!=null){
                discussPost.setTitle(title.get(0));
            }
            List<String> content = discussPostSearchHit.getHighlightFields().get("content");
            if(title!=null){
                discussPost.setContent(content.get(0));
            }
            list.add(discussPost);
        }
        for(DiscussPost discussPost:list){
            System.out.println(discussPost);
        }
    }
}
