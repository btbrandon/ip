import parser.Parser;
import storage.Storage;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import tasklist.TaskList;
import task.Task;
import exceptions.EmptyDescriptionException;
import exceptions.MissingDeadlineException;
import exceptions.TaskNotFoundException;
import exceptions.MissingEventTimeException;


/**
 * Represents the main application for managing tasks.
 * <p>
 * This class handles user interactions, processes commands, and manages tasks in a list. It also loads and saves tasks
 * from/to a specified file.
 * </p>
 */
public class BeeBot {
    private static Storage storage;
    private static ArrayList<Task> taskList;
    private static String FILEPATH;

    /**
     * Constructs a {@code BeeBot} instance with the specified file path.
     * <p>
     * Initializes the storage and attempts to load the task list from the provided file path. If loading fails,
     * initializes an empty task list.
     * </p>
     *
     * @param filePath the path to the file used for loading and saving tasks
     */
    public BeeBot(String filePath) {
        FILEPATH = filePath;
        storage = new Storage();
        try {
            taskList = storage.loadTaskListFromFile(filePath);
            System.out.println(taskList);
        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
            taskList = new ArrayList<>();
        }
    }

    /**
     * Generates a response for the user's chat message.
     */
    public static String getResponse(String input) {
        String[] parts = input.split(" ");
        String cmd = parts[0];
        try {
            switch (cmd) {
                case "list":
                    int size = taskList.size();
                    if (size == 0) {
                        return "There is currently nothing on the list!";
                    } else {
                        String listStr = "";
                        for (int i = 0; i < size; i++) {
                            int num = i + 1;
                            listStr += (num + "." + taskList.get(i).toString());
                        }
                        return listStr;
                    }
                case "mark":
                    int markTaskNum = Integer.parseInt(parts[1]);
                    Task doneTask = Parser.getTask(taskList, markTaskNum);
                    doneTask.markAsDone();
                    storage.saveTaskListToFile(FILEPATH, taskList);
                    return "🐝-utiful! Worker bee marked this task as done:\n" + doneTask;
                case "unmark":
                    int unmarkTaskNum = Integer.parseInt(parts[1]);
                    Task undoneTask = Parser.getTask(taskList, unmarkTaskNum);
                    undoneTask.markAsUndone();
                    storage.saveTaskListToFile(FILEPATH, taskList);
                    return "🐝-utiful! Worker bee marked this task as not done yet:\n" + undoneTask;
                case "todo":
                    if (parts.length == 1) {
                        throw new EmptyDescriptionException("Enter a description for the Todo Task.\n");
                    }
                    String todoName = Parser.concatenate(parts, 1);
                    TaskList.createToDo(todoName, taskList);
                    storage.saveTaskListToFile(FILEPATH, taskList);
                    return "bzzzz... Worker bee added " + todoName + " to the list!";
                case "deadline":
                    if (parts.length == 1) {
                        throw new EmptyDescriptionException("Enter a description for the Deadline Task.\n");
                    }
                    String deadlineName = Parser.concatenateUntil(parts, "/by");
                    String deadlineDate = Parser.dateConverter(Parser.getFollowingDate(parts, "/by"));
                    TaskList.createDeadline(deadlineName, deadlineDate, taskList);
                    storage.saveTaskListToFile(FILEPATH, taskList);
                    return "BZZZZZ... Worker bee " + deadlineName + " to the list!";
                case "event":
                    if (parts.length == 1) {
                        throw new EmptyDescriptionException("Enter a description for the Event Task.\n");
                    }
                    String eventName = Parser.concatenateUntil(parts, "/from");
                    String startTime = Parser.dateConverter(Parser.getFollowingDate(parts, "/from", "/to"));
                    String endTime = Parser.dateConverter(Parser.getFollowingDate(parts, "/to", ""));
                    TaskList.createEvent(eventName, startTime, endTime, taskList);
                    storage.saveTaskListToFile(FILEPATH, taskList);
                    return "buzzbuzzbuzz... Worker bee added " + eventName + " to the list!";
                case "delete":
                    int deletionNumber = Integer.parseInt(parts[1]) - 1;
                    if (deletionNumber >= taskList.size()) {
                        throw new TaskNotFoundException("Task does not exist.\n");
                    }
                    TaskList.deleteEvent(deletionNumber, taskList);
                    storage.saveTaskListToFile(FILEPATH, taskList);
                    return "Yum yum in my tum tum! Task eaten!";
                case "find":
                    String taskName = Parser.concatenate(parts, 1);
                    ArrayList<Task> searchResults = new ArrayList<>();
                    for (Task task: taskList) {
                        if (task.getName().contains(taskName)) {
                            searchResults.add(task);
                        }
                    }
                    int searchSize = searchResults.size();
                    String searchStr = "";
                    for (int i = 0; i < searchSize; i++) {
                        int num = i + 1;
                        searchStr += (num + "." + searchResults.get(i).toString());
                    }
                    return searchStr;
                default:
                    return """
                            Please enter a valid command for worker bee to follow:
                            1. todo [task-name]
                            2. deadline [task-name] /by [due-date]
                            3. event [task-name] /from [start-date] /to [end-date]
                            4. mark [index]
                            5. unmark [index]
                            6. list
                            7. find
                            8. bye""";
            }
        } catch (EmptyDescriptionException | MissingDeadlineException
                 | MissingEventTimeException | TaskNotFoundException e) {
           return e.getMessage();
        } catch (DateTimeParseException e) {
            return "Invalid date format. Enter date in YYYY-MM-DD format";
        } catch (NumberFormatException e) {
            return "Please enter a valid task number.\n";
        } catch (Exception e) {
            return "An error occurred: " + e.getMessage() + e + "\n";
        }
    }
}