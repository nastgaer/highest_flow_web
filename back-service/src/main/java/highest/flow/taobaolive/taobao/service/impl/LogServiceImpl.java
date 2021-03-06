package highest.flow.taobaolive.taobao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import highest.flow.taobaolive.taobao.entity.TaobaoAccountLogEntity;
import highest.flow.taobaolive.taobao.dao.TaobaoAccountLogDao;
import highest.flow.taobaolive.taobao.service.LogService;
import org.springframework.stereotype.Service;

@Service("accountLogService")
public class LogServiceImpl extends ServiceImpl<TaobaoAccountLogDao, TaobaoAccountLogEntity> implements LogService {

}
