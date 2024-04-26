package com.cn.bdth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cn.bdth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 雨纷纷旧故里草木深
 *
 * @author 时间海 @github dulaiduwang003
 * @version 1.0
 * @date 2023/7/21 10:48
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Update("update user set frequency=frequency - 8  where user_id = #{id}")
    void updateFrequencyById(Long id);

    @Select("select frequency from user where user_id = #{id}")
    Long getFrequencyById(Long id);
}
