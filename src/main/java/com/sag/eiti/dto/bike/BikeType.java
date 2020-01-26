
package com.sag.eiti.dto.bike;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for bikeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="bikeType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="number" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="bike_type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lock_types" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="active" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="state" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="electric_lock" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="boardcomputer" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pedelec_battery" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "bikeType", propOrder = {
    "value"
})
@Getter
@Setter
public class BikeType {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "number")
    protected String number;
    @XmlAttribute(name = "bike_type")
    protected String bikeType;
    @XmlAttribute(name = "lock_types")
    protected String lockTypes;
    @XmlAttribute(name = "active")
    protected String active;
    @XmlAttribute(name = "state")
    protected String state;
    @XmlAttribute(name = "electric_lock")
    protected String electricLock;
    @XmlAttribute(name = "boardcomputer")
    protected String boardcomputer;
    @XmlAttribute(name = "pedelec_battery")
    protected String pedelecBattery;
}
