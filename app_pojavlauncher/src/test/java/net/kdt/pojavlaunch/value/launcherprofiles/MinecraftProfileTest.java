package net.kdt.pojavlaunch.value.launcherprofiles;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MinecraftProfileTest {
    @Test
    public void copyKeepsPerProfileConfiguration() {
        MinecraftProfile source = MinecraftProfile.createTemplate();
        source.pojavRendererName = "opengles3";
        source.ramAllocation = 3072;
        source.controlFile = "controls/flint.json";

        MinecraftProfile copy = new MinecraftProfile(source);

        assertEquals("opengles3", copy.pojavRendererName);
        assertEquals(Integer.valueOf(3072), copy.ramAllocation);
        assertEquals("controls/flint.json", copy.controlFile);
    }
}
