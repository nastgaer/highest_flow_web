package highest.flow.taobaolive.sys.entity;

import lombok.Data;

import java.util.List;

@Data
public class RegisterUserEntity {

    private String memberName;

    private String password;

    private String mobile;

    private String comment;

    private List<String> role;

    private int state;
}
