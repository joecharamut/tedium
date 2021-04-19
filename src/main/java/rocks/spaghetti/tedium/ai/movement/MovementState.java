package rocks.spaghetti.tedium.ai.movement;

public class MovementState {
    private boolean[] inputs = new boolean[Input.values().length];
    private MovementStatus status = MovementStatus.INITIAL;

    public MovementState() {

    }

    public MovementStatus getStatus() {
        return status;
    }

    public MovementState setStatus(MovementStatus status) {
        this.status = status;
        return this;
    }

    public MovementState setInput(Input input, boolean state) {
        inputs[input.ordinal()] = state;
        return this;
    }

    public boolean getInput(Input input) {
        return inputs[input.ordinal()];
    }
}
