package me.eliab.sbcontrol.enums;

/**
 * The render type in which the scores will be displayed
 */
public enum RenderType {

    INTEGER("integer"),
    HEARTS("hearts");

    private final String value;

    RenderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
