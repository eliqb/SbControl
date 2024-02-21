package me.eliab.sbcontrol.util;

import com.google.common.base.Preconditions;
import dev.dewy.nbt.api.Tag;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import me.eliab.sbcontrol.SbControl;
import me.eliab.sbcontrol.network.versions.Version;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with Minecraft chat system, providing methods for
 * color formatting, handling legacy text to NBT conversion, and more.
 */
public class ChatUtils {

    private static final Version VERSION = SbControl.getVersion();
    private static final Pattern HEX_FORMAT_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final Pattern HEX_CHAT_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");
    private static final ChatColor[] COLORS = ChatColor.values();

    private static final String TAG_TEXT = "text";
    private static final String TAG_COLOR = "color";
    private static final String TAG_OBFUSCATED = "obfuscated";
    private static final String TAG_BOLD = "bold";
    private static final String TAG_STRIKETHROUGH = "strikethrough";
    private static final String TAG_UNDERLINED = "underlined";
    private static final String TAG_ITALIC = "italic";
    private static final String TAG_EXTRA = "extra";

    private ChatUtils() {
        throw new UnsupportedOperationException("ChatUtils is a utility class therefore it cannot be instantiated");
    }

    /**
     * Translates color codes and HexColors in the input text and returns the formatted string.
     *
     * <p>This method supports HexColors (e.g., "&amp;#abc123") on Minecraft versions 1.16 and above.</p>
     *
     * @param text The input text containing color codes and HexColors.
     * @return The formatted string with translated color codes.
     * @throws IllegalArgumentException If the provided text is null.
     */
    public static String setColors(String text) {

        Preconditions.checkArgument(text != null, "ChatUtils cannot apply color formats to a null String");

        if (VERSION.isHigherOrEqualThan(Version.V1_16)) {
            for (Matcher match = HEX_CHAT_PATTERN.matcher(text); match.find(); match = HEX_CHAT_PATTERN.matcher(text)) {
                String color = text.substring(match.start(), match.end());
                text = text.replace(color, net.md_5.bungee.api.ChatColor.of(color.substring(1)).toString());
            }
        }
        return ChatColor.translateAlternateColorCodes('&', text);

    }

    /**
     * Checks if the provided string is a valid HexColor (e.g., "#abc123").
     *
     * @param hexColor The string to check for HexColor validity.
     * @return {@code true} if the string is a valid HexColor, {@code false} otherwise.
     */
    public static boolean isValidHexColor(String hexColor) {
        return HEX_FORMAT_PATTERN.matcher(hexColor).find();
    }

    /**
     * Retrieves a ChatColor based on its ordinal value.
     *
     * @param ordinal The ordinal value of the desired ChatColor.
     * @return The corresponding ChatColor.
     */
    public static ChatColor colorByOrdinal(int ordinal) {
        return COLORS[ordinal];
    }

    /**
     * Creates a CompoundTag representing various properties of a chat component.
     *
     * @param text          The text content of the chat component.
     * @param color         The color of the chat component.
     * @param obfuscated    Whether the chat component is obfuscated.
     * @param bold          Whether the chat component is bold.
     * @param strikethrough Whether the chat component has strikethrough.
     * @param underlined    Whether the chat component is underlined.
     * @param italic        Whether the chat component is italic.
     *
     * @return The CompoundTag representing the chat component.
     */
    public static CompoundTag compoundTagOf(String text,
                                            String color,
                                            boolean obfuscated,
                                            boolean bold,
                                            boolean strikethrough,
                                            boolean underlined,
                                            boolean italic) {

        CompoundTag compoundTag = new CompoundTag();

        if (text != null) compoundTag.putString(TAG_TEXT, text);
        if (color != null) compoundTag.putString(TAG_COLOR, color);
        if (obfuscated) compoundTag.putByte(TAG_OBFUSCATED, (byte) 1);
        if (bold) compoundTag.putByte(TAG_BOLD, (byte) 1);
        if (strikethrough) compoundTag.putByte(TAG_STRIKETHROUGH, (byte) 1);
        if (underlined) compoundTag.putByte(TAG_UNDERLINED, (byte) 1);
        if (italic) compoundTag.putByte(TAG_ITALIC, (byte) 1);

        return compoundTag;

    }

    /**
     * Converts legacy text containing color codes to NBT format.
     *
     * @param text The legacy text containing color codes.
     * @return The NBT Tag representing the converted text.
     * @throws IllegalArgumentException If the provided text is null.
     */
    public static Tag nbtFromLegacyText(String text) {

        Preconditions.checkArgument(text != null, "ChatUtils cannot convert null String into NBT format");

        CompoundTag root = new CompoundTag();
        root.putString(TAG_TEXT, "");

        if (text.isEmpty()) {
            return root;
        }

        char[] array = text.toCharArray();
        StringBuilder builder = new StringBuilder();

        String lastColor = null;
        boolean obfuscated = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underlined = false;
        boolean italic = false;

        ListTag<CompoundTag> extra = new ListTag<>(TAG_EXTRA);

        for (int i = 0; i < array.length; i++) {

            char c = array[i];

            if (c == ChatColor.COLOR_CHAR) {

                if (++i >= array.length) {
                    break;
                }

                c = array[i];

                if (c >= 'A' && c <= 'Z') {
                    c += 32; // char to lowerCase
                }

                if (c == 'x' && i + 12 < array.length) {

                    // looking for HexColors
                    StringBuilder hexBuilder = new StringBuilder("#");

                    for (int j = 0; j < 6; j++) {
                        hexBuilder.append(array[ i + 2 + (j * 2) ]);
                    }

                    String hexColor = hexBuilder.toString();

                    if (ChatUtils.isValidHexColor(hexColor)) {

                        if (builder.length() > 0) {
                            extra.add(compoundTagOf(builder.toString(), lastColor, obfuscated, bold, strikethrough, underlined, italic));
                            builder = new StringBuilder();
                        }

                        lastColor = hexColor;
                        obfuscated = bold = strikethrough = underlined = italic = false;
                        i += 12;
                        continue;

                    }

                }

                // looking for ChatColors
                ChatColor format = ChatColor.getByChar(c);

                if (format == null) {
                    continue;
                }

                if (builder.length() > 0) {
                    extra.add(compoundTagOf(builder.toString(), lastColor, obfuscated, bold, strikethrough, underlined, italic));
                    builder = new StringBuilder();
                }

                if (format.isFormat()) {

                    switch (format) {
                        case MAGIC: if (!obfuscated) obfuscated = true; break;
                        case BOLD: if (!bold) bold = true; break;
                        case STRIKETHROUGH: if (!strikethrough) strikethrough = true; break;
                        case UNDERLINE: if (!underlined) underlined = true; break;
                        case ITALIC: if (!italic) italic = true; break;
                    }

                } else {
                    if (format.isColor()) lastColor = format.name().toLowerCase();
                    obfuscated = bold = strikethrough = underlined = italic = false;
                }

            } else {
                builder.append(c);
            }

        }

        if (builder.length() > 0) {
            extra.add(compoundTagOf(builder.toString(), lastColor, obfuscated, bold, strikethrough, underlined, italic));
        }

        root.put(extra);
        return root;

    }

}
