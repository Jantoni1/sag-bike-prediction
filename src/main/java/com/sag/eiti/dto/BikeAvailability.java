package com.sag.eiti.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "markers") // TODO: 10.01.2020
@XmlAccessorType(XmlAccessType.FIELD)
public class BikeAvailability{

    @XmlElement(name = "country", required = true)
    protected Object country;

}
