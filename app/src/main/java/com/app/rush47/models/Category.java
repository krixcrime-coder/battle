package com.app.rush47.models;

/**
 * One card in the "Esports Games" 3-per-row grid (e.g. FULL MAP,
 * CS 1VS1, SURVIVAL...). Fully database-driven via categories.php.
 * Tapping a card opens the Matches list for gameId if one is set,
 * otherwise falls back to redirectUrl (a plain external link card).
 */
public class Category {

    private final String categoryId;
    private final String name;
    private final String imageUrl;
    private final String redirectUrl;
    private final String gameId;

    public Category(String categoryId, String name, String imageUrl, String redirectUrl, String gameId) {
        this.categoryId = categoryId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.redirectUrl = redirectUrl;
        this.gameId = gameId;
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

    public String getGameId() {
        return gameId;
    }
}
