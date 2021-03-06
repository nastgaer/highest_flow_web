package highest.flow.taobaolive.taobao.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface TaobaoAccountDao extends BaseMapper<TaobaoAccountEntity> {

    IPage<TaobaoAccountEntity> queryAccounts(IPage<TaobaoAccountEntity> page,
                                             @Param("member_id") int memberId,
                                             @Param("keyword") String keyword);

    List<TaobaoAccountEntity> queryAccounts2(@Param("member_id") int memberId,
                                             @Param("updated") Date updated,
                                             @Param("state") int state,
                                             @Param("limit_count") int limitCount);

    int getNormalCount(@Param("member_id") int memberId, @Param("keyword") String keyword);

    int getExpiredCount(@Param("member_id") int memberId, @Param("keyword") String keyword);
}
