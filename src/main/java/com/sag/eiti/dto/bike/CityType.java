
package com.sag.eiti.dto.bike;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="place" type="{}placeType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="uid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lat" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lng" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="zoom" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="maps_icon" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="alias" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="break" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="num_places" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="refresh_rate" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bounds" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="booked_bikes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="set_point_bikes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="available_bikes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="return_to_official_only" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bike_types" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="website" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cityType", propOrder = {
    "place"
})
@Getter
@Setter
public class CityType {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected List<PlaceType> place;
    @XmlAttribute(name = "uid")
    protected String uid;
    @XmlAttribute(name = "lat")
    protected String lat;
    @XmlAttribute(name = "lng")
    protected String lng;
    @XmlAttribute(name = "zoom")
    protected String zoom;
    @XmlAttribute(name = "maps_icon")
    protected String mapsIcon;
    @XmlAttribute(name = "alias")
    protected String alias;
    @XmlAttribute(name = "break")
    protected String _break;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "num_places")
    protected String numPlaces;
    @XmlAttribute(name = "refresh_rate")
    protected String refreshRate;
    @XmlAttribute(name = "bounds")
    protected String bounds;
    @XmlAttribute(name = "booked_bikes")
    protected String bookedBikes;
    @XmlAttribute(name = "set_point_bikes")
    protected String setPointBikes;
    @XmlAttribute(name = "available_bikes")
    protected String availableBikes;
    @XmlAttribute(name = "return_to_official_only")
    protected String returnToOfficialOnly;
    @XmlAttribute(name = "bike_types")
    protected String bikeTypes;
    @XmlAttribute(name = "website")
    protected String website;

    /**
     * Gets the value of the place property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the place property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PlaceType }
     * 
     * 
     */
    public List<PlaceType> getPlace() {
        if (place == null) {
            place = new ArrayList<>();
        }
        return this.place;
    }

}
