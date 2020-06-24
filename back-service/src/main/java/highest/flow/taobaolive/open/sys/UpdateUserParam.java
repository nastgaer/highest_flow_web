package highest.flow.taobaolive.open.sys;

import lombok.Data;

import java.util.List;

@Data
public class UpdateUserParam {

    private int id;

    private String username;

    private String password;

    private String mobile;

    private String comment;

    private List<String> role;

    private int state;
}
