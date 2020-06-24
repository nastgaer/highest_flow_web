package highest.flow.taobaolive.task;

import highest.flow.taobaolive.service.AutoLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("autoLoginTask")
public class AutoLoginTask implements ITask {

    @Autowired
    private AutoLoginService autoLoginService;

    @Override
    public void run(String params) {
        autoLoginService.doAutoLogin();
        System.out.println("AutoLogin::run");
    }
}
