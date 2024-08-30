import ui.Ui;
import parser.Parser;
import storage.Storage;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;
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
    private final String FILEPATH;

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
        this.FILEPATH = filePath;
        storage = new Storage();
        Ui ui = new Ui();
        try {
            taskList = storage.loadTaskListFromFile(filePath);
        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
            taskList = new ArrayList<>();
        }
    }

    /**
     * The entry point of the application.
     * <p>
     * Creates an instance of {@code BeeBot} with a predefined file path and starts the main loop to handle user input.
     * </p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new BeeBot("/Users/beetee/Documents/NUS/CS/Y2S1/CS2103T/ip/data/BeeBot.txt").run();
    }

    /**
     * Starts the main loop for handling user input.
     * <p>
     * Reads commands from the user, processes them, and updates the task list accordingly. It handles various commands,
     * such as adding, marking, and deleting tasks, and interacts with the {@code Ui} and {@code Parser} classes.
     * </p>
     */
    public void run() {
        boolean exit_status = false;
        while (!exit_status) {
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] parts = input.split(" ");
            String cmd = parts[0];

            try {
                switch (cmd) {
                case "bye":
                    Ui.exit();
                    exit_status = true;
                    break;
                case "list":
                    int size = taskList.size();
                    String listStr = "";
                    for (int i = 0; i < size; i++) {
                        int num = i + 1;
                        listStr += (num + "." + taskList.get(i).toString());
                    }
                    Parser.speak(listStr);
                    break;
                case "mark":
                    int taskNum = Integer.parseInt(parts[1]);
                    Task currTask = Parser.getTask(taskList, taskNum);
                    currTask.markAsDone();
                    Parser.speak("Nice! I've marked this task as done:\n" + currTask.toString());
                    break;
                case "unmark":
                    int taskNum2 = Integer.parseInt(parts[1]);
                    Task curr = Parser.getTask(taskList, taskNum2);
                    curr.markAsUndone();
                    Parser.speak("Nice! I've marked this task as not done yet:\n" + curr.toString());
                    break;
                case "todo":
                    if (parts.length == 1) {
                        throw new EmptyDescriptionException("Enter a description for the Todo Task.\n");
                    }
                    String name = Parser.concatenate(parts, 1);
                    TaskList.createToDo(name, taskList);
                    break;
                case "deadline":
                    if (parts.length == 1) {
                        throw new EmptyDescriptionException("Enter a description for the Deadline Task.\n");
                    }
                    String deadlineName = Parser.concatenateUntil(parts, "/by");
                    String deadlineDate = Parser.dateConverter(Parser.getFollowingDate(parts, "/by"));
                    TaskList.createDeadline(deadlineName, deadlineDate, taskList);
                    break;
                case "event":
                    if (parts.length == 1) {
                        throw new EmptyDescriptionException("Enter a description for the Event Task.\n");
                    }
                    String eventName = Parser.concatenateUntil(parts, "/from");
                    String startTime = Parser.dateConverter(Parser.getFollowingDate(parts, "/from", "/to"));
                    String endTime = Parser.dateConverter(Parser.getFollowingDate(parts, "/to", ""));
                    TaskList.createEvent(eventName, startTime, endTime, taskList);
                    break;

                case "delete":
                    int deletionNumber = Integer.parseInt(parts[1]) - 1;
                    if (deletionNumber >= taskList.size()) {
                        throw new TaskNotFoundException("Task does not exist.\n");
                    }
                    TaskList.deleteEvent(deletionNumber, taskList);
                    break;
                default:
                    Parser.speak("Invalid command.\n");
                }

                storage.saveTaskListToFile(FILEPATH, taskList);
            } catch (EmptyDescriptionException | MissingDeadlineException | MissingEventTimeException | TaskNotFoundException e) {
                Parser.speak(e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Enter date in YYYY-MM-DD format");
            }catch (NumberFormatException e) {
                Parser.speak("Please enter a valid task number.\n");
            } catch (Exception e) {
                Parser.speak("An error occurred: " + e.getMessage() + e + "\n");
            }
        }
    }
}