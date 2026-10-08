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

    @Test
    public void acceptsSemanticallyIdenticalJsonFormatting() {
        String original = "{\"id\":\"1.21.11\",\"downloads\":{\"client\":{\"sha1\":\"abc\"}}}";
        String formatted = "{ \"downloads\": { \"client\": { \"sha1\": \"abc\" } },\n"
                + "  \"id\": \"1.21.11\" }";
        org.junit.Assert.assertTrue(
                MinecraftInstallationPreflight.jsonSemanticallyEquals(original, formatted));
    }

    @Test
    public void rejectsSemanticallyChangedJson() {
        String trusted = "{\"id\":\"1.21.11\",\"clientSha1\":\"trusted\"}";
        String changed = "{\"id\":\"1.21.11\",\"clientSha1\":\"changed\"}";
        org.junit.Assert.assertFalse(
                MinecraftInstallationPreflight.jsonSemanticallyEquals(trusted, changed));
    }

    @Test
    public void verifiesTrustedReferenceHash() throws Exception {
        assertEquals(true, MinecraftInstallationPreflight.hasSha1(
                "trusted metadata", "c31b32927cf9c9fd7baa4e040eec203c76f3c935"));
    }

    @Test
    public void buildsOnlySafeOfficialPackageUrls() {
        assertEquals(
                "https://piston-meta.mojang.com/v1/packages/"
                        + "4f6bd9388f12e9d7adc2ded64acba66212d60521/1.21.11.json",
                MinecraftInstallationPreflight.officialVersionPackageUrl(
                        "1.21.11", "4f6bd9388f12e9d7adc2ded64acba66212d60521"));
        assertNull(MinecraftInstallationPreflight.officialVersionPackageUrl(
                "../other", "4f6bd9388f12e9d7adc2ded64acba66212d60521"));
        assertNull(MinecraftInstallationPreflight.officialVersionPackageUrl(
                "1.21.11", "not-a-sha1"));
    }

    @Test
    public void acceptsOnlyExactOfficialRepresentationForVersion() throws Exception {
        String official = "{\"id\":\"1.21.11\",\"assetIndex\":{\"sha1\":\"old-official\"}}";
        String hash = MinecraftInstallationPreflight.sha1(official);
        assertEquals(true, MinecraftInstallationPreflight.isOfficialVersionRepresentation(
                official, official, "1.21.11", hash));
        assertEquals(false, MinecraftInstallationPreflight.isOfficialVersionRepresentation(
                official.replace("old-official", "modified"), official, "1.21.11", hash));
        assertEquals(false, MinecraftInstallationPreflight.isOfficialVersionRepresentation(
                official, official, "1.21.10", hash));
    }

    private File write(String name, String contents) throws Exception {
        File file = temporaryFolder.newFile(name);
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(contents.getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }
}
