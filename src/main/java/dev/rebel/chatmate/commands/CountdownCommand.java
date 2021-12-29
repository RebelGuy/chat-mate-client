package dev.rebel.chatmate.commands;

import dev.rebel.chatmate.commands.handlers.CountdownHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CountdownCommand extends ChatMateSubCommand {
  private static final String usage = "<start[> <countdownLength> <countdownTitle>]|stop>";

  private final CountdownHandler handler;

  public CountdownCommand(CountdownHandler handler) {
    this.handler = handler;
  }

  @Override
  public String getSubCommandName() {
    return "countdown";
  }

  @Override
  public void processCommand(ICommandSender commandSender, String[] args) throws CommandException {
    if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
      this.handler.stop();
      return;

    } else if (args.length >= 2 && args[0].equalsIgnoreCase("start")) {
      int duration;
      try {
        duration = parseDurationSeconds(args[1]);
      } catch (NumberInvalidException e) {
        throw new CommandException("Unable to parse duration. Usage: " + usage);
      }
      String title = args.length == 2 ? null : String.join(" ", Arrays.stream(args).skip(2).toArray(String[]::new));
      this.handler.start(duration, title);
      return;
    }

    throw new CommandException("Usage: " + usage);
  }

  @Override
  public List<String> addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos position) {
    if (args.length == 1) {
      List<String> options = Arrays.asList("start", "stop");
      return getBestTabCompletionFromOptions(options, args[0]);
    } else {
      return null;
    }
  }


  private static int parseDurationSeconds(String str) throws NumberInvalidException {
    String[] parts = str.split(":");
    int seconds = parts.length >= 1 ? CommandBase.parseInt(parts[0]) : 0;
    int minutes = parts.length >= 2 ? CommandBase.parseInt(parts[1]) : 0;
    int hours = parts.length >= 3 ? CommandBase.parseInt(parts[2]) : 0;

    return seconds + minutes * 60 + hours * 3600;
  }
}
