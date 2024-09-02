package com.nowcoder.community;

import com.nowcoder.community.config.RedisConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void testStrings(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey,1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

    }

    //统计20万个重复数据的独立总数
    @Test
    public void testHyperLoggLog(){
        String rediskey = "test:hll:01";
        for (int i = 1; i <=100000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(rediskey,i);
        }
        for (int i = 1; i <=100000 ; i++) {
            int r= (int) (Math.random()*100000+1);
            redisTemplate.opsForHyperLogLog().add(rediskey,r);
        }
        Long size = redisTemplate.opsForHyperLogLog().size(rediskey);
        System.out.println(size);
    }

    //将3组数据合并，在统计合并之后的重复数据的独立总数
    @Test
    public void testHyperLogLogUnion(){
        String rediskey2 = "test:hll:02";
        for (int i =1; i <=10000; i++) {
            redisTemplate.opsForHyperLogLog().add(rediskey2,i);
        }
        String rediskey3 = "test:hll:03";
        for (int i = 5001; i <=1500; i++) {
            redisTemplate.opsForHyperLogLog().add(rediskey3,i);
        }
        String rediskey4 = "test:hll:04";
        for (int i = 10001; i <=20000; i++) {
            redisTemplate.opsForHyperLogLog().add(rediskey4,i);
        }
        String rediskeyUnion = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(rediskeyUnion,rediskey2,rediskey3,rediskey4);
        Long size = redisTemplate.opsForHyperLogLog().size(rediskeyUnion);
        System.out.println(size);

    }

    //统计一组数据的布尔值
    @Test
    public void testBitMap(){
        String rediskey = "test:bm:01";

        redisTemplate.opsForValue().setBit(rediskey,1,true);
        redisTemplate.opsForValue().setBit(rediskey,4,true);
        redisTemplate.opsForValue().setBit(rediskey,7,true);

        //查询
        System.out.println(redisTemplate.opsForValue().getBit(rediskey,0));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey,1));
        System.out.println(redisTemplate.opsForValue().getBit(rediskey,2));

        //统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(rediskey.getBytes());
            }
        });
        System.out.println(obj);
    }

    //多组数据的布尔运算
    @Test
    public void testBitMapOperation(){
        String rediskey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(rediskey2,0,true);
        redisTemplate.opsForValue().setBit(rediskey2,1,true);
        redisTemplate.opsForValue().setBit(rediskey2,2,true);

        String rediskey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(rediskey3,2,true);
        redisTemplate.opsForValue().setBit(rediskey3,3,true);
        redisTemplate.opsForValue().setBit(rediskey3,4,true);

        String rediskey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(rediskey4,4,true);
        redisTemplate.opsForValue().setBit(rediskey4,5,true);
        redisTemplate.opsForValue().setBit(rediskey4,6,true);

        String rediskeyOr = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {

                connection.bitOp(RedisStringCommands.BitOperation.OR,rediskeyOr.getBytes(),
                        rediskey2.getBytes(),rediskey3.getBytes(),rediskey4.getBytes());
                return connection.bitCount(rediskeyOr.getBytes());
            }
        });
        System.out.println(obj);
        for (int i = 0; i < 7; i++) {
            System.out.println(redisTemplate.opsForValue().getBit(rediskeyOr,i));

        }
    }
    @Test
    public void testTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String data = df.format(new Date());
        System.out.println(data);
    }
}
