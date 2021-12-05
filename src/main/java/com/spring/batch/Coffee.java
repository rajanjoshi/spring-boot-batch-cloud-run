package com.spring.batch;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class Coffee {

    @Id
    @SequenceGenerator(name = "seqGenerator", sequenceName = "COFFEE_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqGenerator")
    private Long id;
    private String brand;
    private String origin;
    private String characteristics;

    public Coffee() {
    }

    public Coffee(String brand, String origin, String characteristics) {
        this.brand = brand;
        this.origin = origin;
        this.characteristics = characteristics;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    @Override
    public String toString() {
        return "Coffee [brand=" + getBrand() + ", origin=" + getOrigin() + ", characteristics=" + getCharacteristics() + "]";
    }

}
