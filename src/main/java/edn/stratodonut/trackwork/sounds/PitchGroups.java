package edn.stratodonut.trackwork.sounds;

public class PitchGroups {
    public enum Group {
        VERY_LOW, LOW, NORMAL, HIGH, VERY_HIGH
    }

    public static Group getGroupFromPitch(float pitch) {
        if (pitch < .70)
            return Group.VERY_LOW;
        if (pitch < .90)
            return Group.LOW;
        if (pitch < 1.10)
            return Group.NORMAL;
        if (pitch < 1.30)
            return Group.HIGH;
        return Group.VERY_HIGH;
    }
}
