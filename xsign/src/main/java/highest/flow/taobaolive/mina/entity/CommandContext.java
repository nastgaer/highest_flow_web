package highest.flow.taobaolive.mina.entity;

import lombok.Data;
import org.apache.mina.core.session.IoSession;

import java.util.Map;

@Data
public class CommandContext {

    private IoSession ioSession;

    private Map<String, Object> data;
}
