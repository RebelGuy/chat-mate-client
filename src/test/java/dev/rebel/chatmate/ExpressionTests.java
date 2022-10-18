package dev.rebel.chatmate;

import dev.rebel.chatmate.gui.Interactive.CounterModal.Expression;
import dev.rebel.chatmate.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.junit.MockitoJUnitRunner;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//@RunWith(MockitoJUnitRunner.class)
public class ExpressionTests {
  @RunWith(Parameterized.class)
  public static class StaticExpressionTests {
    private final int expectedResult;
    private final String expression;

    public StaticExpressionTests(int expectedResult, String expression) {
      this.expectedResult = expectedResult;
      this.expression = expression;
    }

    @Test
    public void testStaticExpression() {
      int result = Expression.evaluateExpression(this.expression, new ArrayList<>(), 0);
      Assert.assertEquals(this.expectedResult, result);
    }

    @Parameterized.Parameters(name = "Expression `{1}` should result in `{0}`")
    public static Collection<Object[]> getTestExpressions() {
      return Collections.list(
          makeStaticExpression(1, "1"),
          makeStaticExpression(150, "150")
      );
    }

    private static Object[] makeStaticExpression(int expectedResult, String expression) {
      return new Object[]{ expectedResult, expression };
    }
  }

  @RunWith(Parameterized.class)
  public static class DynamicExpressionTests {
    private final int expectedResult;
    private final String expression;
    private final int xValue;
    private final List<Tuple2<String, String>> variables;

    public DynamicExpressionTests(int expectedResult, String expression, int xValue, List<Tuple2<String, String>> variables) {
      this.expectedResult = expectedResult;
      this.expression = expression;
      this.xValue = xValue;
      this.variables = variables;
    }

    @Test
    public void testDynamicExpression() {
      int result = Expression.evaluateExpression(this.expression, this.variables, this.xValue);
      Assert.assertEquals(this.expectedResult, result);
    }

    @Parameterized.Parameters(name = "Expression `{1}` with `x`=`{2}` and `{3}` should result in `{0}`")
    public static Collection<Object[]> getTestExpressions() {
      return Collections.list(
          makeDynamicExpression(5, "{{x}}", 5),
          makeDynamicExpression(10, "{{x}} + {{x}}", 5)
      );
    }

    private static Object[] makeDynamicExpression(int expectedResult, String expression, int xValue, String... variableDefinitions) {
      List<Tuple2<String, String>> variables = new ArrayList<>();
      for (int i = 0; i < variableDefinitions.length; i += 2) {
        variables.add(new Tuple2<>(variableDefinitions[i], variableDefinitions[i+1]));
      }

      return new Object[] { expectedResult, expression, xValue, variables };
    }
  }
}
