package dev.rebel.chatmate.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.*;

public class ChatMateCommand extends CommandBase {
  private final Map<String, ChatMateSubCommand> subCommands;

  public ChatMateCommand(ChatMateSubCommand... subCommands) {
    this.subCommands = new HashMap<>();

    for (ChatMateSubCommand cmd: subCommands) {
      this.subCommands.put(cmd.getSubCommandName(), cmd);
    }
  }

  @Override
  public final String getCommandName() {
    return "cm";
  }

  @Override
  public String getCommandUsage(ICommandSender commandSender) {
    return "cm <" + String.join("|", this.subCommands.keySet()) + ">";
  }

  @Override
  public boolean canCommandSenderUseCommand(ICommandSender commandSender) {
    return true;
  }

  @Override
  public void processCommand(ICommandSender commandSender, String[] args) throws CommandException {
    ChatMateSubCommand subCommand = this.tryGetSubCommand(args);
    if (subCommand != null) {
      subCommand.processCommand(commandSender, Arrays.stream(args).skip(1).toArray(String[]::new));
    } else {
      throw new CommandException("Usage: " + this.getCommandUsage(commandSender));
    }
  }

  @Override
  public List<String> addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos position) {
    if (args.length == 1) {
      return new ArrayList<>(this.subCommands.keySet());
    }

    ChatMateSubCommand subCommand = this.tryGetSubCommand(args);
    if (subCommand == null) {
      String arg = args[0];
      ArrayList<String> completions = new ArrayList<>();
      for (String cmd: this.subCommands.keySet()) {
        if (CommandBase.doesStringStartWith(arg, cmd)) {
          completions.add(cmd);
        }
      }
      return completions;

    } else {
      return subCommand.addTabCompletionOptions(commandSender, Arrays.stream(args).skip(1).toArray(String[]::new), position);
    }
  }

  private ChatMateSubCommand tryGetSubCommand(String[] args) {
    if (args.length == 0) {
      return null;
    } else {
      return this.subCommands.get(args[0]);
    }
  }
}
