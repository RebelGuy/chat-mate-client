package dev.rebel.chatmate.commands;

import dev.rebel.chatmate.commands.handlers.CounterHandler;
import dev.rebel.chatmate.gui.Interactive.CounterModal.Expression;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CounterCommand extends ChatMateSubCommand {
  private static final String usage = "<create[> <startValue> <incrementValue> <scale>( <title>)]|delete>";

  private final CounterHandler counterHandler;

  public CounterCommand(CounterHandler counterHandler) {
    this.counterHandler = counterHandler;
  }

  @Override
  public String getSubCommandName() {
    return "counter";
  }

  @Override
  public void processCommand(ICommandSender commandSender, String[] args) throws CommandException {
    if (args.length == 1 && args[0].equalsIgnoreCase("delete")) {
      this.counterHandler.deleteCounter();
      return;

    } else if (args.length >= 4 && args[0].equalsIgnoreCase("create")) {
      int startValue;
      try {
        startValue = Integer.parseInt(args[1]);
      } catch (NumberFormatException e) {
        throw new CommandException("Unable to parse start value. Usage: " + usage);
      }
      if (startValue < 0) {
        throw new CommandException("Start value cannot be negative");
      }

      int incrementValue;
      try {
        incrementValue = Integer.parseInt(args[2]);
      } catch (NumberFormatException e) {
        throw new CommandException("Unable to parse increment value. Usage: " + usage);
      }
      if (incrementValue <= 0) {
        throw new CommandException("Increment value must be positive");
      }

      float scale;
      try {
        scale = Float.parseFloat(args[3]);
      } catch (NumberFormatException e) {
        throw new CommandException("Unable to parse increment value. Usage: " + usage);
      }
      if (scale < 0.1) {
        throw new CommandException("Scale must be at least 0.1");
      }

      String title = args.length == 4 ? null : String.join(" ", Arrays.stream(args).skip(4).toArray(String[]::new));
      Function<Integer, String> displayFunction = Expression.createDisplayFunction(String.format("%s: {{x}}", title), new ArrayList<>());
      this.counterHandler.createCounter(startValue, incrementValue, scale, displayFunction);
      return;
    }

    throw new CommandException("Usage: " + usage);
  }

  @Override
  public List<String> addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos position) {
    if (args.length == 1) {
      List<String> options = Arrays.asList("create", "delete");
      return getBestTabCompletionFromOptions(options, args[0]);
    } else {
      return null;
    }
  }
}
