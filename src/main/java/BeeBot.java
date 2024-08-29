import ui.Ui;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import task.Task;

class EmptyDescriptionException extends Exception {
    public EmptyDescriptionException(String message) {
        super(message);
    }
}

class MissingDeadlineException extends Exception {
    public MissingDeadlineException(String message) {
        super(message);
    }
}

class MissingEventTimeException extends Exception {
    public MissingEventTimeException(String message) {
        super(message);
    }
}

class TaskNotFoundException extends Exception {
    public TaskNotFoundException(String message) {
        super(message);
    }
}



class Parser {
    public static void speak(String cmd) {
        System.out.println("________________________\n"
                + cmd
                + "________________________\n");
    }

    public static Task getTask(TaskList taskList, int taskNum) throws TaskNotFoundException {
        if (taskNum <= 0 || taskNum > taskList.size()) {
            throw new TaskNotFoundException("Task not found.\n");
        }
        return taskList.get(taskNum - 1);
    }

    public static String concatenate(String[] parts, int start) {
        StringBuilder result = new StringBuilder();
        for (int i = start; i < parts.length - 1; i++) {
            result.append(parts[i]).append(" ");
        }
        result.append(parts[parts.length - 1]);
        return result.toString();
    }

    public static String concatenateUntil(String[] parts, String delimiter) throws MissingDeadlineException {
        StringBuilder result = new StringBuilder();
        int i = 1;
        while (!parts[i].equals(delimiter)) {
            result.append(parts[i]).append(" ");
            i++;
            if (i == parts.length) {
                throw new MissingDeadlineException("Missing `" + delimiter + "` or incorrect format.\n");
            }
        }
        return result.toString().trim();
    }

    public static String getFollowingDate(String[] parts, String delimiter) throws MissingEventTimeException {
        int i = 0;
        while (!parts[i].equals(delimiter)) {
            i++;
            if (i == parts.length) {
                throw new MissingEventTimeException("Missing `" + delimiter + "` or incorrect format.\n");
            }
        }
        i++;
        if (i == parts.length) {
            throw new MissingEventTimeException("Please provide a date/time after `" + delimiter + "`.\n");
        }

        return concatenate(parts, i);
    }

    public static String getFollowingDate(String[] parts, String delimiter, String stopDelimiter) throws MissingEventTimeException {
        int i = 0;
        while (!parts[i].equals(delimiter)) {
            i++;
            if (i == parts.length) {
                throw new MissingEventTimeException("Missing `" + delimiter + "` or incorrect format.\n");
            }
        }
        i++;
        if (i == parts.length || parts[i].equals(stopDelimiter)) {
            throw new MissingEventTimeException("Please provide a date/time after `" + delimiter + "`.\n");
        }

        StringBuilder date = new StringBuilder();
        while (i < parts.length && !parts[i].equals(stopDelimiter)) {
            date.append(parts[i]).append(" ");
            i++;
        }
        return date.toString().trim();
    }

    public static String dateConverter(String date) {
        String[] words = date.split("\\s+");
        LocalDate parsedDate = LocalDate.parse(words[0]);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        String formattedDate = parsedDate.format(formatter);
        words[0] = formattedDate;
        return String.join(" ", words);
    }
}

public class BeeBot {
    private static Storage storage;
    private static TaskList taskList;
    private Ui ui;
    private String filePath;
    public BeeBot(String filePath) {
        this.filePath = filePath;
        storage = new Storage();
        ui = new Ui();
        try {
            taskList = storage.loadTaskListFromFile(filePath);
        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
            taskList = new TaskList();
        }
    }

    public static void main(String[] args) {
        new BeeBot("src/data/BeeBot.txt").run();
    }

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
                        taskList.createToDo(name);
                        break;
                    case "deadline":
                        if (parts.length == 1) {
                            throw new EmptyDescriptionException("Enter a description for the Deadline Task.\n");
                        }
                        String deadlineName = Parser.concatenateUntil(parts, "/by");
                        String deadlineDate = Parser.dateConverter(Parser.getFollowingDate(parts, "/by"));
                        taskList.createDeadline(deadlineName, deadlineDate);
                        break;
                    case "event":
                        if (parts.length == 1) {
                            throw new EmptyDescriptionException("Enter a description for the Event Task.\n");
                        }
                        String eventName = Parser.concatenateUntil(parts, "/from");
                        String startTime = Parser.dateConverter(Parser.getFollowingDate(parts, "/from", "/to"));
                        String endTime = Parser.dateConverter(Parser.getFollowingDate(parts, "/to", ""));
                        taskList.createEvent(eventName, startTime, endTime);
                        break;

                    case "delete":
                        int deletionNumber = Integer.parseInt(parts[1]) - 1;
                        if (deletionNumber >= taskList.size()) {
                            throw new TaskNotFoundException("Task does not exist.\n");
                        }
                        taskList.deleteEvent(deletionNumber);
                        break;
                    default:
                        Parser.speak("Invalid command.\n");
                }

                storage.saveTaskListToFile(filePath, taskList);
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