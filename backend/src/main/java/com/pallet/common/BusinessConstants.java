package com.pallet.common;

public class BusinessConstants {
    public static final String ROLE_SHIPPER = "SHIPPER";
    public static final String ROLE_CARRIER = "CARRIER";
    public static final String ROLE_FINANCE = "FINANCE";
    public static final String ROLE_STORE = "STORE";

    public static final String PALLET_STATUS_AVAILABLE = "AVAILABLE";
    public static final String PALLET_STATUS_IN_USE = "IN_USE";
    public static final String PALLET_STATUS_DAMAGED = "DAMAGED";
    public static final String PALLET_STATUS_LOST = "LOST";
    public static final String PALLET_STATUS_SCRAPPED = "SCRAPPED";
    public static final String PALLET_STATUS_IN_TRANSIT = "IN_TRANSIT";

    public static final String PICKUP_STATUS_IN_USE = "IN_USE";
    public static final String PICKUP_STATUS_PARTIAL_RETURNED = "PARTIAL_RETURNED";
    public static final String PICKUP_STATUS_FULLY_RETURNED = "FULLY_RETURNED";
    public static final String PICKUP_STATUS_IN_TRANSIT = "IN_TRANSIT";

    public static final String RETURN_STATUS_NORMAL = "NORMAL";
    public static final String RETURN_STATUS_MISSING_PART = "MISSING_PART";
    public static final String RETURN_STATUS_STAIN = "STAIN";
    public static final String RETURN_STATUS_SCRAPPED = "SCRAPPED";
    public static final String RETURN_STATUS_LOST = "LOST";

    public static final String DAMAGE_TYPE_MISSING_PART = "MISSING_PART";
    public static final String DAMAGE_TYPE_STAIN = "STAIN";
    public static final String DAMAGE_TYPE_SCRAPPED = "SCRAPPED";

    public static final String TRANSFER_STATUS_CONFIRMED = "CONFIRMED";
    public static final String TRANSFER_STATUS_CANCELLED = "CANCELLED";

    public static final String DEDUCTION_TYPE_DAMAGED = "DAMAGED";
    public static final String DEDUCTION_TYPE_MISSING_PART = "MISSING_PART";
    public static final String DEDUCTION_TYPE_STAIN = "STAIN";
    public static final String DEDUCTION_TYPE_SCRAPPED = "SCRAPPED";
    public static final String DEDUCTION_TYPE_LOST = "LOST";
    public static final String DEDUCTION_TYPE_OTHER = "OTHER";

    public static final String PERIOD_STATUS_OPEN = "OPEN";
    public static final String PERIOD_STATUS_CLOSED = "CLOSED";

    public static final String DEPOSIT_STATUS_CONFIRMED = "CONFIRMED";

    public static final String LIFECYCLE_TYPE_PICKUP = "PICKUP";
    public static final String LIFECYCLE_TYPE_TRANSFER = "TRANSFER";
    public static final String LIFECYCLE_TYPE_RETURN = "RETURN";
    public static final String LIFECYCLE_TYPE_DEDUCTION = "DEDUCTION";
    public static final String LIFECYCLE_TYPE_SETTLEMENT = "SETTLEMENT";
}
