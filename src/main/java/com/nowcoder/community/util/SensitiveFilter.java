package com.nowcoder.community.util;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    //替换字符
    private static final String REPLACEMENT = "***";
    //根节点
    private TireNode rootNode = new TireNode();

    //初始化根节点
    @PostConstruct
    public void init() {

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             //将字节流转换为字符流，再转换为缓冲区
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
            //用来接从文件中获得的敏感词
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                //添加到前缀树
                this.addKeyWord(keyword);
            }
        } catch (IOException e) {
            logger.error("加载文件失败" + e.getMessage());
        }

    }

    //添加到前缀树
    private void addKeyWord(String keyword) {
        TireNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TireNode subNode = tempNode.getSubNode(c);
            //如果没有这个节点，则将其作为新节点插入到树中
            if (subNode == null) {
                subNode = new TireNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;
            //若是最后一个节点，设置标识符
            if (i == keyword.length() - 1) {
                tempNode.setWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        //指针1
        TireNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //记录结果
        StringBuilder sb = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position);
            //跳过字符
            if (isSymnol(c)) {
                //如果指针1指在根节点，将此符号计入结果，指针2下移
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或者中间，指针3都要走
                position++;
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                //说明以begin为开头的不是敏感词
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isWordEnd) {
                //发现敏感词,将begin-pisition的全部替换
                sb.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            } else {
                //检查下一个字符
                position++;
            }
        }
        //将最后一批字符加入
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymnol(Character ch) {
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(ch) && (ch < 0x2E80 || ch > 0x9FFF);
    }

    //前缀树
    private class TireNode {
        //关键词结束标志
        @Setter
        @Getter
        private boolean isWordEnd = false;
        //子节点（key是下级字符，value是下级节点）
        private Map<Character, TireNode> subNodes = new HashMap<>();


        //添加下级节点
        public void addSubNode(Character c, TireNode node) {
            subNodes.put(c, node);
        }

        //获取字节的
        public TireNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
