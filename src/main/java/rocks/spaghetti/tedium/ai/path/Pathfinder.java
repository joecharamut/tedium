package rocks.spaghetti.tedium.ai.path;

import net.minecraft.util.math.Vec3i;

import java.util.List;
import java.util.Optional;

public interface Pathfinder {
    Optional<List<Vec3i>> findPath(Vec3i start, Vec3i goal);
}
