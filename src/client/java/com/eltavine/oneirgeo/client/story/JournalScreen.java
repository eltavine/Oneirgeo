package com.eltavine.oneirgeo.client.story;

import com.eltavine.oneirgeo.registry.OneirgeoAttachments;
import com.eltavine.oneirgeo.story.Chapter;
import com.eltavine.oneirgeo.story.Story;
import com.eltavine.oneirgeo.story.StoryProgress;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

/**
 * The dream journal, shown in the vanilla book: a contents page, then every chapter with what has
 * come back and gaps for what has not, and once everything is remembered, the last page.
 */
public final class JournalScreen {
    private static final int WIDTH = 114;
    private static final int LINES = 14;

    private JournalScreen() {
    }

    public static void open() {
        open(null);
    }

    /** Opens the journal, at the first page of {@code focus} when given. */
    public static void open(@Nullable Chapter focus) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        StoryProgress progress = minecraft.player.getAttachedOrElse(OneirgeoAttachments.STORY, StoryProgress.EMPTY);
        int[] firstPages = new int[Chapter.values().length];
        BookViewScreen screen = new BookViewScreen(new BookViewScreen.BookAccess(pages(minecraft.font, progress, firstPages)));
        minecraft.gui.setScreen(screen);
        if (focus != null) {
            screen.setPage(firstPages[focus.ordinal()]);
        }
    }

    static List<Component> pages(Font font, StoryProgress progress, int[] firstPages) {
        List<Component> pages = new ArrayList<>();
        MutableComponent contents = Component.empty()
                .append(Component.translatable("oneirgeo.journal.title").withStyle(ChatFormatting.BOLD))
                .append("\n\n")
                .append(Component.translatable("oneirgeo.journal.intro").withStyle(ChatFormatting.DARK_GRAY))
                .append("\n");
        for (Chapter chapter : Chapter.values()) {
            contents.append("\n").append(Component.translatable("oneirgeo.journal.contents", Component.translatable(chapter.titleKey()),
                    progress.remembered(chapter), Chapter.ENTRIES));
        }
        pages.add(contents);
        for (Chapter chapter : Chapter.values()) {
            firstPages[chapter.ordinal()] = pages.size();
            Page page = new Page(font, pages);
            page.add(Component.translatable(chapter.titleKey()).withStyle(ChatFormatting.BOLD));
            for (int i = 0; i < Chapter.ENTRIES; i++) {
                page.add(i < progress.remembered(chapter)
                        ? Story.entry(chapter, i, progress)
                        : Component.literal("· · ·").withStyle(ChatFormatting.GRAY));
            }
            page.flush();
        }
        if (progress.allComplete()) {
            Page page = new Page(font, pages);
            page.add(Component.translatable("oneirgeo.journal.last.0", java.time.Year.now().getValue() - 1997));
            page.add(Component.translatable("oneirgeo.journal.last.1"));
            page.add(Component.translatable("oneirgeo.journal.last.2").withStyle(ChatFormatting.DARK_GRAY));
            page.flush();
        }
        return pages;
    }

    /** Fills book pages with paragraphs, starting a new page when the next one would not fit. */
    private static final class Page {
        private final Font font;
        private final List<Component> pages;
        private MutableComponent text = Component.empty();
        private int lines;

        Page(Font font, List<Component> pages) {
            this.font = font;
            this.pages = pages;
        }

        void add(Component paragraph) {
            int height = this.font.split(paragraph, WIDTH).size();
            int needed = this.lines == 0 ? height : height + 1;
            if (this.lines > 0 && this.lines + needed > LINES) {
                this.flush();
                needed = height;
            }
            if (this.lines > 0) {
                this.text.append("\n\n");
            }
            this.text.append(paragraph);
            this.lines += needed;
        }

        void flush() {
            if (this.lines > 0) {
                this.pages.add(this.text);
            }
            this.text = Component.empty();
            this.lines = 0;
        }
    }
}
