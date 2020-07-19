package highest.flow.taobaolive.taobao.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaobaoAccountDao extends BaseMapper<TaobaoAccountEntity> {

    int getNormalCount();

    int getExpiredCount();
}
