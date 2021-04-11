package rocks.spaghetti.tedium.ai.movement;

public enum MovementStatus {
    PREPARING(false),
    WAITING(false),
    RUNNING(false),
    SUCCESS(true),
    FAILED(true);

    public final boolean complete;
    MovementStatus(boolean complete) {
        this.complete = complete;
    }
}
