package com.pilaslot.reservation.domain;

public final class ReservationPolicy {

    private static final long WEEKLY_RESERVATION_LIMIT = 14;
    private static final long WEEKLY_CANCELLATION_LIMIT = 7;

    private ReservationPolicy() {
    }

    public static boolean isWeeklyReservationLimitReached(long count) {
        return count >= WEEKLY_RESERVATION_LIMIT;
    }

    public static boolean isWeeklyCancellationLimitReached(long count) {
        return count >= WEEKLY_CANCELLATION_LIMIT;
    }
}
