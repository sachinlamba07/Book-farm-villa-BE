package com.bookfarmvilla.entity;

import com.bookfarmvilla.entity.enums.ListingStatus;
import com.bookfarmvilla.entity.enums.PropertyType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
public class Property extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType type;

    private String address;

    @Column(nullable = false)
    private String city;

    /** Exact coordinates for the property, used to plot it on a map. Both optional. */
    private Double latitude;

    private Double longitude;

    @Column(name = "starting_price", precision = 12, scale = 2)
    private java.math.BigDecimal startingPrice;

    @Column(name = "ending_price", precision = 12, scale = 2)
    private java.math.BigDecimal endingPrice;

    private Integer capacity;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.ACTIVE;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("isPrimary DESC")
    private List<PropertyImage> images = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "property_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();
}
