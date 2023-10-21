package fr.raksrinana.fallingtree.common.tree;

import fr.raksrinana.fallingtree.common.config.enums.BreakMode;
import fr.raksrinana.fallingtree.common.tree.exception.*;
import org.jetbrains.annotations.NotNull;

public record BreakTreeResult(boolean shouldCancel, @NotNull BreakMode breakMode) {
    // C-style enum
    public static final int FLAG_CANCELLED = 1 << 31;
    public static final int FLAG_ERROR = 1 << 29;

    public static final int ERROR_NOT_SERVER = 0;
    public static final int ERROR_NOT_ENABLED = 1;
    public static final int ERROR_ABSENT_TOOL = 2;
    public static final int ERROR_WRONG_PLAYER_STATE = 3;
    public static final int ERROR_ABSENT_TREE = 4;
    public static final int ERROR_TREE_TOO_LARGE = 5;

    public static BreakTreeResult decode(int resultCode) throws TreeBreakingNotEnabledException, PlayerNotInRightState, ToolUseForcedException, TreeBreakingException, NoTreeFoundException, NotServerException {
        if ((resultCode & FLAG_ERROR) == 0) {
            return new BreakTreeResult((resultCode & FLAG_CANCELLED) == 0, BreakMode.ofOrdinal(resultCode & ~FLAG_CANCELLED));
        } else {
            switch (resultCode & ~(FLAG_CANCELLED | FLAG_ERROR)) {
                case ERROR_NOT_SERVER -> throw new NotServerException();
                case ERROR_NOT_ENABLED -> throw new TreeBreakingNotEnabledException();
                case ERROR_ABSENT_TOOL -> throw new ToolUseForcedException();
                case ERROR_WRONG_PLAYER_STATE -> throw new PlayerNotInRightState();
                case ERROR_ABSENT_TREE -> throw new NoTreeFoundException();
                case ERROR_TREE_TOO_LARGE -> throw new TreeBreakingException();
                default -> throw new AssertionError();
            }
        }
    }
}
