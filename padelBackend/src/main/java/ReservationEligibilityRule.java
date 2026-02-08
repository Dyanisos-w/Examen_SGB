import java.time.LocalDate;
import java.util.Locale;

public interface ReservationEligibilityRule {
    boolean canCreatMatch(User user);
}
