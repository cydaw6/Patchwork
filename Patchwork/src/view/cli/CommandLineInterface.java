package view.cli;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import model.Action;
import model.Coordinates;
import model.Patch;
import model.QuiltBoard;
import view.Drawable;
import view.UserInterface;

public final class CommandLineInterface implements UserInterface {
  
  // Should be a singleton then ? Because there is only one System.in
  // and closing a PatchworkCLI instance (therefore the scanner) 
  // would mess with other instance also using System.in
  private static final Scanner scanner = new Scanner(System.in); 
  // It's like the window, we draw our elements on it and we refresh the display
  private final StringBuilder builder = new StringBuilder();
  private final LinkedHashSet<String> messages = new LinkedHashSet<>();
  
  public StringBuilder builder() {
    return builder;
  }
  
  public void addMessage(String message) {
    Objects.requireNonNull(message, "The message can't be null");
    messages.add(message);
  }
  
  public void clearMessages() {
    messages.clear();
  }
  
  public void drawMessages() {
    messages.stream().forEachOrdered(message -> {
      builder
      .append("\n")
      .append(message)
      .append("\n");
      }); 
  }
  
  public void display() {
    System.out.print(builder);
  }
  
  @Override
  public void clear() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
    builder.setLength(0);
    drawSplashScreen();
  }

  
  @Override
  public Patch selectPatch(List<Patch> patches) {
    Objects.requireNonNull(patches);
    if(patches.isEmpty()) {
      throw new IllegalArgumentException("Their should be at least 1 patch in the list");
    }
    var i = 0;
    clear();
    // Draw choices
    builder.append("\n");
    for(var patch: patches) {
      i++;
      builder.append(i + ". ");
      patch.drawOnCLI(this);
      builder.append("\n");
    }
    display();
    // Menu
    var localBuilder = new StringBuilder();
    localBuilder
    .append(Color.ANSI_ORANGE)
    .append("\nChoose : ")
    .append(Color.ANSI_RESET);
    System.out.print(localBuilder);
    if(scanner.hasNextInt()) {
      var input = scanner.nextInt();
      scanner.nextLine();
      if(input > 0 && input <= i) {
        return patches.get(input - 1);
      }
    }
    scanner.nextLine();
    System.out.println("Wrong choice\n");
    return null;
  }
  @Override
  public void drawDummyQuilt(QuiltBoard quilt, Patch patch) {
    // top
    builder.append("┌");
    for(var i = 0; i < quilt.width(); i++) {
      builder.append("─");
    }
    builder.append("┐\n");
    // body
    for(var y = 0; y < quilt.height(); y++) {
      builder.append("|");
      for(var x = 0; x < quilt.width(); x++) {
        var isPatchHere = patch.meets(new Coordinates(y, x));
        if(quilt.occupied(new Coordinates(y, x))) {
          if(isPatchHere){
            builder.append(Color.ANSI_RED_BACKGROUND)
            .append("░")
            .append(Color.ANSI_RESET);
          }else {
            builder.append(Color.ANSI_CYAN_BACKGROUND)
            .append("▒")
            .append(Color.ANSI_RESET);
          }
        }else {
          if(isPatchHere) {
            builder.append(Color.ANSI_YELLOW_BACKGROUND)
            .append("▓");
          }else {
            builder.append(" ");
          }
          builder.append(Color.ANSI_RESET);
        }
      }
      builder.append("|\n");
    }
    // bottom
    builder.append("└");
    for(var i = 0; i < quilt.width(); i++) {
      builder.append("─");
    }
    builder.append("┘");
  }
  
  @Override
  public Action getPlayerAction(Set<Action> options) {
    var localBuilder = new StringBuilder();
    localBuilder
    .append(Color.ANSI_ORANGE)
    .append("\n[Actions]\n")
    .append(Color.ANSI_RESET);
    options.stream().forEach(option -> 
      localBuilder.append(option).append("\n"));
    localBuilder.append("\nChoice ? : ");
    System.out.print(localBuilder);
    if(scanner.hasNextLine()) {
      var input = scanner.nextLine();
      for(var op: options) {
        if(op.bind().equals(input)) {
          return op;
        }
      }
    }
    System.out.println("Wrong choice\n");
    return Action.DEFAULT;
  }
  
  /**
   * Close the interface: <br>
   * 
   * close scanner on {@link System.in}
   */
  @Override
  public void close() {
    scanner.close();
    System.out.println("Bye.");
  }

  @Override
  public void drawSplashScreen() {
    var splash = Color.ANSI_BOLD + "  _____      _       _                       _    \n"
        + " |  __ \\    | |     | |                     | |   \n"
        + " | |__) |_ _| |_ ___| |____      _____  _ __| | __\n"
        + " |  ___/ _` | __/ __| '_ \\ \\ /\\ / / _ \\| '__| |/ /\n"
        + " | |  | (_| | || (__| | | \\ V  V / (_) | |  |   < \n"
        + " |_|   \\__,_|\\__\\___|_| |_|\\_/\\_/ \\___/|_|  |_|\\_\\\n"
        + Color.ANSI_GREEN + "└─────────────────────────────────────────────────┘\n"
        + "\n"
        + Color.ANSI_RESET;
    builder.append(splash);
  }

  @Override
  public void draw(Drawable drawable) {
    ((DrawableOnCLI) drawable).drawOnCLI(this);
  }

}