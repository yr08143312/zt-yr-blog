package xyz.yangrui.ztyrblog.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import xyz.yangrui.ztyrblog.modal.Vo.UserVo;
import xyz.yangrui.ztyrblog.modal.Vo.UserVoExample;

import java.util.List;


@Component
public interface UserVoMapper {
    long countByExample(UserVoExample example);

    int deleteByExample(UserVoExample example);

    int deleteByPrimaryKey(Integer uid);

    int insert(UserVo record);

    int insertSelective(UserVo record);

    List<UserVo> selectByExample(UserVoExample example);

    UserVo selectByPrimaryKey(Integer uid);

    int updateByExampleSelective(@Param("record") UserVo record, @Param("example") UserVoExample example);

    int updateByExample(@Param("record") UserVo record, @Param("example") UserVoExample example);

    int updateByPrimaryKeySelective(UserVo record);

    int updateByPrimaryKey(UserVo record);
}