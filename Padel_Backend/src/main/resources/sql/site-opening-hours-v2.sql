CREATE TABLE SiteOpeningHours (
    id INT IDENTITY(1,1) PRIMARY KEY,
    site_id INT NOT NULL,
    day_of_week VARCHAR(16) NOT NULL,
    opening_time TIME NULL,
    closing_time TIME NULL,
    is_closed BIT NOT NULL DEFAULT 0,
    CONSTRAINT uk_site_opening_hours_site_day UNIQUE (site_id, day_of_week),
    CONSTRAINT fk_site_opening_hours_site FOREIGN KEY (site_id) REFERENCES Site(site_id)
);

