package com.example.padelbackend.model;

public class Terrain {

	private int IDterrain;
	private int SiteID;

    public Terrain() {
    }

    public int getIDterrain() {
        return IDterrain;
    }

    public void setIDterrain(int ID_terrain) {
        this.IDterrain = ID_terrain;
    }

    public int getSiteID() {
        return SiteID;
    }

    public void setSiteID(int siteID) {
        SiteID = siteID;
    }
}