package com.lld.im.service.friendship.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.jeffreyning.mybatisplus.anno.AutoMap;
import com.github.jeffreyning.mybatisplus.base.MppBaseMapper;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.model.req.CheckFriendShipReq;
import com.lld.im.service.friendship.model.resp.CheckFriendShipResp;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Repository
//@AutoMap
public interface ImFriendShipMapper extends MppBaseMapper<ImFriendShipEntity> {

    @Select(
            " select Max(friend_sequence) from im_friendship where app_id = #{appId} and from_id = #{userId} "
    )
    Long getFriendShipMaxSeq(Integer appId, String userId);

    @Select("<script>" +
            " select from_id AS fromId, to_id AS toId , if(status = 1,1,0) as status from im_friendship where app_id = #{appId} and from_id = #{fromId}  and  to_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            "</script>"
    )
    List<CheckFriendShipResp> checkFriendShip(CheckFriendShipReq req);

//    @Select("<script>" +
//            " select from_id AS fromId, to_id AS toId , if(status = 1,1,0) as status from im_friendship where app_id = #{appId} and to_id = #{fromId}  and  from_id in " +
//            "<foreach collection='toId' index='index' item='id' separator=',' close=')' open='('>" +
//            " #{id} " +
//            "</foreach>" +
//            "</script>"
//    )
//    List<CheckFriendShipResp> checkFriendShipBoth(CheckFriendShipReq toId);


    @Select("<script>" +
            " select a.fromId,a.toId , ( \n" +
            " case \n" +
            " when a.status = 1 and b.status = 1 then 1 \n" +
            " when a.status = 1 and b.status != 1 then 2 \n" +
            " when a.status != 1 and b.status = 1 then 3 \n" +
            " when a.status != 1 and b.status != 1 then 4 \n" +
            " end \n" +
            " ) \n " +
            " as status from "+
            " (select from_id AS fromId , to_id AS toId , if(status = 1,1,0) as status from im_friendship where app_id = #{appId} and from_id = #{fromId} AND to_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " ) as a INNER join" +
            " (select from_id AS fromId, to_id AS toId , if(status = 1,1,0) as status from im_friendship where app_id = #{appId} and to_id = #{fromId} AND from_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " ) as b " +
            " on a.fromId = b.toId AND b.fromId = a.toId "+
            "</script>"
    )
    List<CheckFriendShipResp> checkFriendShipBoth(CheckFriendShipReq toId);


// sql示例
//    select a.fromId,a.toId, (
//            case
//
//    when a.status = 1 and b.status = 1 then 1
//    when a.status = 1 and b.status != 1 then 2
//    when a.status != 1 and b.status = 1 then 3
//    when a.status != 1 and b.status != 1 then 4
//    end
//
//) as status from
//            (select from_id AS fromId, to_id AS toId , if(status = 1,1,0) as status from im_friendship where app_id = 10000 and from_id = 'lld'  and  to_id in ('lld2','lld4','lld5')) as a
//    INNER join
//            (select from_id AS fromId, to_id AS toId , if(status = 1,1,0) as status from im_friendship where app_id = 10000 and to_id = 'lld'  and  from_id in ('lld2','lld4','lld5')) as b
//    on a.fromId = b.toId AND b.fromId = a.toId


    @Select("<script>" +
            " select from_id AS fromId, to_id AS toId , if(black = 1,1,0) as status from im_friendship where app_id = #{appId} and from_id = #{fromId}  and  to_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            "</script>"
    )
    List<CheckFriendShipResp> checkFriendShipBlack(CheckFriendShipReq req);

    @Select("<script>" +
            " select a.fromId,a.toId , ( \n" +
            " case \n" +
            " when a.black = 1 and b.black = 1 then 1 \n" +
            " when a.black = 1 and b.black != 1 then 2 \n" +
            " when a.black != 1 and b.black = 1 then 3 \n" +
            " when a.black != 1 and b.black != 1 then 4 \n" +
            " end \n" +
            " ) \n " +
            " as status from "+
            " (select from_id AS fromId , to_id AS toId , if(black = 1,1,0) as black from im_friendship where app_id = #{appId} and from_id = #{fromId} AND to_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " ) as a INNER join" +
            " (select from_id AS fromId, to_id AS toId , if(black = 1,1,0) as black from im_friendship where app_id = #{appId} and to_id = #{fromId} AND from_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " ) as b " +
            " on a.fromId = b.toId AND b.fromId = a.toId "+
            "</script>"
    )
    List<CheckFriendShipResp> checkFriendShipBlackBoth(CheckFriendShipReq toId);

    @Select(
            " select to_id from im_friendship where from_id = #{userId} AND app_id = #{appId} and status = 1 and black = 1 "
    )
    List<String> getAllFriendId(String userId,Integer appId);

}
