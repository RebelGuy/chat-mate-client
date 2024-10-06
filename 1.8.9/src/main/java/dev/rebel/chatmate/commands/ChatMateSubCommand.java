package dev.rebel.chatmate.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ChatMateSubCommand {
  public abstract String getSubCommandName();

  public abstract void processCommand(ICommandSender commandSender, String[] args) throws CommandException;

  // args does NOT include the base command or sub command.
  // should return a list of completions for the last arg.
  public List<String> addTabCompletionOptions(ICommandSender commandSender, String[] args, BlockPos position) {
    return null;
  }

  public static List<String> getBestTabCompletionFromOptions(List<String> options, String arg) {
    // todo: if we register arguments (name, type, mandatory for each arg) with the sub command base, we can intelligently
    // auto-complete the arguments. e.g. typing `ti` could autocomplete to `title=''`

    // by default, it will try to autocomplete to the longest common string. that is not very helpful.
    // if we start typing an option, and there is only one options with this beginning part, it should auto-complete.
    // if we type out the full option and hit tab, it should cycle to the next option.

    if (arg.isEmpty()) {
      // empty: return the first option (because otherwise it will only autocomplete the common string)
      return options.subList(0, 1);
    } else if (options.stream().anyMatch(arg::equals)) {
      // full match with any option: return the next option
      int index = options.indexOf(arg);
      return Arrays.asList(options.get((index + 1) % options.size()));
    } else {
      // partial: return the option that best matches the arg (i.e. most initial characters in common)
      if (options.stream().noneMatch(o -> getOverlap(o, arg) > 0)) {
        // -> if there are none, return the first option
        return options.subList(0, 1);
      } else {
        // -> if there are one or multiple, return the first of the matches with the highest overlap
        int index = 0;
        int highest = getOverlap(options.get(0), arg);
        for (int i = 1; i < options.size(); i++) {
          int overlap = getOverlap(options.get(i), arg);
          if (overlap > highest) {
            index = i;
            highest = overlap;
          }
        }
        return options.subList(index, index + 1);
      }
    }
  }

  // returns the number of chars at the beginning of both words that are the same
  private static int getOverlap(String option, String arg) {
    int overlap = 0;
    int N = Math.min(option.length(), arg.length());

    for (int i = 0; i < N; i++) {
      if (option.charAt(i) == arg.charAt(i)) {
        overlap++;
      } else {
        break;
      }
    }

    return overlap;
  }
}
