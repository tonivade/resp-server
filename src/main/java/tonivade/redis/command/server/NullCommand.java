package tonivade.redis.command.server;

import tonivade.redis.command.ICommand;
import tonivade.redis.command.IRequest;
import tonivade.redis.command.IResponse;

public class NullCommand implements ICommand {

    @Override
    public void execute(IRequest request, IResponse response) {
        response.addError("ERR unknown command '" + request.getCommand() + "'");
    }

}
