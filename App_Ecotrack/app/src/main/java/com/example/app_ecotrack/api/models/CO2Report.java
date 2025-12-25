package com.example.app_ecotrack.api.models;

public class CO2Report {
    public double totalCO2Saved; // in kg
    public double treesEquivalent; // totalCO2 / 21.77
    public double kmNotDriven; // totalCO2 / 0.21
    public double kwhSaved; // totalCO2 / 0.5

    public CO2Report() {}

    public CO2Report(double totalCO2Saved, double treesEquivalent, double kmNotDriven, double kwhSaved) {
        this.totalCO2Saved = totalCO2Saved;
        this.treesEquivalent = treesEquivalent;
        this.kmNotDriven = kmNotDriven;
        this.kwhSaved = kwhSaved;
    }
}
