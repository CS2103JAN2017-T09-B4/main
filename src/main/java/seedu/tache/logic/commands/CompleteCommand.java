package seedu.tache.logic.commands;

import java.util.ArrayList;
import java.util.List;

import seedu.tache.commons.core.Messages;
import seedu.tache.logic.commands.exceptions.CommandException;
import seedu.tache.model.task.ReadOnlyTask;
import seedu.tache.model.task.Task;
import seedu.tache.model.task.Task.RecurInterval;
import seedu.tache.model.task.UniqueTaskList;

/**
 * Edits the details of an existing task in the task manager.
 */
public class CompleteCommand extends Command {

    public static final String COMMAND_WORD = "complete";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Completes the task identified "
            + "by the index number used in the last tasks listing. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX1 (must be a positive integer), INDEX2, INDEX3, ... \n"
            + "Example: " + COMMAND_WORD + " 1, 2, 6, 8";

    public static final String MESSAGE_COMPLETED_TASK_SUCCESS = "Completed Task: %1$s";
    public static final String MESSAGE_NOT_COMPLETED = "At least one task's index must be provided.";
    public static final String MESSAGE_DUPLICATE_TASK = "This task already exists in the task manager.";

    private final List<Integer> indexList;

    /**
     * @param filteredTaskListIndex the index of the task in the filtered task list to edit
     * @param completeTaskDescriptor details to edit the task with
     */
    public CompleteCommand(List<Integer> indexList) {
        assert indexList.size() > 0;
        this.indexList = indexList;

        // converts indexList from one-based to zero-based.
        for (int i = 0; i < indexList.size(); i++) {
            this.indexList.set(i, indexList.get(i) - 1);
        }
    }

    @Override
    public CommandResult execute() throws CommandException {
        List<ReadOnlyTask> lastShownList = new ArrayList<ReadOnlyTask>(model.getFilteredTaskList());
        List<ReadOnlyTask> completedList = new ArrayList<ReadOnlyTask>();

        //Check all indexes are valid before proceeding
        for (int i = 0; i < indexList.size(); i++) {
            if (indexList.get(i) >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
            }
        }

        for (int i = 0; i < indexList.size(); i++) {
            ReadOnlyTask taskToEdit = lastShownList.get(indexList.get(i));
            Task completedTask = createCompletedTask(taskToEdit);
            try {
                model.updateTask(indexList.get(i), completedTask);
            } catch (UniqueTaskList.DuplicateTaskException dpe) {
                throw new CommandException(MESSAGE_DUPLICATE_TASK);
            }
            completedList.add(taskToEdit);
        }
        model.updateFilteredListToShowUncompleted();
        return new CommandResult(String.format(MESSAGE_COMPLETED_TASK_SUCCESS, completedList.toString()));
    }

    /**
     * Creates and returns a {@code Task} with the details of {@code taskToEdit}
     * edited with {@code editTaskDescriptor}.
     */
    private static Task createCompletedTask(ReadOnlyTask taskToEdit) {
        assert taskToEdit != null;

        return new Task(taskToEdit.getName(), taskToEdit.getStartDateTime(), taskToEdit.getEndDateTime(),
                            taskToEdit.getTags(), false, false, RecurInterval.NONE);

    }
}
