package highest.flow.taobaolive.common.defines;

public enum ScheduleState {

    /**
     * 正常
     */
    NORMAL(0),
    /**
     * 暂停
     */
    PAUSE(1);

    private int value;

    ScheduleState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
