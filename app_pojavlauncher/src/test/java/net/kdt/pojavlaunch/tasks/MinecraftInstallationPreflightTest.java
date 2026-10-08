package net.kdt.pojavlaunch.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class MinecraftInstallationPreflightTest {
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void reportsMissingFile() throws Exception {
        File missing = new File(temporaryFolder.getRoot(), "missing.jar");
        assertEquals(MinecraftInstallationPreflight.Failure.MISSING,
                MinecraftInstallationPreflight.verify(missing, null));
    }

    @Test
    public void acceptsReadableFileWhenMetadataHasNoHash() throws Exception {
        File file = write("legacy.jar", "legitimate legacy library");
        assertNull(MinecraftInstallationPreflight.verify(file, null));
    }

    @Test
    public void verifiesKnownHash() throws Exception {
        File file = write("client.jar", "minecraft client");
        assertNull(MinecraftInstallationPreflight.verify(file,
                "d3ca8e155ff221f475ad4cece53d4265eb6f2be9"));
    }

    @Test
    public void reportsHashMismatch() throws Exception {
        File file = write("asset", "corrupt");
        assertEquals(MinecraftInstallationPreflight.Failure.HASH_MISMATCH,
                MinecraftInstallationPreflight.verify(file,
                        "0000000000000000000000000000000000000000"));
    }

    private File write(String name, String contents) throws Exception {
        File file = temporaryFolder.newFile(name);
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(contents.getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }
}
