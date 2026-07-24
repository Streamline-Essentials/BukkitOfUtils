package host.plas.bou.gui;

import org.bukkit.entity.Player;

/**
 * Configuration for lightweight {@link AbstractInventoryGui} instances.
 */
public final class GuiConfig {
    private final Player player;
    private final CornerColor cornerColor;
    private final Integer backSlot;
    private final String balanceText;

    public GuiConfig(Player player, CornerColor cornerColor, Integer backSlot, String balanceText) {
        this.player = player;
        this.cornerColor = cornerColor;
        this.backSlot = backSlot;
        this.balanceText = balanceText;
    }

    public static Builder builder(Player player) {
        return new Builder(player);
    }

    public Player player() {
        return player;
    }

    public CornerColor cornerColor() {
        return cornerColor;
    }

    public Integer backSlot() {
        return backSlot;
    }

    public String balanceText() {
        return balanceText;
    }

    public int resolveBackSlot(int size) {
        return backSlot != null ? backSlot : GuiLayout.defaultBackSlot(size);
    }

    public static final class Builder {
        private final Player player;
        private CornerColor cornerColor = CornerColor.YELLOW;
        private Integer backSlot;
        private String balanceText;

        private Builder(Player player) {
            this.player = player;
        }

        public Builder cornerColor(CornerColor cornerColor) {
            this.cornerColor = cornerColor;
            return this;
        }

        public Builder cornerColorFromTitle(String title) {
            this.cornerColor = CornerColor.fromDisplayText(title);
            return this;
        }

        public Builder backSlot(int backSlot) {
            this.backSlot = backSlot;
            return this;
        }

        public Builder balanceText(String balanceText) {
            this.balanceText = balanceText;
            return this;
        }

        public GuiConfig build() {
            return new GuiConfig(this.player, this.cornerColor, this.backSlot, this.balanceText);
        }
    }
}
