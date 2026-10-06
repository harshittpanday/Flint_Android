package net.kdt.pojavlaunch.tasks;

/** Expected launch stop when a local account needs a file it is not allowed to download. */
public final class LocalAccountMissingFilesException extends Exception {
    private static final long serialVersionUID = 1L;

    public LocalAccountMissingFilesException(String fileName) {
        super(fileName);
    }
}
