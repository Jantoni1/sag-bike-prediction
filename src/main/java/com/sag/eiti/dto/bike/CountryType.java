
package com.sag.eiti.dto.bike;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for countryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="countryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="city" type="{}cityType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="lat" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lng" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="zoom" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="hotline" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="domain" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="language" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="email" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="timezone" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="currency" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="country_calling_code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="system_operator_address" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="country" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="country_name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="terms" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="policy" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="website" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="booked_bikes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="set_point_bikes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="available_bikes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pricing" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "countryType", propOrder = {
    "city"
})
@Getter
@Setter
public class CountryType {

    @XmlElement(required = true)
    protected CityType city;
    @XmlAttribute(name = "lat")
    protected String lat;
    @XmlAttribute(name = "lng")
    protected String lng;
    @XmlAttribute(name = "zoom")
    protected String zoom;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "hotline")
    protected String hotline;
    @XmlAttribute(name = "domain")
    protected String domain;
    @XmlAttribute(name = "language")
    protected String language;
    @XmlAttribute(name = "email")
    protected String email;
    @XmlAttribute(name = "timezone")
    protected String timezone;
    @XmlAttribute(name = "currency")
    protected String currency;
    @XmlAttribute(name = "country_calling_code")
    protected String countryCallingCode;
    @XmlAttribute(name = "system_operator_address")
    protected String systemOperatorAddress;
    @XmlAttribute(name = "country")
    protected String country;
    @XmlAttribute(name = "country_name")
    protected String countryName;
    @XmlAttribute(name = "terms")
    protected String terms;
    @XmlAttribute(name = "policy")
    protected String policy;
    @XmlAttribute(name = "website")
    protected String website;
    @XmlAttribute(name = "booked_bikes")
    protected String bookedBikes;
    @XmlAttribute(name = "set_point_bikes")
    protected String setPointBikes;
    @XmlAttribute(name = "available_bikes")
    protected String availableBikes;
    @XmlAttribute(name = "pricing")
    protected String pricing;
}
