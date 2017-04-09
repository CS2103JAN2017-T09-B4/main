//@@author A0139961U
package seedu.tache.logic.commands;

import java.io.IOException;
import java.util.Optional;

import seedu.tache.commons.core.Config;
import seedu.tache.commons.exceptions.DataConversionException;
import seedu.tache.commons.exceptions.IllegalValueException;
import seedu.tache.commons.util.ConfigUtil;
import seedu.tache.logic.commands.exceptions.CommandException;
import seedu.tache.model.ReadOnlyTaskManager;
import seedu.tache.model.util.SampleDataUtil;

/**
 * Adds a task to the task manager.
 */
public class SaveCommand extends Command implements Undoable {

    public static final String COMMAND_WORD = "save";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Changes the save location of the"
            + "data files based on the directory entered or selected.\n"
            + "Parameters: DIRECTORY \n"
            + "Example: " + COMMAND_WORD + " C:/Users/User/Desktop";

    public static final String MESSAGE_SUCCESS = "Save location changed to: %1$s";
    public static final String MESSAGE_FAILURE = "Save location failed to change to: %1$s";
    public static final int FILE_NAME_LENGTH = 16;
    private String newPath;
    private String prevPath;
    private boolean commandSuccess;

    /**
     * Creates a SaveCommand using the directory entered by the user.
     *
     * @throws IllegalValueException if any of the raw values are invalid
     */
    public SaveCommand(String newDirectory) {
        this.newPath = newDirectory;
        commandSuccess = false;
    }

    @Override
    public CommandResult execute() throws CommandException, DataConversionException, IOException {
        assert storage != null;
        assert config != null;
        Optional<ReadOnlyTaskManager> taskManagerOptional;
        ReadOnlyTaskManager initialData;
        taskManagerOptional = storage.readTaskManager();
        initialData = taskManagerOptional.orElseGet(SampleDataUtil::getSampleTaskManager);

        this.prevPath = storage.getTaskManagerFilePath()
                               .substring(0, storage.getTaskManagerFilePath().length() - FILE_NAME_LENGTH);
        config.setTaskManagerFilePath(newPath + "\\taskmanager.xml");
        storage.setTaskManagerFilePath(newPath + "\\taskmanager.xml");

        ConfigUtil.saveConfig(config, Config.DEFAULT_CONFIG_FILE);

        model.resetData(initialData);
        commandSuccess = true;
        undoHistory.push(this);
        return new CommandResult(String.format(MESSAGE_SUCCESS, newPath));
    }

    @Override
    public boolean isUndoable() {
        return commandSuccess;
    }

    @Override
    public String undo() throws CommandException, DataConversionException, IOException {
        try {
            this.newPath = prevPath;
            this.execute();
            undoHistory.remove(this);
            return String.format(MESSAGE_SUCCESS, newPath);
        } catch (CommandException e) {
            throw new CommandException(String.format(MESSAGE_FAILURE, prevPath));
        }
    }
}

