package io.yue.im.platform.friend.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.yue.im.platform.common.model.entity.Friend;
import io.yue.im.platform.common.model.vo.FriendVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @description 好友数据仓库
 */
public interface FriendRepository extends BaseMapper<Friend> {

    @Select("select id as id, friend_nick_name as nickName, friend_head_image as headImage from im_friend where user_id = #{userId}")
    List<FriendVO> getFriendVOList(@Param("userId") Long userId);

    @Select("select id as id, friend_nick_name as nickName, friend_head_image as headImage from im_friend where friend_id = #{friendId} and user_id = #{userId}")
    FriendVO getFriendVO(@Param("friendId") Long friendId, @Param("userId") Long userId);

    @Select("select 1 from im_friend where friend_id = #{friendId} and user_id = #{userId} limit 1 ")
    Integer checkFriend(@Param("friendId") Long friendId, @Param("userId") Long userId);

    @Update("update im_friend set friend_head_image = #{headImage}, friend_nick_name = #{nickName} where friend_id = #{friendId} and user_id = #{userId} ")
    int updateFriend(@Param("headImage") String headImage, @Param("nickName") String nickName, @Param("friendId") Long friendId, @Param("userId") Long userId);

    @Delete("delete from im_friend where friend_id = #{friendId} and user_id = #{userId} ")
    int deleteFriend(@Param("friendId") Long friendId, @Param("userId") Long userId);

    @Select("select friend_id from im_friend where user_id = #{userId}")
    List<Long> getFriendIdList(@Param("userId") Long userId);

    @Select("select id as id, user_id as userId, friend_id as friendId, friend_nick_name as friendNickName, friend_head_image as friendHeadImage, created_time as createdTime " +
            "from im_friend where user_id = #{userId} ")
    List<Friend> getFriendByUserId(@Param("userId") Long userId);

    @Update("update im_friend set " +
            "<set>" +
            "<if test = \"headImage != null and headImage != ''\">" +
            "friend_head_image = #{headImage} " +
            "</if>" +
            "<if test = \"nickName != null and nickName != ''\">" +
            "friend_nick_name = #{nickName} " +
            "</if>" +
            " where friend_id = #{friendId}" +
            "</set>")
    int updateFriendByFriendId(@Param("headImage") String headImage, @Param("nickName") String nickName, @Param("friendId") Long friendId);
}
