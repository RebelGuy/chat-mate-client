package dev.rebel.chatmate.commands;

import dev.rebel.chatmate.commands.handlers.SearchHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class SearchCommand extends ChatMateSubCommand {
  private static final String usage = "searchTerm";

  private final SearchHandler searchHandler;

  public SearchCommand(SearchHandler searchHandler) {
    this.searchHandler = searchHandler;
  }

  @Override
  public String getSubCommandName() {
    return "search";
  }

  @Override
  public void processCommand(ICommandSender commandSender, String[] args) throws CommandException {
    if (args.length > 0) {
      String searchTerm = String.join(" ", args).trim();
      if (searchTerm.length() > 0) {
        this.searchHandler.search(searchTerm);
        return;
      }
    }

    throw new CommandException("Usage: " + usage);
  }
}
