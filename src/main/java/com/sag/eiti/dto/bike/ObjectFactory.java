
package com.sag.eiti.dto.bike;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sag.eiti.dto package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Markers_QNAME = new QName("", "markers");
    private final static QName _PlaceTypeBike_QNAME = new QName("", "bike");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sag.eiti.dto
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BikeAvailability }
     * 
     */
    public BikeAvailability createMarkersType() {
        return new BikeAvailability();
    }

    /**
     * Create an instance of {@link CityType }
     * 
     */
    public CityType createCityType() {
        return new CityType();
    }

    /**
     * Create an instance of {@link PlaceType }
     * 
     */
    public PlaceType createPlaceType() {
        return new PlaceType();
    }

    /**
     * Create an instance of {@link BikeType }
     * 
     */
    public BikeType createBikeType() {
        return new BikeType();
    }

    /**
     * Create an instance of {@link CountryType }
     * 
     */
    public CountryType createCountryType() {
        return new CountryType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BikeAvailability }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "markers")
    public JAXBElement<BikeAvailability> createMarkers(BikeAvailability value) {
        return new JAXBElement<BikeAvailability>(_Markers_QNAME, BikeAvailability.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BikeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "bike", scope = PlaceType.class)
    public JAXBElement<BikeType> createPlaceTypeBike(BikeType value) {
        return new JAXBElement<BikeType>(_PlaceTypeBike_QNAME, BikeType.class, PlaceType.class, value);
    }

}
