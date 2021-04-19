package rocks.spaghetti.tedium.ai.movement;

import rocks.spaghetti.tedium.util.Rotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MovementState {
    private final Map<Input, Boolean> inputs = new HashMap<>();
    private Optional<Rotation> lookTarget = Optional.empty();
    private MovementStatus status = MovementStatus.INITIAL;

    public MovementState() {
        for (Input key : Input.values()) inputs.put(key, false);
    }

    public MovementStatus getStatus() {
        return status;
    }

    public MovementState setStatus(MovementStatus status) {
        this.status = status;
        return this;
    }

    public MovementState setInput(Input input, boolean state) {
        inputs.put(input, state);
        return this;
    }

    public boolean getInput(Input input) {
        return inputs.get(input);
    }

    public void clearInputs() {
        for (Input key : Input.values()) inputs.put(key, false);
    }

    public MovementState setLookTarget(Rotation rotation) {
        if (rotation == null) {
            this.lookTarget = Optional.empty();
        } else {
            this.lookTarget = Optional.of(rotation);
        }
        return this;
    }

    public Optional<Rotation> getLookTarget() {
        return lookTarget;
    }
}
