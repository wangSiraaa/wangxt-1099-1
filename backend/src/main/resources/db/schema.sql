CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code VARCHAR(50) NOT NULL UNIQUE,
    user_name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL DEFAULT '123456',
    role_type VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pallet_code VARCHAR(50) NOT NULL UNIQUE,
    pallet_name VARCHAR(100),
    pallet_type VARCHAR(50),
    specification VARCHAR(200),
    deposit_amount DECIMAL(10,2) NOT NULL DEFAULT 100.00,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_pallet_code (pallet_code),
    INDEX idx_pallet_status (status)
);

CREATE TABLE IF NOT EXISTS account_period (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_code VARCHAR(20) NOT NULL UNIQUE,
    period_name VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    close_time TIMESTAMP,
    close_by BIGINT,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS deposit_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipper_id BIGINT NOT NULL,
    shipper_name VARCHAR(100),
    total_paid DECIMAL(12,2) DEFAULT 0.00,
    total_deducted DECIMAL(12,2) DEFAULT 0.00,
    total_refunded DECIMAL(12,2) DEFAULT 0.00,
    current_balance DECIMAL(12,2) DEFAULT 0.00,
    frozen_amount DECIMAL(12,2) DEFAULT 0.00,
    available_amount DECIMAL(12,2) DEFAULT 0.00,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS deposit_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(30) NOT NULL UNIQUE,
    shipper_id BIGINT NOT NULL,
    shipper_name VARCHAR(100),
    payment_amount DECIMAL(12,2) NOT NULL,
    payment_date DATE NOT NULL,
    period_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    payment_method VARCHAR(50),
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pallet_pickup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pickup_no VARCHAR(30) NOT NULL UNIQUE,
    shipper_id BIGINT NOT NULL,
    shipper_name VARCHAR(100),
    carrier_id BIGINT,
    carrier_name VARCHAR(100),
    dest_store_id BIGINT,
    dest_store_name VARCHAR(100),
    pallet_count INT NOT NULL,
    total_deposit DECIMAL(12,2) NOT NULL,
    pickup_date DATE NOT NULL,
    period_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_USE',
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_pickup_shipper_id (shipper_id),
    INDEX idx_pickup_carrier_id (carrier_id),
    INDEX idx_pickup_date (pickup_date),
    INDEX idx_pickup_status (status)
);

CREATE TABLE IF NOT EXISTS pallet_pickup_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pickup_id BIGINT NOT NULL,
    pallet_id BIGINT NOT NULL,
    pallet_code VARCHAR(50),
    deposit_amount DECIMAL(10,2) NOT NULL,
    current_holder_id BIGINT,
    current_holder_name VARCHAR(100),
    return_status VARCHAR(20) DEFAULT 'NOT_RETURNED',
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pickup_id (pickup_id),
    INDEX idx_pallet_id (pallet_id),
    INDEX idx_current_holder_id (current_holder_id),
    INDEX idx_return_status (return_status)
);

CREATE TABLE IF NOT EXISTS pallet_transfer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_no VARCHAR(30) NOT NULL UNIQUE,
    pickup_detail_id BIGINT NOT NULL,
    pallet_id BIGINT NOT NULL,
    pallet_code VARCHAR(50),
    from_carrier_id BIGINT NOT NULL,
    from_carrier_name VARCHAR(100),
    to_carrier_id BIGINT NOT NULL,
    to_carrier_name VARCHAR(100),
    shipper_id BIGINT NOT NULL,
    shipper_name VARCHAR(100),
    deposit_bearer_id BIGINT,
    deposit_bearer_name VARCHAR(100),
    deposit_amount DECIMAL(10,2),
    transfer_date DATE NOT NULL,
    period_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_transfer_pickup_detail_id (pickup_detail_id),
    INDEX idx_transfer_pallet_id (pallet_id),
    INDEX idx_transfer_from_carrier_id (from_carrier_id),
    INDEX idx_transfer_to_carrier_id (to_carrier_id),
    INDEX idx_transfer_status (status)
);

CREATE TABLE IF NOT EXISTS pallet_return (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_no VARCHAR(30) NOT NULL UNIQUE,
    carrier_id BIGINT NOT NULL,
    carrier_name VARCHAR(100),
    shipper_id BIGINT NOT NULL,
    shipper_name VARCHAR(100),
    return_count INT NOT NULL,
    normal_count INT DEFAULT 0,
    missing_part_count INT DEFAULT 0,
    stain_count INT DEFAULT 0,
    scrapped_count INT DEFAULT 0,
    lost_count INT DEFAULT 0,
    damaged_count INT DEFAULT 0,
    return_date DATE NOT NULL,
    period_id BIGINT,
    deduction_amount DECIMAL(12,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    INDEX idx_carrier_id (carrier_id),
    INDEX idx_shipper_id (shipper_id),
    INDEX idx_return_date (return_date),
    INDEX idx_period_id (period_id)
);

CREATE TABLE IF NOT EXISTS pallet_return_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_id BIGINT NOT NULL,
    pickup_id BIGINT,
    pallet_id BIGINT NOT NULL,
    pallet_code VARCHAR(50),
    return_status VARCHAR(20) NOT NULL,
    damage_type VARCHAR(20),
    deposit_amount DECIMAL(10,2),
    deduction_amount DECIMAL(10,2) DEFAULT 0.00,
    remark VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS deduction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    deduction_no VARCHAR(30) NOT NULL UNIQUE,
    return_id BIGINT,
    shipper_id BIGINT NOT NULL,
    shipper_name VARCHAR(100),
    deduction_type VARCHAR(20) NOT NULL,
    damage_type VARCHAR(20),
    deduction_amount DECIMAL(12,2) NOT NULL,
    deduction_date DATE NOT NULL,
    period_id BIGINT,
    pallet_count INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);
