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
public class LoadCommand extends Command implements Undoable {

    public static final String COMMAND_WORD = "load";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Loads the file specified.\n"
            + "Parameters: FILE PATH \n"
            + "Example: " + COMMAND_WORD + " C:/Users/User/Desktop/taskmanager.xml";

    public static final String MESSAGE_SUCCESS = "Data file loaded successfully from %1$s";
    public static final String MESSAGE_FAILURE = "Data file failed to load from %1$s";

    private String newPath;
    private String prevPath;
    private boolean commandSuccess;

    /**
     * Creates a SaveCommand using the directory entered by the user.
     *
     * @throws IllegalValueException if any of the raw values are invalid
     */
    public LoadCommand(String newDirectory) {
        this.newPath = newDirectory;
        commandSuccess = false;
    }

    @Override
    public CommandResult execute() throws CommandException, DataConversionException, IOException {
        assert storage != null;
        assert config != null;
        Optional<ReadOnlyTaskManager> taskManagerOptional;
        ReadOnlyTaskManager initialData;
        this.prevPath = storage.getTaskManagerFilePath();
        config.setTaskManagerFilePath(newPath);
        storage.setTaskManagerFilePath(newPath);
        ConfigUtil.saveConfig(config, Config.DEFAULT_CONFIG_FILE);

        taskManagerOptional = storage.readTaskManager();
        initialData = taskManagerOptional.orElseGet(SampleDataUtil::getSampleTaskManager);

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

