package com.dudal.javachat.protocol;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

import java.util.Set;

final class SystemChatFilter {
    private static final Set<String> HIDDEN_TRANSLATION_KEYS = Set.of(
            "multiplayer.player.joined",
            "multiplayer.player.joined.renamed",
            "multiplayer.player.left"
    );

    private SystemChatFilter() {}

    static boolean shouldDisplay(Component component) {
        if (component == null || containsHiddenTranslation(component)) {
            return false;
        }
        return !HIDDEN_TRANSLATION_KEYS.contains(ComponentText.plain(component).trim());
    }

    private static boolean containsHiddenTranslation(Component component) {
        if (component instanceof TranslatableComponent translatable
                && HIDDEN_TRANSLATION_KEYS.contains(translatable.key())) {
            return true;
        }
        for (Component child : component.children()) {
            if (containsHiddenTranslation(child)) {
                return true;
            }
        }
        return false;
    }
}
