package ui;

public class Ui {
    public Ui() {
        speak("Hello! I'm BeeBot\n" + "What can I do for you?\n");
    }

    public static void speak(String cmd) {
        System.out.println("________________________\n"
                + cmd
                + "________________________\n");
    }

    public static void exit() {
        speak("Bye. Hope to see you again!\n");
    }
}