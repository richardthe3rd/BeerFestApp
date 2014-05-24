package ralcock.cbf.model;

import org.junit.Test;
import static org.junit.Assert.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StarRatingTest
{
    @Test
    public void constructFromInteger()
    {
        int n = 1;
        StarRating r = new StarRating(n);
        assertThat(r.getNumberOfStars(), equalTo(n));
    }

    @Test
    public void constructFromFloat()
    {
        float n = 1.0f;
        StarRating r = new StarRating(n);
        assertThat(r.getNumberOfStars(), equalTo((int)n));
    }

    @Test
    public void comparesEqual()
    {
        assertThat(new StarRating(3),
                   comparesEqualTo(new StarRating(3)));
    }

    @Test
    public void comparesLessThan()
    {
        assertThat(new StarRating(1),
                   lessThan(new StarRating(5)));
    }

    @Test
    public void comparesGreaterThan()
    {
        assertThat(new StarRating(5),
                   greaterThan(new StarRating(2)));
    }

}
