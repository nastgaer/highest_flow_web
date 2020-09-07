package highest.flow.taobaolive.taobao.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaobaoAccountLogDao extends BaseMapper<TaobaoAccountLogEntity> {

    IPage<TaobaoAccountLogEntity> queryAccountLogs(IPage<TaobaoAccountLogEntity> page,
                                                   @Param("member_id") int memberId,
                                                   @Param("keyword") String keyword);
}
