package me.eliab.sbcontrol.numbers;

import com.google.common.base.Preconditions;
import dev.dewy.nbt.api.Tag;
import me.eliab.sbcontrol.enums.NumberFormatType;
import me.eliab.sbcontrol.util.ChatUtils;

/**
 * A number format type that will display a given text as placeholder for the score.
 */
public final class FixedFormat implements NumberFormat {

    private final String text;
    private final Tag format;

    /**
     * Creates a new fixed format with the given text as placeholder.
     *
     * @param text The text to be displayed.
     * @throws IllegalArgumentException If the provided text is null.
     */
    public FixedFormat(String text) {
        Preconditions.checkArgument(text != null, "FixedFormat cannot have null text");
        this.text = ChatUtils.setColors(text);
        format = ChatUtils.nbtFromLegacyText(this.text);
    }

    /**
     * Returns the given text that is used as placeholder.
     * @return The given text.
     */
    public String getText() {
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag getFormat() {
        return format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NumberFormatType getType() {
        return NumberFormatType.FIXED;
    }

}
