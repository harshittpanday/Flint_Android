package net.kdt.pojavlaunch.profiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class ProfileSelectionTest {
    @Test
    public void keepsAvailableSelection() {
        assertEquals("profile-b", ProfileSelection.resolve(
                "profile-b", Arrays.asList("profile-a", "profile-b")));
    }

    @Test
    public void replacesMissingSelectionDeterministically() {
        assertEquals("profile-a", ProfileSelection.resolve(
                "deleted-profile", Arrays.asList("profile-c", "profile-a")));
    }

    @Test
    public void returnsNullWhenNoProfilesExist() {
        assertNull(ProfileSelection.resolve("missing", Collections.emptyList()));
    }
}
