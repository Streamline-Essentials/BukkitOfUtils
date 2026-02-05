package host.plas.bou.scheduling;

public enum TaskAnswer {
    ACCEPTED,
    REJECTED,
    ERROR,
    NULL,
    RUNNING,
    ;

    public boolean isDone() {
        return this != RUNNING;
    }
}
