package highest.flow.taobaolive.common.defines;

public enum ServiceType {

    刷热度(1),
    高级引流(2);

    private int serviceType = 0;
    ServiceType(int serviceType) {
        this.serviceType = serviceType;
    }

    public int getServiceType() {
        return serviceType;
    }

}
