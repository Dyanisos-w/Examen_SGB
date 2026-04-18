package be.ephec.padel_backend.DTO.admin;

public class DashboardOverviewDto {

    private long totalReservations;
    private double totalRevenue;
    private long totalUsers;
    private double occupancyRate;
    private double cancellationRate;

    public DashboardOverviewDto(long totalReservations,
                                double totalRevenue,
                                long totalUsers,
                                double occupancyRate,
                                double cancellationRate) {
        this.totalReservations = totalReservations;
        this.totalRevenue = totalRevenue;
        this.totalUsers = totalUsers;
        this.occupancyRate = occupancyRate;
        this.cancellationRate = cancellationRate;
    }

    public long getTotalReservations() {
        return totalReservations;
    }
    public void setTotalReservations(long totalReservations) {
        this.totalReservations = totalReservations;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public double getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(double cancellationRate) {
        this.cancellationRate = cancellationRate;
    }
}
