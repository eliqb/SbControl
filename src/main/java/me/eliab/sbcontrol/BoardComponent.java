package me.eliab.sbcontrol;

import com.google.common.base.Preconditions;
import me.eliab.sbcontrol.network.versions.Version;

/**
 * Represents a component of the {@link Board} system.
 * This is an abstract class serving as the base for various components within the scoreboard system.
 */
abstract class BoardComponent {

    static final Version VERSION = SbControl.getVersion();

    final Board board;

    /**
     * Constructs a new {@code BoardComponent} with the specified {@link Board}.
     * @param board The associated {@link Board} instance.
     */
    BoardComponent(Board board) {
        Preconditions.checkArgument(board != null, "BoardComponent cannot be created with null board");
        this.board = board;
    }

    /**
     * Gets the associated {@link Board} instance.
     * @return The {@link Board} associated with this board component.
     * @throws IllegalStateException If the board component has been destroyed.
     */
    public Board getBoard() {
        checkState();
        return board;
    }

    /**
     * Destroys the board component, performing necessary cleanup actions.
     * Actual implementation details are left to subclasses.
     * @throws IllegalStateException If the board component has already been destroyed.
     */
    public abstract void destroy();

    abstract void checkState();

    static void checkLengthForVersion(Version maxVersion, String value, int maxLength, String errorMessage) {
        if (VERSION.isLowerOrEqualThan(maxVersion) && value.length() >= maxLength) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    static void checkVersion(Version version, String errorMessage) {
        if (!VERSION.isHigherOrEqualThan(version)) {
            throw new UnsupportedOperationException(errorMessage);
        }
    }

}
