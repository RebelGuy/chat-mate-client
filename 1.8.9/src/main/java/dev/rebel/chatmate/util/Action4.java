package dev.rebel.chatmate.util;

@FunctionalInterface
public interface Action4<In1, In2, In3, In4> {
  void run(In1 in1, In2 in2, In3 in3, In4 in4);
}
