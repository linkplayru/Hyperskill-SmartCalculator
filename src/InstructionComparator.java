import java.util.Comparator;

public class InstructionComparator implements Comparator<Instruction> {

    @Override
    public int compare(Instruction i1, Instruction i2) {
        if (i1.getPos() > i2.getPos()) {
            return 1;
        } else if (i1.getPos() < i2.getPos()) {
            return -1;
        } else {
            return 0;
        }
    }

}