package ecsimsw.picup.album.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StorageUsageTest {

    @DisplayName("스토리지 제한값은 음수가 될 수 없다.")
    @Test
    void invalidLimit() {
        assertThatThrownBy(
            () -> new StorageUsage(1L, -1L, 0L)
        );
    }

    @DisplayName("스토리지 사용량은 음수가 될 수 없다.")
    @Test
    void invalidUsage() {
        assertThatThrownBy(
            () -> new StorageUsage(1L, 0L, -1L)
        );
    }

    @DisplayName("스토리지 사용량은 제한량보다 클 수 없다.")
    @Test
    void invalidUsageOverLimit() {
        assertThatThrownBy(
            () -> new StorageUsage(1L, 0L, 1L)
        );
    }

    @DisplayName("기존 사용량에 새로운 사용량을 더한다.")
    @Test
    void add() {
        var oldUsage = 0L;
        var addUsage = 1L;

        var usage = new StorageUsage(0L, 10L, oldUsage);
        usage.add(addUsage);

        assertThat(usage.getUsageAsByte()).isEqualTo(oldUsage + addUsage);
    }

    @DisplayName("제한량보다 큰 사용량을 더할 수 없다.")
    @Test
    void addWithInvalidUsage() {
        var limit = 10L;
        var addUsage = limit +1;
        var usage = new StorageUsage(0L, 10L, 0L);

        assertThatThrownBy(
            () -> usage.add(addUsage)
        );
    }

    @DisplayName("사용량을 뺀다.")
    @Test
    void subtract() {
        var oldUsage = 10L;
        var subtractUsage = 1L;

        var usage = new StorageUsage(0L, 10L, oldUsage);
        usage.subtract(subtractUsage);

        assertThat(usage.getUsageAsByte()).isEqualTo(oldUsage - subtractUsage);
    }

    @DisplayName("사용량을 뺀 후의 사용량이 0보다 작다면, 그 사용량을 0으로 한다.")
    @Test
    void subtractWithUnderflow() {
        var oldUsage = 10L;
        var subtractUsage = oldUsage +1;

        var usage = new StorageUsage(0L, 10L, oldUsage);
        usage.subtract(subtractUsage);

        assertThat(usage.getUsageAsByte()).isEqualTo(0);
    }
}