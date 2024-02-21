package me.eliab.sbcontrol;

import com.google.common.base.Preconditions;
import me.eliab.sbcontrol.network.versions.Version;
import me.eliab.sbcontrol.numbers.NumberFormat;
import me.eliab.sbcontrol.util.ChatUtils;

/**
 * Represents a score associated with a specific entity and objective on a scoreboard.
 * <p><strong>Note:</strong> It can be safely used asynchronously as everything is at packet level.</p>
 */
public class Score extends BoardComponent {

    private final Objective objective;
    private final String entityName;
    private int score;
    private String displayName;
    private NumberFormat numberFormat;

    Score(Board board, Objective objective, String entityName) {

        super(board);

        Preconditions.checkArgument(objective != null, "Score cannot be created with null objective");
        Preconditions.checkArgument(entityName != null, "Score cannot be created with null entity name");
        checkLengthForVersion(Version.V1_20, entityName, 40, "Score entity name cannot be longer than 40 characters");

        this.objective = objective;
        this.entityName = entityName;
        board.onScoreCreate(objective, entityName, this);

    }

    /**
     * Destroys the score, removing it from the associated scoreboard. After calling this method
     * the score becomes unusable and calling any method will throw {@code IllegalStateException}.
     *
     * @throws IllegalStateException If the score has already been destroyed.
     */
    @Override
    public synchronized void destroy() {
        checkState();
        board.onScoreRemove(objective, entityName, this);
    }

    /**
     * Sets the score value for the entity
     *
     * @param score The new score value.
     * @throws IllegalStateException If the score has been destroyed.
     */
    public void setScore(int score) {
        checkState();
        this.score = score;
        board.onScoreUpdate(this);
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Sets the display name for the score.
     *
     * <p>
     * It automatically converts the color codes and hex colors, found in the string using the color code '&amp;',
     * to its chat format.
     * </p>
     *
     * @param displayName The new display name.
     * @throws UnsupportedOperationException If the server version does not support this.
     * @throws IllegalStateException         If the score has been destroyed.
     */
    public void setDisplayName(String displayName) {

        checkState();
        checkVersion(Version.V1_20_3, "Score '" + entityName + "' cannot have display name because it is not available for this version");

        this.displayName = ChatUtils.setColors(displayName);
        board.onScoreUpdate(this);

    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Sets the number format for this score.
     *
     * @param numberFormat The new number format.
     * @throws UnsupportedOperationException If the server version does not support this.
     * @throws IllegalStateException         If the score has been destroyed.
     */
    public void setNumberFormat(NumberFormat numberFormat) {

        checkState();
        checkVersion(Version.V1_20_3, "Score '" + entityName + "' cannot have NumberFormat because it is not available for this version");

        this.numberFormat = numberFormat;
        board.onScoreUpdate(this);

    }

    /**
     * Retrieves the name of the entity for which the score is tracked.
     *
     * @return The entity name.
     * @throws IllegalStateException If the score has been destroyed.
     */
    public String getEntityName() {
        checkState();
        return entityName;
    }

    /**
     * Retrieves the objective to which the score belongs.
     *
     * @return The associated objective.
     * @throws IllegalStateException If the score has been destroyed.
     */
    public Objective getObjective() {
        checkState();
        return objective;
    }

    /**
     * Retrieves the current score value.
     *
     * @return The score value.
     * @throws IllegalStateException If the score has been destroyed.
     */
    public int getScore() {
        checkState();
        return score;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Retrieves the display name for the score
     *
     * @return The display name.
     * @throws UnsupportedOperationException If the server version does not support this.
     * @throws IllegalStateException         If the score has been destroyed.
     */
    public String getDisplayName() {
        checkState();
        checkVersion(Version.V1_20_3, "Score '" + entityName + "' cannot have display name because it is not available for this version");
        return displayName;
    }

    /**
     * <p><strong>(Available in 1.20.3 or greater versions)</strong></p>
     * Retrieves the number format for the score.
     *
     * @return The number format.
     * @throws UnsupportedOperationException If the server version does not support this.
     * @throws IllegalStateException         If the score has been destroyed.
     */
    public NumberFormat getNumberFormat() {
        checkState();
        checkVersion(Version.V1_20_3, "Score '" + entityName + "' cannot have NumberFormat because it is not available for this version");
        return numberFormat;
    }

    @Override
    protected void checkState() {
        if (!objective.isTracking(entityName)) {
            throw new IllegalStateException("Untracked entity score");
        }
    }

}
