package com.pilaslot.reservation.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationPolicyTest {

    @Test
    void determinesWhetherWeeklyReservationLimitIsReached() {
        assertThat(ReservationPolicy.isWeeklyReservationLimitReached(13)).isFalse();
        assertThat(ReservationPolicy.isWeeklyReservationLimitReached(14)).isTrue();
        assertThat(ReservationPolicy.isWeeklyReservationLimitReached(15)).isTrue();
    }

    @Test
    void determinesWhetherWeeklyCancellationLimitIsReached() {
        assertThat(ReservationPolicy.isWeeklyCancellationLimitReached(6)).isFalse();
        assertThat(ReservationPolicy.isWeeklyCancellationLimitReached(7)).isTrue();
        assertThat(ReservationPolicy.isWeeklyCancellationLimitReached(8)).isTrue();
    }
}
