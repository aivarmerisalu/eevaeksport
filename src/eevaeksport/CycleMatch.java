package eevaeksport;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class CycleMatch {
	    private final LocalDate date;
	    public final DayOfWeek dayOfWeek;
	    private final String code;

	    public CycleMatch(LocalDate date, String code) {
	        this.date = date;
	        this.dayOfWeek = date.getDayOfWeek();
	        this.code = code;
	    }

	    public LocalDate getDate() {
	        return date;
	    }

	    public DayOfWeek getDayOfWeek() {
	        return dayOfWeek;
	    }

	    public String getCode() {
	        return code;
	    }

	    @Override
	    public String toString() {
	        return date + " (" + dayOfWeek + ") -> " + code;
	    }
	}