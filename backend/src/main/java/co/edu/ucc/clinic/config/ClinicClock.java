package co.edu.ucc.clinic.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class ClinicClock {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private ClinicClock() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE).withSecond(0).withNano(0);
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}


