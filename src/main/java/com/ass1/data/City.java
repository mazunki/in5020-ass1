package com.ass1.data;

public class City {
    private int geonameID;
    private String name;
    private String countryCode;
    private String countryName;
    private int population;
    private String timezone;
    private String coordinates;

    public City(int geonameID, String name, String countryCode, String countryName, int population, String timezone,
            String coordinates) {
        this.geonameID = geonameID;
        this.name = name;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.population = population;
        this.timezone = timezone;
        this.coordinates = coordinates;
    }

    public String getCountryName()
    {
        return countryName;
    }

    public int getPopulation()
    {
        return population;
    }


    @Override
    // public String toString() {
        // return "City [countryCode=" + countryCode + ", countryName=" + countryName + ", coordinates=" + coordinates
        //         + ", geonameID=" + geonameID + ", name=" + name + ", population=" + population + ", timezone=" + timezone
        //         + "]";  
    // }

    public String toString() {
        return "City{" +
                "geonameId=" + geonameID +
                ", name='" + name + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", countryName='" + countryName + '\'' +
                ", population=" + population +
                ", timezone='" + timezone + '\'' +
                ", coordinates='" + coordinates + '\'' +
                '}';
    }



    
}
