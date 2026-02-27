package com.example.padelbackend.model;

public class Matchs {


	private int ID;
	private int SiteID;
	private int ReservationIDReservation;
	private int terrainID_terrain;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getSiteID() {
        return SiteID;
    }

    public void setSiteID(int siteID) {
        SiteID = siteID;
    }

    public int getReservationIDReservation() {
        return ReservationIDReservation;
    }

    public void setReservationIDReservation(int reservationIDReservation) {
        ReservationIDReservation = reservationIDReservation;
    }

    public int getTerrainID_terrain() {
        return terrainID_terrain;
    }

    public void setTerrainID_terrain(int terrainID_terrain) {
        this.terrainID_terrain = terrainID_terrain;
    }
}