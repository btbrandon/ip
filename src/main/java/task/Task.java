package task;

public class Task {
    private boolean done = false;
    private String name;

    public Task() {
    }

    public Task(String name) {
        this.name = name;
    }

    public String toString() {
        String str = "";
        if (done) {
            str += "[X]";
        } else {
            str += "[ ]";
        }
        str += (" " + this.name + "\n");
        return str;
    }

    public void markAsDone() {
        this.done = true;
    }

    public void markAsUndone() {
        this.done = false;
    }

    public Boolean isDone() {
        return this.done;
    }

    public String getName() {
        return this.name;
    }
}
