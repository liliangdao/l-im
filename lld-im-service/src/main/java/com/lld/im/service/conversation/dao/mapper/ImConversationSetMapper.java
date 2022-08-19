package com.lld.im.service.conversation.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.conversation.dao.ImConversationSetEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ImConversationSetMapper extends BaseMapper<ImConversationSetEntity> {

    /**
     * @description 插入/更新會話
     * @author chackylee
     * @date 2022/7/29 16:49
     * @param [entity] 
     * @return int
    */
    @Insert(" insert into im_conversation_set " +
            "(conversation_id , conversation_type , from_id , to_id ,is_mute,is_top,sequence,readed_sequence,app_id)" +
            " values(#{conversationId},#{conversationType}" +
            " ,#{fromId},#{toId},#{isMute},#{isTop},#{sequence} " +
            " ,#{readedSequence},#{appId} ) " +
            " ON DUPLICATE KEY UPDATE " +
            "  sequence = VALUES (sequence) , " +
            "  readed_sequence = VALUES (readed_sequence) ")
    public int markConversation(ImConversationSetEntity entity);




    @Select(
            " select sequence from im_conversation_set where app_id = #{appId} and owner_id = #{userId} "
            )
    Long geConversationSetMaxSeq(Integer appId, String userId);

}
