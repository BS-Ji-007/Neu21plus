package io.github.legentpc.neu21plus.client.overlay;

public class LerpingFloat {

    private float value;
    private float target;
    private float speed;

    public LerpingFloat(float value, float speed) {
        this.value = value;
        this.target = value;
        this.speed = speed;
    }

    public LerpingFloat(float value) {
        this(value, 0.1f);
    }

    public void tick() {
        if (Math.abs(target - value) < 0.01f) {
            value = target;
        } else {
            value += (target - value) * speed;
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public void reset() {
        this.value = 0;
        this.target = 0;
    }

    public boolean isAtTarget() {
        return Math.abs(target - value) < 0.01f;
    }

    public void snapToTarget() {
        this.value = target;
    }
}
