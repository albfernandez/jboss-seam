/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package com.jboss.dvd.seam;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;


@Entity
@Table(name="PRODUCTS")
public class Product
    implements Serializable
{
    long productId;
    String asin;
    String title;
    String description;
    String imageURL;
    float price;

    List<Actor>    actors;
    Set<Category> categories;
    Inventory inventory;

    @Id @GeneratedValue
    @Column(name="PROD_ID")
    public long getProductId() {
        return productId;
    }                    
    public void setProductId(long id) {
        this.productId = id;
    }     

    @Column(name="ASIN", length=16)
    public String getASIN() {
        return asin;
    }

    public void setASIN(String asin) {
        this.asin = asin;
    }

    @OneToOne(fetch=FetchType.LAZY,mappedBy="product")
    public Inventory getInventory() {
        return inventory;
    }
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="PRODUCT_ACTORS",
               joinColumns=@JoinColumn(name="PROD_ID"),
               inverseJoinColumns=@JoinColumn(name="ACTOR_ID"))
    public List<Actor> getActors() {
        return actors;
    }
    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    
    @ManyToMany
    @JoinTable(name="PRODUCT_CATEGORY",
               joinColumns=@JoinColumn(name="PROD_ID"),
               inverseJoinColumns=@JoinColumn(name="CATEGORY"))
    public Set<Category> getCategories() {
        return categories;
    }
    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }
    
    @Column(name="TITLE",nullable=false,length=50)
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name="DESCRIPTION",length=1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name="IMAGE_URL",length=256)
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Column(name="PRICE",nullable=false,precision=12,scale=2)
    public float getPrice() {
        return price;
    }
    public void setPrice(float price) {
        this.price=price;
    }
}
