public class Token {
  private Type tpe;
  private String text;
  private int startPos;

  public Token(Type tpe, String text, int startPos) {
      this.tpe = tpe;
      this.text = text;
      this.startPos = startPos;
  }

  public Type getType() {
      return tpe;
  }

  public String getText() {
      return text;
  }

  public int getStartPos() {
      return startPos;
  }

  @Override
  public String toString() {
      return "Token{" +
              "type=" + tpe +
              ", text='" + text + '\'' +
              ", startPos=" + startPos +
              '}';
  }

  public enum Type {
      Num, Plus, Times, Divide, Modulo, Minus, Greater, Less, GreaterEqual, LessEqual,
      NotEqual, Assign, And, Or, Not, Identifier, Container, Int, Bool, Char, Float, 
      Equals, Comma, Print, Parentheses, SingleQ, DoubleQ, SquareB, Concat, NewLine, BooleanLiteral,
      Scan, 
  }
}
