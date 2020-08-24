package highest.flow.taobaolive.mina.filter;

import highest.flow.taobaolive.mina.entity.CommandContext;

public interface ICommandFilter {

    boolean isRequest(CommandContext context);

    void execute(CommandContext context);

}
