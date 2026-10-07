package net.kdt.pojavlaunch;

import java.io.IOException;

/** An actionable failure while selecting, unpacking, or validating a bundled Java runtime. */
public class RuntimeInstallationException extends IOException {
    public RuntimeInstallationException(String message) {
        super(message);
    }

    public RuntimeInstallationException(String message, Throwable cause) {
        super(message, cause);
    }
}
