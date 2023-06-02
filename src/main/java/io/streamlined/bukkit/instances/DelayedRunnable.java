package io.streamlined.bukkit.instances;

public abstract class DelayedRunnable extends BaseRunnable {
    public DelayedRunnable(int delay, boolean isAsyncable) {
        super(delay, 1, isAsyncable);
    }

    @Override
    public void execute() {
        onlyOnce();
        cancel();
    }

    public abstract void onlyOnce();
}
