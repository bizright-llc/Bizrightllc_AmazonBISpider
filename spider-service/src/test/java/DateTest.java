import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateTest {

    @Test
    public void testFirstAndLastDateOfWeek(){
        LocalDate ld = LocalDate.now();

        System.out.println(ld.with(DayOfWeek.SUNDAY));

        System.out.println(ld.with(WeekFields.of(Locale.US).dayOfWeek(), 1L));
        System.out.println(ld.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7L));

        LocalDate lastWeek = LocalDate.now().minusWeeks(1);

        System.out.println(lastWeek);
        System.out.println(lastWeek.getMonth());

        System.out.println(lastWeek.with(DayOfWeek.MONDAY));
        System.out.println(lastWeek.with(DayOfWeek.SUNDAY));
    }

}
