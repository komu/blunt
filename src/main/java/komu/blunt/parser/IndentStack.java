package komu.blunt.parser;

import java.util.ArrayList;
import java.util.List;

final class IndentStack {
    private final List<Integer> indents = new ArrayList<Integer>();

    public void push(int column) {
        indents.add(column);
    }

    public boolean popIf(int column) {
        int last = indents.size()-1;
        if (!indents.isEmpty() && column <= indents.get(last)) {
            indents.remove(last);
            return true;
        }
        return false;
    }

    public List<Integer> toList() {
        return new ArrayList<Integer>(indents);
    }

    public void reset(List<Integer> indents) {
        this.indents.clear();
        this.indents.addAll(indents);
    }
}
