
package com.sag.eiti.dto.bike;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for placeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="placeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bike" type="{}bikeType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="uid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lat" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lng" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="spot" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="number" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bikes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="booked_bikes" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bike_racks" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="free_racks" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="terminal_type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bike_numbers" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bike_types" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="place_type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="maintenance" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bike" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "placeType", propOrder = {
    "content"
})
@Getter
@Setter
public class PlaceType {

    @XmlElementRef(name = "bike", type = JAXBElement.class, required = false)
    @XmlMixed
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected List<Serializable> content;
    @XmlAttribute(name = "uid")
    protected String uid;
    @XmlAttribute(name = "lat")
    protected String lat;
    @XmlAttribute(name = "lng")
    protected String lng;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "spot")
    protected String spot;
    @XmlAttribute(name = "number")
    protected String number;
    @XmlAttribute(name = "bikes")
    protected String bikes;
    @XmlAttribute(name = "booked_bikes")
    protected String bookedBikes;
    @XmlAttribute(name = "bike_racks")
    protected String bikeRacks;
    @XmlAttribute(name = "free_racks")
    protected String freeRacks;
    @XmlAttribute(name = "terminal_type")
    protected String terminalType;
    @XmlAttribute(name = "bike_numbers")
    protected String bikeNumbers;
    @XmlAttribute(name = "bike_types")
    protected String bikeTypes;
    @XmlAttribute(name = "place_type")
    protected String placeType;
    @XmlAttribute(name = "maintenance")
    protected String maintenance;
    @XmlAttribute(name = "bike")
    protected String bike;

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link BikeType }{@code >}
     * {@link String }
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

}
