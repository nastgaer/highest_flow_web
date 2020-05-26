package highest.flow.taobaolive.common.defines;

public enum ServiceType {

    刷热度(1),
    高级引流(2),
    引流神器(3);

    private int serviceType = 0;
    ServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getServiceType() {
        return serviceType;
    }

    public static ServiceType fromInt(int value) {
        for (ServiceType serviceType : ServiceType.values()) {
            if (serviceType.getServiceType() == value) {
                return serviceType;
            }
        }
        return null;
    }
}
