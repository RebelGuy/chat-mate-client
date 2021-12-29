package dev.rebel.chatmate.commands;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static dev.rebel.chatmate.commands.ChatMateSubCommand.getBestTabCompletionFromOptions;

public class ChatMateSubCommandTests {
  @Test
  public void tabCompletion_badArg_choosesFirstOption() {
    List<String> options = Arrays.asList("abcdef", "abcxyz", "xyz");

    List<String> completions = getBestTabCompletionFromOptions(options, "test");

    Assert.assertNull(completions);
  }

  @Test
  public void tabCompletion_emptyArg_choosesFirstOption() {
    List<String> options = Arrays.asList("abcdef", "abcxyz", "xyz");

    List<String> completions = getBestTabCompletionFromOptions(options, "");

    Assert.assertEquals(completions.size(), 1);
    Assert.assertEquals(completions.get(0), "abcdef");
  }

  @Test
  public void tabCompletion_partialArg_choosesBestMatch() {
    List<String> options = Arrays.asList("abcdef", "abcxyz", "xyz");

    List<String> completions = getBestTabCompletionFromOptions(options, "abcx");

    Assert.assertEquals(completions.size(), 1);
    Assert.assertEquals(completions.get(0), "abcxyz");
  }

  @Test
  public void tabCompletion_fullArg_choosesNextOption() {
    List<String> options = Arrays.asList("abcdef", "abcxyz", "xyz");

    List<String> completions = getBestTabCompletionFromOptions(options, "abcxyz");

    Assert.assertEquals(completions.size(), 1);
    Assert.assertEquals(completions.get(0), "xyz");
  }
}
