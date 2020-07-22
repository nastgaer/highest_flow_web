package highest.flow.taobaolive.security.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import highest.flow.taobaolive.security.entity.LicenseCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LicenseCodeDao extends BaseMapper<LicenseCode> {

    LicenseCode getCodeDesc(@Param("code") String code);
}
