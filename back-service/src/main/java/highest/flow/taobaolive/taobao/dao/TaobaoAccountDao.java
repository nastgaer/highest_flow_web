package highest.flow.taobaolive.taobao.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaobaoAccountDao extends BaseMapper<TaobaoAccountEntity> {

    int getNormalCount();

    int getExpiredCount();

    TaobaoAccountEntity getActiveOne();

    TaobaoAccountEntity getActiveOneByMember(@Param("member_id") int memberId);

    List<TaobaoAccountEntity> getActiveAll();

    List<TaobaoAccountEntity> getActiveAllByMember(@Param("member_id") int memberId);
}
