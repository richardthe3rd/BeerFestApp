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

    @Test
    public void toStringReturnsAsterisks()
    {
        assertThat(new StarRating(0).toString(), equalTo(""));
        assertThat(new StarRating(1).toString(), equalTo("*"));
        assertThat(new StarRating(3).toString(), equalTo("***"));
        assertThat(new StarRating(5).toString(), equalTo("*****"));
    }

    @Test
    public void toFancyStringReturnsUnicodeStars()
    {
        char star = 0x272F;
        assertThat(new StarRating(0).toFancyString(), equalTo(""));
        assertThat(new StarRating(1).toFancyString(), equalTo(String.valueOf(star)));
        assertThat(new StarRating(3).toFancyString(),
                   equalTo(String.valueOf(star) + star + star));
    }

    @Test
    public void noStarsConstant()
    {
        assertThat(StarRating.NO_STARS.getNumberOfStars(), equalTo(0));
        assertThat(StarRating.NO_STARS, comparesEqualTo(new StarRating(0)));
    }

    @Test
    public void floatTruncation()
    {
        assertThat(new StarRating(3.7f).getNumberOfStars(), equalTo(3));
        assertThat(new StarRating(4.9f).getNumberOfStars(), equalTo(4));
        assertThat(new StarRating(0.5f).getNumberOfStars(), equalTo(0));
    }

    @Test
    public void negativeRating()
    {
        StarRating negative = new StarRating(-1);
        assertThat(negative.getNumberOfStars(), equalTo(-1));
        assertThat(negative.toString(), equalTo(""));
    }

    @Test
    public void ratingAboveFive()
    {
        StarRating aboveFive = new StarRating(10);
        assertThat(aboveFive.getNumberOfStars(), equalTo(10));
        assertThat(aboveFive.toString(), equalTo("**********"));
    }

}
