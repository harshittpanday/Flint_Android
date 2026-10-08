package net.kdt.pojavlaunch.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Pure file-integrity checks used before a local-account launch. */
final class MinecraftInstallationPreflight {
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

    private static String sha1(File file) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IOException("SHA-1 is unavailable", impossible);
        }
        byte[] buffer = new byte[32768];
        try (FileInputStream input = new FileInputStream(file)) {
            int count;
            while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        StringBuilder result = new StringBuilder(40);
        for (byte value : digest.digest()) result.append(String.format("%02x", value & 0xff));
        return result.toString();
    }
}
