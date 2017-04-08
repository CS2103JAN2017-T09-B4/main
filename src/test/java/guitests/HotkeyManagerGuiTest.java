//@@author A0139925U
package guitests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import guitests.guihandles.MainGuiHandle;

public class HotkeyManagerGuiTest extends TaskManagerGuiTest {

    @Test
    public void toggleWindowFocus() {
        assertMainGuiOpen(mainGui.toggleMainGuiUsingCtrlAltDAccelerator());
        assertMainGuiOpen(mainGui.toggleMainGuiUsingCtrlQAccelerator());
        assertMainGuiHidden(mainGui.toggleMainGuiUsingCtrlAltDAccelerator());
        assertMainGuiOpen(mainGui.toggleMainGuiUsingCtrlAltDAccelerator());
    }

    private void assertMainGuiOpen(MainGuiHandle mainGuiHandle) {
        assertTrue(mainGuiHandle.getPrimaryStage().isIconified());
    }

    private void assertMainGuiHidden(MainGuiHandle mainGuiHandle) {
        assertFalse(mainGuiHandle.getPrimaryStage().isIconified());
    }

}
