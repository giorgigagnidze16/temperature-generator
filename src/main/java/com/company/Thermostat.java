package com.company;

import java.io.Serializable;

public class Thermostat implements Serializable {
  private Long id;
  private double temperature;

  public Thermostat(Long id) {
    this.id = id;
  }

  public Thermostat(Long id, double temperature) {
    this.id = id;
    this.temperature = temperature;
  }

  public Thermostat() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }
}
