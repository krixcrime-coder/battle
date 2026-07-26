package com.app.rush47.models;

/**
 * One card in the "Esports Games" 3-per-row grid (e.g. FULL MAP,
 * CS 1VS1, SURVIVAL...). Fully database-driven via categories.php -
 * tapping a card opens redirectUrl if one is set.
 */
public class Category {

    private final String categoryId;
    private final String name;
    private final String imageUrl;
    private final String redirectUrl;

    public Category(String categoryId, String name, String imageUrl, String redirectUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.redirectUrl = redirectUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
