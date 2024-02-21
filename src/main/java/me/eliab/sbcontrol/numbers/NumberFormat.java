package me.eliab.sbcontrol.numbers;

import dev.dewy.nbt.api.Tag;
import me.eliab.sbcontrol.enums.NumberFormatType;

/**
 * Represents a strategy for formatting score numbers.
 * Implementations of this interface define how a score number should be formatted.
 */
public interface NumberFormat {

    /**
     * Retrieves the NBT format tag associated with this number format.
     * @return The NBT format tag for this number format.
     */
    Tag getFormat();

    /**
     * Retrieves the type of this number format.
     * @return The type of the number format.
     */
    NumberFormatType getType();

}
