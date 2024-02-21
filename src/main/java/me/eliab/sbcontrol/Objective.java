package me.eliab.sbcontrol;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import me.eliab.sbcontrol.enums.Position;
import me.eliab.sbcontrol.enums.RenderType;
import me.eliab.sbcontrol.network.versions.Version;
import me.eliab.sbcontrol.numbers.NumberFormat;
import me.eliab.sbcontrol.util.ChatUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents an objective on a scoreboard. Objectives are used to track and display scores for entities.
 * <p><strong>Note:</strong> It can be safely used asynchronously as everything is at packet level.</p>
 */
public class Objective extends BoardComponent {

    private final String name;
    private String displayName;
    private RenderType renderType = RenderType.INTEGER;
    private NumberFormat numberFormat;
    private final Map<String, Score> scores = new HashMap<>();

    Objective(Board board, String name) {

        super(board);
        Preconditions.checkArgument(name != null, "Objective cannot be created with null name");
        checkLengthForVersion(Version.V1_20, name, 16, "Objective name cannot be larger than 16 characters");

        this.name = name;
        displayName = name;

        board.onObjectiveCreate(name, this, scores);

    }

    /**
     * Destroys this objective, removing it from the associated board. After calling this method
     * the objective becomes unusable and calling any method will throw {@code IllegalStateException}.
     * @throws IllegalStateException If the objective has already been destroyed.
     */
    @Override
    public synchronized void destroy() {
        checkState();
        board.onObjectiveRemove(name, this);
    }

    /**
     * Sets the display name of the objective.
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the string using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param displayName The new display name.
     * @throws IllegalArgumentException If the display name is null.
     *                                  If the display name is larger than 32 characters
     *                                  <strong>(only in 1.12 version)</strong>.
     * @throws IllegalStateException    If the objective has been destroyed.
     */
    public void setDisplayName(String displayName) {

        checkState();
        Preconditions.checkArgument(displayName != null, "Objective '" + name + "' cannot have null display name");
        checkLengthForVersion(Version.V1_12, displayName, 32, "Objective '" + name + "' cannot be larger than 32 characters");

        this.displayName = ChatUtils.setColors(displayName);
        board.onObjectiveUpdate(this);

    }

    /**
     * Sets the render type of the objective.
     *
     * @param renderType The render type to set.
     * @throws IllegalArgumentException If the render type is null.
     * @throws IllegalStateException    If the objective has been destroyed.
     */
    public void setRenderType(RenderType renderType) {

        checkState();
        Preconditions.checkArgument(renderType != null, "Objective '" + name + "' cannot have null render type");

        this.renderType = renderType;
        board.onObjectiveUpdate(this);

    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Sets the number format for the objective used by the scores.
     *
     * @param numberFormat The number format to set.
     * @throws UnsupportedOperationException If the server version does not support this.
     * @throws IllegalStateException         If the objective has been destroyed.
     */
    public void setNumberFormat(NumberFormat numberFormat) {

        checkState();
        checkVersion(Version.V1_20_3, "Objective '" + name + "' cannot have NumberFormat because it is not available for this version");

        this.numberFormat = numberFormat;
        board.onObjectiveUpdate(this);

    }

    /**
     * Sets the position of the objective on the scoreboard. If the position is null, the objective will be removed from
     * its current position.
     *
     * @param position The position to set.
     * @throws IllegalStateException If the objective has been destroyed.
     */
    public void setPosition(Position position) {

        checkState();

        if (position == null) {
            position = getPosition();
            if (position != null) {
                board.onObjectiveDisplay(position, null);
            }
        } else {
            board.onObjectiveDisplay(position, this);
        }

    }

    /**
     * Gets the score associated with the specified entity name. If the entity is not being tracked, a new Score instance
     * is created and returned.
     *
     * @param entityName The name of the entity.
     * @return The score for the entity.
     * @throws IllegalArgumentException If the entity name is null.
     *                                  If the entity name is larger than 40 characters
     *                                  <strong>(only in 1.20 or lower versions)</strong>.
     * @throws IllegalStateException    If the objective has been destroyed.
     */
    public Score getScore(String entityName) {

        checkState();
        Preconditions.checkArgument(entityName != null, "Objective '" + name + "' cannot track score for a null entity name");

        Score score = scores.get(entityName);
        if (score == null) {
            score = new Score(board, this, entityName);
        }
        return score;

    }

    /**
     * Resets all scores associated with this objective.
     * @throws IllegalStateException If the objective has been destroyed.
     */
    public void resetScores() {
        checkState();
        scores.values().forEach((Score::destroy));
    }

    /**
     * Checks if the objective is currently tracking scores for the specified entity name.
     *
     * @param entityName The name of the entity.
     * @return {@code true} if the entity is being tracked, {@code false} otherwise.
     * @throws IllegalArgumentException If the entity name is null.
     * @throws IllegalStateException    If the objective has been destroyed.
     */
    public boolean isTracking(String entityName) {
        checkState();
        Preconditions.checkArgument(entityName != null, "Objective '" + name + "' cannot track null entity");
        return scores.containsKey(entityName);
    }

    /**
     * Retrieves an immutable set of all scores being tracked by this objective.
     *
     * @return A set of the tracked scores.
     * @throws IllegalStateException If the objective has been destroyed.
     */
    public Set<Score> getScores() {
        checkState();
        return ImmutableSet.copyOf(scores.values());
    }

    /**
     * Retrieves the name of the objective.
     *
     * @return The name of the objective.
     * @throws IllegalStateException If the objective has been destroyed.
     */
    public String getName() {
        checkState();
        return name;
    }

    /**
     * Retrieves the render type of the objective.
     *
     * @return The render type of the objective.
     * @throws IllegalStateException If the objective has been destroyed.
     */
    public RenderType getRenderType() {
        checkState();
        return renderType;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Retrieves the display name of the objective.
     *
     * @return The display name of the objective.
     * @throws IllegalStateException If the objective has been destroyed.
     */
    public String getDisplayName() {
        checkState();
        return displayName;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Retrieves the number format of the objective used by the scores.
     *
     * @return The number format of the objective.
     * @throws UnsupportedOperationException If the server version does not support this.
     * @throws IllegalStateException         If the objective has been destroyed.
     */
    public NumberFormat getNumberFormat() {
        checkState();
        checkVersion(Version.V1_20_3, "Objective '" + name + "' cannot have NumberFormat because it is not available for this version");
        return numberFormat;
    }

    /**
     * Gets the position of the objective on the scoreboard or null if it is not being displayed.
     *
     * @return The position of the objective.
     * @throws IllegalStateException If the objective has been destroyed.
     */
    public Position getPosition() {

        checkState();

        for (Position position : Position.values()) {
            if (board.getObjectiveByPosition(position) == this) {
                return position;
            }
        }
        return null;

    }

    @Override
    protected void checkState() {
        if (board.getObjective(name) == null) {
            throw new IllegalStateException("Unregistered board component");
        }
    }

}
