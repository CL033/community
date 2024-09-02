package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    //将指定IP存入UV
    public void recordUV(String ip) {
        String date = df.format(new Date());
       String rediskey = RedisKeyUtil.getUVKey(date);
       redisTemplate.opsForHyperLogLog().add(rediskey, ip);
    }

    //统计指定日期范围内的UV
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数错误");
        }
        //整理范围内的key
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));
            keyList.add(key);
            //日期加1
            calendar.add(Calendar.DATE, 1);
        }
        //合并数据
        String rediskey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(rediskey, keyList.toArray());

        //返回统计结果
        return redisTemplate.opsForHyperLogLog().size(rediskey);
    }

    //将指定用户存入DAU
    public void recordDAU(int userId) {
        String rediskey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(rediskey,userId,true);
    }

    //统计指定日期范围内的UV
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数错误");
        }
        //整理范围内的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
            keyList.add(key.getBytes());
            //日期加1
            calendar.add(Calendar.DATE, 1);
        }
        //进行or运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String rediskey = RedisKeyUtil.getDAUKey(df.format(start),df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,rediskey.getBytes(),
                        keyList.toArray(new byte[0][0]));
                return connection.bitCount(rediskey.getBytes());
            }
        });

    }
}
