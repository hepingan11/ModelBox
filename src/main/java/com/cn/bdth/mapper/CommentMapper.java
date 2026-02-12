package com.cn.bdth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.cn.bdth.entity.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
