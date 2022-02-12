package dev.rebel.chatmate.commands;

import dev.rebel.chatmate.commands.handlers.RanksHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import javax.annotation.Nullable;

public class RanksCommand extends ChatMateSubCommand {
  private static final String usage = "[name]";

  private final RanksHandler ranksHandler;

  public RanksCommand(RanksHandler ranksHandler) {
    this.ranksHandler = ranksHandler;
  }

  @Override
  public String getSubCommandName() {
    return "ranks";
  }

  @Override
  public void processCommand(ICommandSender commandSender, String[] args) throws CommandException {
    @Nullable String name = null;
    if (args.length > 0) {
      String joinedArgs = String.join("", args).trim();
      if (joinedArgs.length() > 0) {
        name = joinedArgs;
      }
    }

    this.ranksHandler.onRank(name);
  }
}
