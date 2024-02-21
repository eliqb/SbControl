package me.eliab.sbcontrol.numbers;

import com.google.common.base.Preconditions;
import dev.dewy.nbt.api.Tag;
import me.eliab.sbcontrol.enums.NumberFormatType;
import me.eliab.sbcontrol.util.ChatUtils;
import org.bukkit.ChatColor;

/**
 * A number format type that will provide a style to use when formatting the score number.
 * This is similar to a Chat, but only the styling fields are present.
 *
 * <p>
 * To create a new StyleFormat use {@link StyledFormat.Builder}
 * </p>
 */
public final class StyledFormat implements NumberFormat {

    private final String color;
    private final boolean obfuscated;
    private final boolean bold;
    private final boolean strikethrough;
    private final boolean underlined;
    private final boolean italic;
    private final Tag style;

    private StyledFormat(String color, boolean obfuscated, boolean bold, boolean strikethrough, boolean underlined, boolean italic) {
        this.color = color;
        this.obfuscated = obfuscated;
        this.bold = bold;
        this.strikethrough = strikethrough;
        this.underlined = underlined;
        this.italic = italic;
        style = ChatUtils.compoundTagOf(null, color, obfuscated, bold, strikethrough, underlined, italic);
    }

    public String getColor() {
        return color;
    }

    public Boolean isObfuscated() {
        return obfuscated;
    }

    public Boolean isBold() {
        return bold;
    }

    public Boolean isItalic() {
        return italic;
    }

    public Boolean isUnderlined() {
        return underlined;
    }

    public Boolean isStrikethrough() {
        return strikethrough;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag getFormat() {
        return style;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NumberFormatType getType() {
        return NumberFormatType.STYLED;
    }

    /**
     * StyledFormat builder
     */
    public static class Builder {

        private String color = "white";
        private boolean bold = false;
        private boolean italic = false;
        private boolean underlined = false;
        private boolean strikethrough = false;
        private boolean obfuscated = false;

        public Builder setColor(ChatColor color) {
            Preconditions.checkArgument(color != null, "StyledFormat cannot have null color");
            this.color = color.name().toLowerCase();
            return this;
        }

        public Builder setColor(String hexColor) {
            Preconditions.checkArgument(color != null, "StyledFormat cannot have null hex color");
            Preconditions.checkArgument(ChatUtils.isValidHexColor(hexColor));
            this.color = hexColor;
            return this;
        }

        public Builder setObfuscated(boolean obfuscated) {
            this.obfuscated = obfuscated;
            return this;
        }

        public Builder setBold(boolean bold) {
            this.bold = bold;
            return this;
        }

        public Builder setStrikethrough(boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }

        public Builder setUnderlined(boolean underlined) {
            this.underlined = underlined;
            return this;
        }

        public Builder setItalic(boolean italic) {
            this.italic = italic;
            return this;
        }

        public StyledFormat build() {
            return new StyledFormat(color, obfuscated, bold, strikethrough, underlined, italic);
        }

    }

}
