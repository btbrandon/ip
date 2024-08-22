public class Event extends Task {
    private String startTime;
    private String endTime;

    public Event(String name, String startTime, String endTime) {
        super(name);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String stringify() {
        String str = "[E]";
        if (super.isDone()) {
            str += "[X]";
        } else {
            str += "[ ]";
        }
        str += (" " + super.getName()
                + "(from: " + this.startTime
                + "to: " + this.endTime
                + ")\n");
        return str;
    }
}
