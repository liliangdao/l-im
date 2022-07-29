package com.lld.im.service.conversation.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.conversation.dao.ImConversationSetEntity;
import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

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
            "(conversation_id , conversation_type , from_id , to_id ,is_mute,is_top,conversation_sequence,readed_sequence,app_id)" +
            " values(#{entity.conversationId},#{entity.conversationType}" +
            " ,#{entity.fromId},#{entity.toId},#{entity.isMute},#{entity.isTop},#{entity.conversationSequence} " +
            " ,#{entity.readedSequence},#{entity.appId} ) " +
            " ON DUPLICATE KEY UPDATE" +
            "  conversation_sequence = VALUES (conversation_sequence), " +
            "  readed_sequence = VALUES (readed_sequence), ")
    public int markConversation(ImConversationSetEntity entity);

}
