package com.gmail.sneakdevs.diamondchestshop.interfaces;

import java.util.UUID;

public interface SignBlockEntityInterface {
    void diamondchestshop_setOwner(String newOwner);
    void diamondchestshop_setItemEntity(UUID newEntity);
    void diamondchestshop_setShop(boolean newShop);
    void diamondchestshop_setAdminShop(boolean newAdminShop);
    String diamondchestshop_getOwner();
    UUID diamondchestshop_getItemEntity();
    boolean diamondchestshop_getAdminShop();
    boolean diamondchestshop_getShop();
}