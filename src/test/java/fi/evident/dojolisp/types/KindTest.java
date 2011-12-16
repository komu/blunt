package fi.evident.dojolisp.types;

import org.junit.Test;

import static fi.evident.dojolisp.types.Kind.STAR;
import static fi.evident.dojolisp.types.Kind.arrow;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class KindTest {

    @Test
    public void stringRepresentationOfKinds() {
        assertThat(STAR.toString(), is("*"));
        
        assertThat(arrow(STAR, STAR).toString(), is("* -> *"));
        
        assertThat(arrow(STAR, arrow(STAR, STAR)).toString(), is("* -> * -> *"));
        assertThat(arrow(STAR, arrow(STAR, arrow(STAR, STAR))).toString(), is("* -> * -> * -> *"));

        assertThat(arrow(arrow(STAR, STAR), STAR).toString(), is("(* -> *) -> *"));
    }
}
