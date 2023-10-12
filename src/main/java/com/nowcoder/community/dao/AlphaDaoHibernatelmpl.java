package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("alphadapHibernate")
public class AlphaDaoHibernatelmpl implements AlphaDao{
    @Override
    public String select() {
        return "hibernate";
    }
}
