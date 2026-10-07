package com.eltavine.oneirgeo.client.datagen;

import com.google.common.hash.Hashing;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/** Writes the procedural placeholder textures and the mod icon. */
final class TextureProvider implements DataProvider {
    private final FabricPackOutput output;

    TextureProvider(FabricPackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path assets = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(this.output.getModId());
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<String, Consumer<TextureArt.Canvas>> entry : TextureArt.BLOCKS.entrySet()) {
            futures.add(write(cache, assets.resolve("textures/block/" + entry.getKey() + ".png"), entry.getKey(), 16, entry.getValue()));
        }
        for (Map.Entry<String, Consumer<TextureArt.Canvas>> entry : TextureArt.ITEMS.entrySet()) {
            futures.add(write(cache, assets.resolve("textures/item/" + entry.getKey() + ".png"), entry.getKey(), 16, entry.getValue()));
        }
        for (Map.Entry<String, TextureArt.Sized> entry : TextureArt.EXTRA.entrySet()) {
            futures.add(write(cache, assets.resolve(entry.getKey() + ".png"), entry.getKey(), entry.getValue().size(), entry.getValue().painter()));
        }
        futures.add(write(cache, assets.resolve("icon.png"), "icon", 128, TextureArt::icon));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private static CompletableFuture<?> write(CachedOutput cache, Path path, String name, int size, Consumer<TextureArt.Canvas> painter) {
        return CompletableFuture.runAsync(() -> {
            TextureArt.Canvas canvas = new TextureArt.Canvas(name, size);
            painter.accept(canvas);
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ImageIO.write(canvas.image, "png", bytes);
                byte[] data = bytes.toByteArray();
                cache.writeIfNeeded(path, data, Hashing.sha1().hashBytes(data));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public String getName() {
        return "Oneirgeo textures";
    }
}
