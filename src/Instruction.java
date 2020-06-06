import java.util.Map;

public class Instruction {

    final static public Map<String, Integer> priorities = Map.of(
            "(", 0,
            "+", 1,
            "-", 1,
            "*", 2,
            "/", 2,
            "^", 3);;

    private String data;
    private int pos;
    private InstructionType type;

    public Instruction(String data, int pos, InstructionType type) {
        this.data = data;
        this.pos = pos;
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getPos() {
        return pos;
    }

    public InstructionType getType() {
        return type;
    }

    public void setType(InstructionType type) {
        this.type = type;
    }

    public boolean isLeft() {
        if ("(".equals(data)) {
            return true;
        } else {
            return false;
        }
    }

    public int getPriority() {
        return priorities.get(data);
    }

}
