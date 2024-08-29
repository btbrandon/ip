package todo;
import task.Task;

public class ToDo extends Task {
    public ToDo(String name) {
        super(name);
    }

    @Override
    public String toString() {
        String str = "[T]";
        str += super.toString();
        return str;
    }
}
