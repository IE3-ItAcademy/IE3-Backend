package ie3.i_e3_backend.util;

public class OverAlocationException extends RuntimeException {
    private final int totalHours;
    private final int maxHours;

    public OverAlocationException(int totalHours, int maxHours) {
        super(String.format(
                "A alocação excederia o limite máximo de horas semanais. Total: %d, Máximo: %d",
                totalHours, maxHours
        ));
        this.totalHours = totalHours;
        this.maxHours = maxHours;
    }

    public int getTotalHours() { return totalHours; }
    public int getMaxHours() { return maxHours; }
}
