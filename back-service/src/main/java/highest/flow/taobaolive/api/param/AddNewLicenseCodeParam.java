package highest.flow.taobaolive.api.param;

import lombok.Data;

@Data
public class AddNewLicenseCodeParam {

    /**
     * LicenseCodeType
     */
    private int licenseCodeType;

    /**
     * MemberServiceType
     */
    private int serviceType;

    private int hours;
}
