package net.kdt.pojavlaunch.tasks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Pure file-integrity checks used before a local-account launch. */
final class MinecraftInstallationPreflight {
    private static final String OFFICIAL_VERSION_PACKAGE_BASE =
            "https://piston-meta.mojang.com/v1/packages/";
    private static final String SHA1_PATTERN = "[0-9a-fA-F]{40}";
    private static final String VERSION_ID_PATTERN = "[A-Za-z0-9._-]+";

    enum Failure {
        MISSING,
        NOT_A_FILE,
        UNREADABLE,
        HASH_MISMATCH
    }

    private MinecraftInstallationPreflight() {}

    static Failure verify(File file, String expectedSha1) throws IOException {
        if (!file.exists()) return Failure.MISSING;
        if (!file.isFile()) return Failure.NOT_A_FILE;
        if (!file.canRead()) return Failure.UNREADABLE;
        if (expectedSha1 == null || expectedSha1.trim().isEmpty()) return null;
        return expectedSha1.equalsIgnoreCase(sha1(file)) ? null : Failure.HASH_MISMATCH;
    }

    static String sha1(File file) throws IOException {
        MessageDigest digest = newSha1Digest();
        byte[] buffer = new byte[32768];
        try (FileInputStream input = new FileInputStream(file)) {
            int count;
            while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        return hex(digest.digest());
    }

    static boolean hasSha1(String content, String expectedSha1) throws IOException {
        return expectedSha1.equalsIgnoreCase(sha1(content));
    }

    static String sha1(String content) throws IOException {
        MessageDigest digest = newSha1Digest();
        digest.update(content.getBytes(StandardCharsets.UTF_8));
        return hex(digest.digest());
    }

    static boolean jsonSemanticallyEquals(String first, String second) {
        try {
            Gson gson = new Gson();
            JsonElement firstJson = gson.fromJson(first, JsonElement.class);
            JsonElement secondJson = gson.fromJson(second, JsonElement.class);
            return firstJson != null && firstJson.equals(secondJson);
        } catch (RuntimeException invalidJson) {
            return false;
        }
    }

    static String officialVersionPackageUrl(String versionId, String sha1) {
        if (versionId == null || !versionId.matches(VERSION_ID_PATTERN)
                || sha1 == null || !sha1.matches(SHA1_PATTERN)) return null;
        return OFFICIAL_VERSION_PACKAGE_BASE + sha1.toLowerCase() + "/" + versionId + ".json";
    }

    static boolean isOfficialVersionRepresentation(String installedMetadata,
                                                   String officialMetadata,
                                                   String versionId,
                                                   String installedSha1) throws IOException {
        if (!hasSha1(officialMetadata, installedSha1)
                || !jsonSemanticallyEquals(installedMetadata, officialMetadata)) return false;
        try {
            Gson gson = new Gson();
            JsonElement root = gson.fromJson(officialMetadata, JsonElement.class);
            if (root == null || !root.isJsonObject() || !root.getAsJsonObject().has("id")) return false;
            return versionId.equals(root.getAsJsonObject().get("id").getAsString());
        } catch (RuntimeException invalidJson) {
            return false;
        }
    }

    private static MessageDigest newSha1Digest() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IOException("SHA-1 is unavailable", impossible);
        }
    }

    private static String hex(byte[] digest) {
        StringBuilder result = new StringBuilder(40);
        for (byte value : digest) result.append(String.format("%02x", value & 0xff));
        return result.toString();
    }
}
