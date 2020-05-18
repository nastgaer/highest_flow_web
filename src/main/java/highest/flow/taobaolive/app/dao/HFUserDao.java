package highest.flow.taobaolive.app.dao;

import highest.flow.taobaolive.app.entity.HFUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HFUserDao {

    public HFUser getUserByUserId(String userId);

    public int insertUser(HFUser user);
}
