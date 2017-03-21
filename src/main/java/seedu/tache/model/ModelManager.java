package seedu.tache.model;

import java.util.Set;
import java.util.logging.Logger;

import javafx.collections.transformation.FilteredList;
import seedu.tache.commons.core.ComponentManager;
import seedu.tache.commons.core.LogsCenter;
import seedu.tache.commons.core.UnmodifiableObservableList;
import seedu.tache.commons.events.model.TaskManagerChangedEvent;
import seedu.tache.commons.util.CollectionUtil;
import seedu.tache.commons.util.StringUtil;
import seedu.tache.model.task.ReadOnlyTask;
import seedu.tache.model.task.Task;
import seedu.tache.model.task.UniqueTaskList;
import seedu.tache.model.task.UniqueTaskList.DuplicateTaskException;
import seedu.tache.model.task.UniqueTaskList.TaskNotFoundException;

/**
 * Represents the in-memory model of the task manager data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    public static final int MARGIN_OF_ERROR = 1;

    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final TaskManager taskManager;
    private final FilteredList<ReadOnlyTask> filteredTasks;

    /**
     * Initializes a ModelManager with the given taskManager and userPrefs.
     */
    public ModelManager(ReadOnlyTaskManager taskManager, UserPrefs userPrefs) {
        super();
        assert !CollectionUtil.isAnyNull(taskManager, userPrefs);

        logger.fine("Initializing with task manager: " + taskManager + " and user prefs " + userPrefs);

        this.taskManager = new TaskManager(taskManager);
        filteredTasks = new FilteredList<>(this.taskManager.getTaskList());
    }

    public ModelManager() {
        this(new TaskManager(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyTaskManager newData) {
        taskManager.resetData(newData);
        indicateTaskManagerChanged();
    }

    @Override
    public ReadOnlyTaskManager getTaskManager() {
        return taskManager;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateTaskManagerChanged() {
        raise(new TaskManagerChangedEvent(taskManager));
    }

    @Override
    public synchronized void deleteTask(ReadOnlyTask target) throws TaskNotFoundException {
        taskManager.removeTask(target);
        indicateTaskManagerChanged();
    }

    @Override
    public synchronized void addTask(Task task) throws DuplicateTaskException {
        taskManager.addTask(task);
        updateFilteredListToShowUncompleted();
        indicateTaskManagerChanged();
    }

    @Override
    public void updateTask(int filteredTaskListIndex, ReadOnlyTask editedTask)
            throws UniqueTaskList.DuplicateTaskException {
        assert editedTask != null;

        int taskManagerIndex = filteredTasks.getSourceIndex(filteredTaskListIndex);
        taskManager.updateTask(taskManagerIndex, editedTask);
        indicateTaskManagerChanged();
    }

    //=========== Filtered Task List Accessors =============================================================

    @Override
    public UnmodifiableObservableList<ReadOnlyTask> getFilteredTaskList() {
        return new UnmodifiableObservableList<>(filteredTasks);
    }

    @Override
    public void updateFilteredListToShowAll() {
        filteredTasks.setPredicate(null);
    }

    @Override
    public void updateFilteredListToShowUncompleted() {
        updateFilteredTaskList(new PredicateExpression(new ActiveQualifier(true)));
    }

    @Override
    public void updateFilteredListToShowCompleted() {
        updateFilteredTaskList(new PredicateExpression(new ActiveQualifier(false)));
    }

    @Override
    public void updateFilteredTaskList(Set<String> keywords) {
        updateFilteredTaskList(new PredicateExpression(new NameQualifier(keywords)));
    }

    private void updateFilteredTaskList(Expression expression) {
        filteredTasks.setPredicate(expression::satisfies);
    }

    //========== Inner classes/interfaces/methods used for filtering =================================================

    interface Expression {
        boolean satisfies(ReadOnlyTask task);
        String toString();
    }

    private class PredicateExpression implements Expression {

        private final Qualifier qualifier;

        PredicateExpression(Qualifier qualifier) {
            this.qualifier = qualifier;
        }

        @Override
        public boolean satisfies(ReadOnlyTask task) {
            return qualifier.run(task);
        }

        @Override
        public String toString() {
            return qualifier.toString();
        }
    }

    interface Qualifier {
        boolean run(ReadOnlyTask task);
        String toString();
    }

    private class NameQualifier implements Qualifier {
        private Set<String> nameKeyWords;

        NameQualifier(Set<String> nameKeyWords) {
            this.nameKeyWords = nameKeyWords;
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            String[] nameElements = task.getName().fullName.split(" ");
            boolean partialMatch = false;
            String trimmedNameKeyWords = nameKeyWords.toString()
                                         .substring(1, nameKeyWords.toString().length() - 1).toLowerCase();
            for (int i = 0; i < nameElements.length; i++) {
                if (computeLevenshteinDistance(trimmedNameKeyWords, nameElements[i].toLowerCase()) <= MARGIN_OF_ERROR) {
                    partialMatch = true;
                    break;
                }
            }
            return nameKeyWords.stream()
                    .filter(keyword -> StringUtil.containsWordIgnoreCase(task.getName().fullName, keyword))
                    .findAny()
                    .isPresent()
                    || partialMatch;
        }

        @Override
        public String toString() {
            return "name=" + String.join(", ", nameKeyWords);
        }
    }

    private class ActiveQualifier implements Qualifier {
        private boolean isActive;

        ActiveQualifier(boolean isActive) {
            this.isActive = isActive;
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            if (isActive) {
                return task.getActiveStatus();
            } else {
                return !task.getActiveStatus();
            }
        }

        @Override
        public String toString() {
            return "active=true";
        }
    }

    private class DateQualifier implements Qualifier {
        private Set<String> dateKeyWords;

        DateQualifier(Set<String> dateKeyWords) {
            this.dateKeyWords = dateKeyWords;
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            return false;
        }

        @Override
        public String toString() {
            return "date=" + String.join(", ", dateKeyWords);
        }

    }

    private class TimeQualifier implements Qualifier {
        private Set<String> timeKeyWords;

        TimeQualifier(Set<String> timeKeyWords) {
            this.timeKeyWords = timeKeyWords;
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            return false;
        }

        @Override
        public String toString() {
            return "time=" + String.join(", ", timeKeyWords);
        }

    }

    private class MultiQualifier implements Qualifier {
        private Set<String> multiKeyWords;
        private NameQualifier nameQualifier;
        private DateQualifier dateQualifier;
        private TimeQualifier timeQualifier;

        MultiQualifier(Set<String> multiKeyWords) {
            this.multiKeyWords = multiKeyWords;
            nameQualifier = new NameQualifier(multiKeyWords);
            dateQualifier = new DateQualifier(multiKeyWords);
            timeQualifier = new TimeQualifier(multiKeyWords);
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            return false;
        }

        @Override
        public String toString() {
            return "multi=" + String.join(", ", multiKeyWords);
        }

    }

    private int computeLevenshteinDistance(CharSequence str1, CharSequence str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 1; j <= str2.length(); j++) {
            distance[0][j] = j;
        }
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                distance[i][j] =
                 minimum(
                    distance[i - 1][j] + 1,
                    distance[i][j - 1] + 1,
                    distance[i - 1][j - 1] +
                        ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));
            }
        }
        return distance[str1.length()][str2.length()];
    }

    private int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

}
