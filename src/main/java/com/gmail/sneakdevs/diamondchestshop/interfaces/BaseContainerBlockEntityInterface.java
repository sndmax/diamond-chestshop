package com.gmail.sneakdevs.diamondchestshop.interfaces;

public interface BaseContainerBlockEntityInterface {
    void diamondchestshop_setOwner(String newOwner);
    void diamondchestshop_setItem(String newItem);
    void diamondchestshop_setTag(String newNbt);
    void diamondchestshop_setShop(boolean newShop);
    void diamondchestshop_setId(int newId);

    String diamondchestshop_getOwner();
    String diamondchestshop_getItem();
    String diamondchestshop_getNbt();
    boolean diamondchestshop_getShop();
    int diamondchestshop_getId();
}