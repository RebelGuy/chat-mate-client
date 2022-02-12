package dev.rebel.chatmate.services.util;

@FunctionalInterface
public interface Action3 <In1, In2, In3> {
  void run(In1 in1, In2 in2, In3 in3);
}
