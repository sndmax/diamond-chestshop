package com.gmail.sneakdevs.diamondchestshop.sql;

public interface ChestshopDatabaseManager {
    int addShop(String item, String nbt);
    int getMostRecentId();
    int playerShopCount(String uuid);
    String getItem(int id);
    String getNbt(int id);
    void removeShop(int id);
}
