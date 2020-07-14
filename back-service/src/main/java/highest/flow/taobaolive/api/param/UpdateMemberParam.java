package highest.flow.taobaolive.api.param;

import lombok.Data;

import java.util.List;

@Data
public class UpdateMemberParam {

    private int id;

    private String memberName;

    private String password;

    private String mobile;

    private String comment;

    private List<String> roles;

    private int state;
}
