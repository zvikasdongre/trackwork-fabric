package edn.stratodonut.trackwork.tracks;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import edn.stratodonut.trackwork.tracks.blocks.OleoWheelBlockEntity;
import edn.stratodonut.trackwork.tracks.blocks.WheelBlockEntity;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class TrackPonderScenes {
    public static void trackTutorial(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("tracks", "How to place tracks");
        scene.showBasePlate();

        scene.overlay().showText(60)
                .independent(20)
                .text("Disclaimer: Tracks only work on assembled VS2 ships!");
        scene.idle(60);

        scene.world().showSection(util.select().fromTo(0, 2, 1, 4, 2, 1), Direction.DOWN);
        scene.overlay().showText(60)
                .text("Just like chaindrive blocks, ")
                .attachKeyFrame();

        scene.idle(60);

        scene.world().hideSection(util.select().fromTo(0, 2, 1, 4, 2, 1), Direction.UP);

        scene.idle(20);

        scene.overlay().showText(80)
                .text("tracks are built in a row.");
        for (int i = 0; i < 5; i++) {
            scene.world().showSection(util.select().position(i, 2, 2), Direction.DOWN);
            scene.idle(7);
        }

        scene.idle(40);

        scene.world().showSection(util.select().fromTo(0, 2, 3, 4, 2, 4), Direction.UP);
        scene.overlay().showOutlineWithText(util.select().position(0, 2, 2), 100)
                .text("Tracks are powered via connected sprockets.");
        scene.markAsFinished();
    }

    public static void wheelTutorial(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("wheels", "How to place wheels");
        scene.showBasePlate();

        scene.overlay().showText(60)
                .independent(20)
                .text("Disclaimer: Wheels only work on assembled VS2 ships!");

        scene.idle(50);
        scene.addKeyframe();
        scene.idle(10);

//        scene.world.showSection(util.select.fromTo(0, 2, 1, 4, 2, 1), Direction.DOWN);
        scene.overlay().showText(60)
                .text("Wheels are placed like, well, wheels");
        // SHOW AXLES
        scene.world().showSection(util.select().fromTo(4, 3, 1, 4, 3, 3), Direction.DOWN);
        // SHOW WHEELS
        scene.world().showSection(util.select().position(0, 3, 0), Direction.DOWN);
        scene.world().showSection(util.select().position(0, 3, 4), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 3, 0), Direction.DOWN);
        scene.world().showSection(util.select().position(4, 3, 4), Direction.DOWN);

        scene.idle(60);

        // GIVE ROTATION?
        scene.overlay().showOutlineWithText(util.select().position(4, 3, 1), 60)
                .text("Drive the wheels via the rib side,");

        scene.idle(60);

        // HIGHLIGHT EMPTY SHAFT
        scene.overlay().showOutlineWithText(util.select().position(0, 3, 1), 80)
                .text("Or don't connect them, which lets the wheel spin freely.");

        scene.idle(80);
        scene.addKeyframe();
        scene.idle(20);

        // SHOW REDSTONE LINk
        scene.world().showSection(util.select().fromTo(0, 3, 1, 0, 3, 3), Direction.DOWN);
        scene.overlay().showOutlineWithText(util.select().position(0, 3, 1), 80)
                .text("Power a wheel with redstone to steer it,");

        scene.idle(40);

        // ACTIVATE LINK
        scene.world().toggleRedstonePower(util.select().position(0, 3, 1));

        scene.world().modifyBlockEntity(util.grid().at(0, 3, 0), WheelBlockEntity.class,
                wbe -> wbe.setSteeringValue(-1.0f));
        scene.world().modifyBlockEntity(util.grid().at(0, 3, 4), WheelBlockEntity.class,
                wbe -> wbe.setSteeringValue(-1.0f));

        scene.idle(40);

        // SHOW LINKAGE
        scene.overlay().showOutlineWithText(util.select().position(0, 3, 4), 80)
                .text("and the opposite side will automatically follow.");
        scene.markAsFinished();
    }

    public static void landingGearTutorial(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("oleo", "Landing Gear Configuration");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        scene.overlay().showText(60)
                .independent(20)
                .text("Disclaimer: Wheels only work on assembled VS2 ships!");

        scene.idle(50);
        scene.addKeyframe();
        scene.idle(10);

        // Define Selections
        Selection leftMain = util.select().position(4, 3, 4);
        Selection rightMain = util.select().position(4, 3, 0);
        Selection chassis = util.select().fromTo(4, 3, 1, 4, 3, 3); // Structure connecting rear landing gears
        BlockPos bearingLeft = util.grid().at(5, 3, 4);
        BlockPos bearingRight = util.grid().at(5, 3, 0);

        Selection noseAssembly = util.select().fromTo(0, 3, 2, 0, 4, 2);
        BlockPos noseGear = util.grid().at(0, 3, 2);
        BlockPos bearingPos = util.grid().at(0, 4, 3); // Mechanical bearing stowing nose gear
        BlockPos bearingShaft = util.grid().at(0, 4, 4);

        Selection redstone_brakes = util.select().fromTo(3, 3, 0, 3, 3, 4);

        Selection redstone_left = util.select().position(0, 3, 1);
        Selection redstone_right = util.select().position(0, 3, 3);

        // --- STEP 1: SETUP ---

        scene.idle(10);
        // Show the frame/chassis first
        scene.world().showSection(chassis, Direction.DOWN);
        scene.idle(10);

        // Show the Main Gears
        ElementLink<WorldSectionElement> leftContraption = scene.world().showIndependentSection(leftMain, Direction.DOWN);
        ElementLink<WorldSectionElement> rightContraption = scene.world().showIndependentSection(rightMain, Direction.DOWN);
        scene.idle(5);

        // Show the Nose Gear
        ElementLink<WorldSectionElement> noseContraption =
                scene.world().showIndependentSection(noseAssembly, Direction.EAST);

        scene.idle(20);

        // Text Overlay
        scene.overlay().showText(70)
                .text("There are two variants of landing gears, though they function the same.");

        // Highlight the arrangement
        scene.overlay().showOutline(PonderPalette.GREEN, "NLG", util.select().position(noseGear), 60);
        scene.overlay().showOutline(PonderPalette.GREEN, "LMLG", leftMain, 60);
        scene.overlay().showOutline(PonderPalette.GREEN, "RMLG", rightMain, 60);

        scene.idle(70);
        scene.addKeyframe();

        // --- STEP 2: STEERING ---

        // Show the redstone source (e.g., a lever or link)
        scene.world().showSection(redstone_left, Direction.DOWN);
        scene.world().showSection(redstone_right, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
                .text("Apply a Redstone signal to the sides of the block to steer")
                .pointAt(util.vector().centerOf(0, 3, 2));

        scene.idle(40);

        // Toggle Redstone
        scene.world().toggleRedstonePower(redstone_left);
        scene.effects().indicateRedstone(util.grid().at(0, 3, 2));

        // Animate the wheel turning
        scene.world().modifyBlockEntity(util.grid().at(0, 3, 2), OleoWheelBlockEntity.class,
                be -> be.setSteeringValue(-1.0f));

        scene.idle(20);

        // Other way
        scene.world().toggleRedstonePower(redstone_left);
        scene.world().toggleRedstonePower(redstone_right);
        scene.world().modifyBlockEntity(util.grid().at(0, 3, 2), OleoWheelBlockEntity.class,
                be -> be.setSteeringValue(1.0f));

        scene.idle(20);

        // Reset
        scene.world().toggleRedstonePower(redstone_right);
        scene.world().modifyBlockEntity(util.grid().at(0, 3, 2), OleoWheelBlockEntity.class,
                be -> be.setSteeringValue(0.0f));

        scene.idle(20);

        scene.world().hideSection(redstone_left, Direction.WEST);
        scene.world().hideSection(redstone_right, Direction.WEST);

        scene.addKeyframe();

        // STEP 3: BRAKES

        scene.world().showSection(redstone_brakes, Direction.DOWN);
        scene.idle(20);

        scene.world().toggleRedstonePower(redstone_brakes);
        scene.overlay().showText(60)
                .text("Apply a Redstone signal to the front or back of the block to brake");

        scene.idle(60);
        scene.world().toggleRedstonePower(redstone_brakes);
        scene.idle(20);

        scene.world().hideSection(redstone_brakes, Direction.UP);

        scene.idle(30);
        scene.addKeyframe();

        // --- STEP 4: STOWING (CONTRAPTION) ---

        // Show the nose mechanical bearing setup
        scene.world().showSection(util.select().position(bearingLeft), Direction.WEST);
        scene.world().showSection(util.select().position(bearingRight), Direction.WEST);
        scene.world().showSection(util.select().fromTo(bearingPos, bearingShaft), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(80)
                .text("Use contraption mechanisms to stow your landing gear after takeoff")
                .pointAt(util.vector().topOf(bearingPos));

        scene.overlay().showOutline(PonderPalette.FAST, "BEARING", util.select().position(bearingPos),  60);

        scene.idle(50);

        scene.world().setKineticSpeed(util.select().position(bearingShaft), 7.5f);
        scene.effects().rotationSpeedIndicator(bearingPos.south());
        scene.world().rotateBearing(bearingPos, 90, 40);
        scene.world().moveSection(noseContraption, util.vector().of(0.5, 0.5, 0), 40);
        scene.world().rotateSection(noseContraption, 0, 0, 90, 40);

        scene.world().rotateBearing(bearingLeft, -90, 40);
        scene.world().rotateSection(leftContraption, -90, 0, 0, 40);

        scene.world().rotateBearing(bearingRight, 90, 40);
        scene.world().rotateSection(rightContraption, 90, 0, 0, 40);

        scene.idle(40);

        scene.world().setKineticSpeed(util.select().position(bearingShaft), 0);

        scene.markAsFinished();
    }
}
