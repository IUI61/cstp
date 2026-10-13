package org.example.cstp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.cstp.entity.User;
import org.example.cstp.vo.ConversationsListVo;

public interface UserMapper extends BaseMapper<User> {
    ConversationsListVo findById(Long id);
}
