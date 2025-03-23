package ui;

public interface Client {
    String help();
    Client transition(String token);
    Client transition();
    String printState();
    String eval(String input);
}
