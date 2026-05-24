package be.ephec.padel_backend.DTO.admin;

public record TerrainAdminResponseDto(Integer terrainId, String nom, SiteInfo site) {
    public record SiteInfo(Integer siteId, String nom) {}
}
