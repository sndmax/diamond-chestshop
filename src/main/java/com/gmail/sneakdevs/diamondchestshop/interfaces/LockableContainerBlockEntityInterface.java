package com.gmail.sneakdevs.diamondchestshop.interfaces;

public interface LockableContainerBlockEntityInterface {
    void diamondchestshop_setOwner(String newOwner);
    void diamondchestshop_setItem(String newItem);
    void diamondchestshop_setNbt(String newNbt);
    void diamondchestshop_setShop(boolean newShop);
    String diamondchestshop_getOwner();
    String diamondchestshop_getItem();
    String diamondchestshop_getNbt();
    boolean diamondchestshop_getShop();
}