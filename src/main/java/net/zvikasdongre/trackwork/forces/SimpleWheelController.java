package net.zvikasdongre.trackwork.forces;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mojang.datafixers.util.Pair;
import edn.stratodonut.trackwork.tracks.data.SimpleWheelData;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY
)
public class SimpleWheelController implements ShipForcesInducer {
    @JsonIgnore
    public static final double RPM_TO_RADS = 0.10471975512;
    @JsonIgnore
    public static final double MAXIMUM_SLIP = 10;
    @JsonIgnore
    public static final double MAXIMUM_SLIP_LATERAL = MAXIMUM_SLIP * 1.5;
    @JsonIgnore
    public static final double MAXIMUM_G = 98.1 * 5;
    public static final Vector3dc UP = new Vector3d(0, 1, 0);
    private final HashMap<Long, SimpleWheelData> trackData = new HashMap<>();

    @JsonIgnore
    private final ConcurrentLinkedQueue<Pair<Long, SimpleWheelData.SimpleWheelCreateData>> createdTrackData = new ConcurrentLinkedQueue<>();
    @JsonIgnore
    private final ConcurrentHashMap<Long, SimpleWheelData.SimpleWheelUpdateData> trackUpdateData = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Long> removedTracks = new ConcurrentLinkedQueue<>();
    private int nextBearingID = 0;

    private volatile Vector3dc suspensionAdjust = new Vector3d(0, 1, 0);
    private volatile float suspensionStiffness = 1.0f;
    private float debugTick = 0;

    public SimpleWheelController() {
    }

    public static SimpleWheelController getOrCreate(ServerShip ship) {
        if (ship.getAttachment(SimpleWheelController.class) == null) {
            ship.saveAttachment(SimpleWheelController.class, new SimpleWheelController());
        }

        return ship.getAttachment(SimpleWheelController.class);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {
        // DO NOTHING
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        // DO NOTHING
    }
}
